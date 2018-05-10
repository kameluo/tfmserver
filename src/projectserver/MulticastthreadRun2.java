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

class MulticastthreadRun2 implements Runnable,serverInterface{	
	public ArrayList<Client> ClientIpArrayList=new ArrayList<Client>();//Array List For Saving The IPs of the Clients

	static int serverstate;//flag 
	static String oldstate="SD2";
	
	@Override
	public void run() {
				//while(true){
				try {
					//Receiving the "CRQ" message from the Client by a Multicast datagram object 
						int portMulticastCast=3456;//receiving port
						InetAddress group=InetAddress.getByName("225.4.5.6");
						InetSocketAddress mg = new InetSocketAddress(group,portMulticastCast);
						InetSocketAddress is = new InetSocketAddress("192.168.0.104",portMulticastCast);//the IP of this machine
						MulticastSocket multicastSocket=new MulticastSocket(is);
						NetworkInterface nis = NetworkInterface.getByInetAddress(is.getAddress());
						multicastSocket.joinGroup(mg,nis);//subscribing the multicast IP address to that socket,listening to the message
						
						byte [] bMulti=new byte[3];
						DatagramPacket datagrampacketmulticast=new DatagramPacket(bMulti,bMulti.length);
						multicastSocket.receive(datagrampacketmulticast);
						String multiMessage=new String(bMulti);
						System.out.println(multiMessage);
						
						InetAddress clientIP=datagrampacketmulticast.getAddress();
						int clientPort=datagrampacketmulticast.getPort();
						System.out.println(clientIP+"<<<<<<<<<<<<<<<<<<<<<<<<<<<");
						System.out.println(clientPort+"___________________________");
						setclientIP(clientIP);
						setclientPort(clientPort);
						
						String clientIPString=clientIP.getHostAddress();//converting the IP from Bytes format to String format to access the client IPs Array list
						String clientPortString=String.valueOf(clientPort);//converting the Port from integer format to String format to access the client IPs Array list
						
						SocketAddress socket = new InetSocketAddress("192.168.0.104",20002);
						System.out.println(multiMessage.equals("CRQ"));
						setsocket(socket);
					//the end of the broadcast
					System.out.println("after receiving the CRQ");
					
					//Sending the log In message to the whole group by a unicast datagram object
					send(loginMessage,clientIP,clientPort);
					
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
				//}//the end of the infinite while ,to be able to wait for many clients
	}
	
	public void setMyList(ArrayList<Client> ClientIpArrayList){
		this.ClientIpArrayList=ClientIpArrayList;
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
	@Override
	public void run() {
		//Constructing the date
		DateFormat dateformat = new SimpleDateFormat("dd/MM/yy HH:mm a");//To Set the Format of the Date
		Date currentdate = new Date();//To Get the Current Date
				
		//creating a log file for the receiver side
		File file=new File("logserver.txt");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		InetAddress clientIP =MulticastthreadRun2.getclientIP();
		int clientPort=MulticastthreadRun2.getclientPort();
		System.out.println(clientPort);
		//int clientPortInteger=Integer.parseInt(clientPortString);//converting the string format to integer format
		System.out.println(client.getStatus());
		String state=client.getStatus();
		System.out.println(state.length());
		System.out.println(state.equals("1"));
		
		
		while(state.equals("1")){
				//Receiving the Sound States
				String soundStateMessageRecieved=recievemessage(getsocket());
				System.out.println(soundStateMessageRecieved);
				//Sending Acknowledgment to the client to let him know that the server received the Sound State Message
				send(acknowledgementSoundState,clientIP,clientPort);
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
						ClientIpArrayList.remove(clientIP);//removing the client IP from the ArrayList
						client.setStatus("0");//setting the flag 0 to not access the if condition again
					}else{
						//if the client sends something else rather than the sound states or the disconnect message we will send him "500" message,(-->datagramPacketUnicastunknownCommandMessage6)
						System.out.println("UnKnown Command !!!");
						send(unknownCommandMessage,clientIP,clientPort);
					}
			
					//String Contains the received sound state,the date and time of receiving it and the IP of the client
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
		//TODO other cleaning operations if needed
		
	}//the end of the run loop
}//the end of the UniCastThreadRun class
//methods
	//Send Packets
	public static void send(String message,InetAddress IP,int Port){
		byte [] buffer=message.getBytes();
		DatagramPacket datagrampacket=new DatagramPacket(buffer,buffer.length,IP,Port); 
		datagrampacket.setPort(20002);
		try {
			DatagramSocket datagramSocket=new DatagramSocket();
			datagramSocket.send(datagrampacket);
			datagramSocket.setReuseAddress(true);
			datagramSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Receive Packet
	private static InetAddress clientIP;
	private static int clientPort;
	private static SocketAddress socket;
	public static String recievemessage(SocketAddress socket){
		byte [] buffer=new byte [3];
		DatagramPacket datagrampacket=new DatagramPacket(buffer,buffer.length);
		try {
			DatagramSocket datagramsocket=new DatagramSocket(socket);
			datagramsocket.receive(datagrampacket);
			datagramsocket.close();
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
	//Getter and Setter IP,Port "for the receiving method" and Socket 
	public static void setclientIP(InetAddress clientIP){
		MulticastthreadRun2.clientIP=clientIP;
	}
	public static InetAddress getclientIP(){
		return clientIP;
	}
	public static void setclientPort(int clientIPort){
		MulticastthreadRun2.clientPort=clientIPort;
	}
	public static int getclientPort(){
		return clientPort;
	}
	public static void setsocket(SocketAddress socket){
		MulticastthreadRun2.socket=socket;
	}
	public static SocketAddress getsocket(){
		return socket;
	}
}//end multicast