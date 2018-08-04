package projectserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import projectserver.MulticastthreadRun2.UniCastThreadRun;

public class BroadcastServer implements Runnable,serverInterface {	
			public static final int SERVICE_UNICAST_PORT = 9000;
			public static final int SERVICE_BROADCAST_PORT = 9999;// receiving port
			
			public ArrayList<Client> ClientIpArrayList=new ArrayList<Client>();//Array List For Saving The IPs of the Clients

			static int serverstate;//flag 
			static String oldstate="SD2";

			DatagramSocket datagramSocketBroadcast;
			DatagramSocket datagramSocketUnicast;

			@Override
			public void run() {
				try {
					datagramSocketUnicast=new DatagramSocket(SERVICE_UNICAST_PORT);
					datagramSocketBroadcast=new DatagramSocket(SERVICE_BROADCAST_PORT);
				} catch (SocketException e) {
					e.printStackTrace();

				} 
				while(true){
						try {
							//Receiving the "CRQ" message from the Client by a Multicast datagram object 
								byte [] byteBroadCast=new byte[3];
								DatagramPacket datagramPacketBroadCast=new DatagramPacket(byteBroadCast,byteBroadCast.length);
								datagramSocketBroadcast.receive(datagramPacketBroadCast);
								String multiMessage=new String(byteBroadCast);
								System.out.println(multiMessage);
								
								InetAddress clientIP=datagramPacketBroadCast.getAddress();
								int clientPort=datagramPacketBroadCast.getPort();
								System.out.println(clientIP+"<<<<<<<<<<<<<<<<<<<<<<<<<<<");
								System.out.println(clientPort+"___________________________");
								setclientIP(clientIP);//Getting the IP of the the received message 
								setclientPort(clientPort);//Getting the Port of the the received message 
								
								String clientIPString=clientIP.getHostAddress();//converting the IP from Bytes format to String format to access the client IPs Array list
								String clientPortString=String.valueOf(clientPort);//converting the Port from integer format to String format to access the client IPs Array list
								System.out.println(multiMessage.equals("CRQ"));
							//the end of the broadcast
							System.out.println("after receiving the CRQ");
							
							//Sending the log In message to the whole group by a unicast datagram object
							send(loginMessage,clientIP,clientPort,datagramSocketUnicast);
							
							//passing the ClientIP and the Client Port to the client class to use them in the unicast thread later
							Client clnt=new Client(clientIPString,clientPortString);
							//check before adding in the Arraylist
							if(addClient(clnt)>-1){
								if(multiMessage.equals("CRQ")){
									System.out.println("hello from if condition------------------");
									clnt.setStatus("1");//the server is ready to receive 
									//thread to start the unicast sending and receiving messages
									Thread uniCastThread =new Thread(new UniCastThreadRun(clnt));
									uniCastThread.start();
								}
							}
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						}//the end of the infinite while ,to be able to wait for many clients
			}
			
			private int addClient(Client c){
				//TODO check if the client already exists prior to insert it in the list
				if(!ClientIpArrayList.contains(c)){//checking if the array list contains that IP address or not,if not we will add it to it
					ClientIpArrayList.add(c);
					return ClientIpArrayList.size();
				}else
					return -1;
			}
		//TODO The UniCast Class
		class UniCastThreadRun implements Runnable, serverInterface{//client
			Client client = null;
			
			
			UniCastThreadRun(Client c){
				client=c;
				
			}
			//Constructing the date
					DateFormat dateformat = new SimpleDateFormat("dd/MM/yy HH:mm a");//To Set the Format of the Date
					Date currentdate = new Date();//To Get the Current Date
			
					
					
				
			@Override
			public void run() {		
				//creating a log file for the receiver side
				File file=new File("logserver.txt");
				if(!file.exists()){
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				System.out.println(client.getStatus());
				String state=client.getStatus();
				System.out.println(state.length());
				System.out.println(state.equals("1"));
				
				while(state.equals("1")){
						//Receiving the Sound States
						String soundStateMessageRecieved=recievemessage(datagramSocketUnicast);
						System.out.println(datagramSocketUnicast.getPort()+" "+soundStateMessageRecieved);
						//Sending Acknowledgment to the client to let him know that the server received the Sound State Message
						send(acknowledgementSoundState,clientIP,getclientPort(),datagramSocketUnicast);//16-7-2018
						//Identifying the received message
						String soundState="";
							if(soundStateMessageRecieved.equals("SD0")){
								soundState="Speech";//Speech=SD0
								System.out.println(soundState);
							}else if(soundStateMessageRecieved.equals("SD1")){
								soundState="Alarm";//Alarm=SD1
								System.out.println(soundState);
							}else if(soundStateMessageRecieved.equals("SD2")){
								soundState="Silence";//Silence==SD2
								System.out.println(soundState);
							}else if(soundStateMessageRecieved.equals("DQR")){
								//Receiving "DQR" from the client means that he will disconnect
								//close and disconnect the datagramSocketForUniCast
								//datagramSocketunicast.close();
								//datagramSocketunicast.disconnect();
								
							
								//===================check this step with juan carlos
								ClientIpArrayList.remove(client);//removing the client IP from the ArrayList
								client.setStatus("0");//setting the flag 0 to not access the if condition again
							}else{
								//if the client sends something else rather than the sound states or the disconnect message we will send him "500" message,(-->datagramPacketUnicastunknownCommandMessage6)
								System.out.println("UnKnown Command !!!");
								send(unknownCommandMessage,clientIP,clientPort,datagramSocketUnicast);
							}
					
							//String Contains the received sound state,the date, time of receiving it and the IP of the client
							String currentState=dateformat.format(currentdate)+" "+clientIP+" "+soundState;
				
							if(!oldstate.equals(soundStateMessageRecieved)){
								oldstate=soundStateMessageRecieved;
								//Write the received state in The Log File Of The Server
								try {
									FileWriter fileWriterSoundStates=new FileWriter(file,true);
									fileWriterSoundStates.write(currentState+"\r\n");
									fileWriterSoundStates.flush();
									fileWriterSoundStates.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
				}//the end of the attention loop it finishes when the client status goes to 0
			}//the end of the run loop
		}//the end of the UniCastThreadRun class
		/***************************************** The Methods ******************************************************/
			/**
			 * Sending Packets Method
			 * @param message-the message we want to send to the client side
			 * @param IP-in InetAddress format
			 * @param Port-in integer format
			 * @return Null
			 */
			public void send(String message,InetAddress IP,int Port,DatagramSocket datagramSocketsending){
				byte [] buffer=message.getBytes();
				DatagramPacket datagrampacket=new DatagramPacket(buffer,buffer.length,IP,SERVICE_UNICAST_PORT); 
				//datagrampacket.setPort();
				try {
					datagramSocketsending.send(datagrampacket);
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/**
			 * message we will receive from the client side
			 * @param IP of the socket in SocketAddress format
			 * @return message received from the client side in string format
			 */
			public  String recievemessage(DatagramSocket datagramSocketrecieving){
				byte [] buffer=new byte [3];
				DatagramPacket datagrampacket=new DatagramPacket(buffer,buffer.length);
				try {
					datagramSocketrecieving.receive(datagrampacket);
					BroadcastServer.setclientPort(datagrampacket.getPort());
					System.out.println("IP "+datagrampacket.getAddress().toString()+" PORT:"+datagrampacket.getPort());
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String message=new String(buffer);
				InetAddress clientIP=datagrampacket.getAddress();
				setclientIP(clientIP);
				int port=datagrampacket.getPort();
				setclientPort(port);
				return message;
			}
			private static InetAddress clientIP;
			private static int clientPort;
			private static SocketAddress socket;
			/** Getter and Setter IP,Port "for the receiving method" and Socket **/
			public static void setclientIP(InetAddress clientIP){
				BroadcastServer.clientIP=clientIP;
			}
			public static InetAddress getclientIP(){
				return clientIP;
			}
			public static void setclientPort(int clientIPort){
				BroadcastServer.clientPort=clientIPort;
			}
			public static int getclientPort(){
				return clientPort;
			}
			public static void setsocket(SocketAddress socket){
				BroadcastServer.socket=socket;
			}
			public static SocketAddress getsocket(){
				return socket;
			}
}//end multicast