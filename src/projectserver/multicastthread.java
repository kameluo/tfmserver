package projectserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class MulticastthreadRun implements Runnable,serverInterface{	
	public ArrayList<Client> ClientIpArrayList=new ArrayList<Client>();//Array List For Saving The IPs of the Clients

	static int serverstate;//flag 
	
	@Override
	public void run() {
				while(true){
				try {
					//First step is to send a multicast message for all the clients
					int portmulticast=3456;
					InetAddress group=InetAddress.getByName("225.4.5.6");//creating a multicast IP address
					MulticastSocket multicastSocket=new MulticastSocket(portmulticast);//opening a multicast socket port
					multicastSocket.joinGroup(group);//subscribing the multicast IP address to  that socket port,listening to the messages of that IP address from that port
					
					
					//Receiving the "CRQ" message from the Client by a multicast datagram object,(-->datagrampacketsentmulticastmessage2) 
					byte [] b2=new byte[100];
					DatagramPacket datagramPacketunicastmessage2=new DatagramPacket(b2, b2.length);
					multicastSocket.receive(datagramPacketunicastmessage2);//receiving the "CRQ" message in a multicast socket  
					InetAddress clientIP=datagramPacketunicastmessage2.getAddress();//getting the IP of the client side in bytes format
					int clientPort=datagramPacketunicastmessage2.getPort();//getting the Port Number of the client side
					String clientIPString=clientIP.toString();//converting the IP from Bytes format to String format to access the client IPs Array list
					String clientPortString=String.valueOf(clientPort);//converting the Port from integer format to String format to access the client IPs Array list
					DatagramSocket datagramSocketunicast=new DatagramSocket(clientPort);//creating an object from the datasocket class to send all the unicast packages through it

					System.out.println("after receiving the CRQ");
					
					//Sending the log In message to the whole group by a unicast datagram object,(-->datagrampacketsentmulticastmessage1)
					DatagramPacket datagrampacketsentmulticastmessage1=new DatagramPacket(loginMessage.getBytes(),loginMessage.length(),clientIP,clientPort);
					datagramSocketunicast.send(datagrampacketsentmulticastmessage1);
					
					Client clnt=new Client(clientIPString,clientPortString);
					//clnt.setClientIP(clientIPString);
					clnt.setClientIPandPort(clientIPString,clientPortString);
					//check before adding in the Arraylist
					if(addClient(clnt)>-1){
					
					b2=datagramPacketunicastmessage2.getData();
					String messagereceived=new String (b2);
					System.out.println(messagereceived);
						if(messagereceived.equals("CRQ")){
							System.out.println("hello from if condition------------------");
							//Sending the "acknowledgementSoundState" means that the server has received the "CRQ" message unicast datagram object,(-->datagrampacketsentmulticastmessage3)
							byte [] byteAcknowledgement=acknowledgementSoundState.getBytes();//Transferring the Strings to Bytes
							DatagramPacket datagramPacketUnicast3=new DatagramPacket(byteAcknowledgement,byteAcknowledgement.length,clientIP,clientPort);//creating the packet
							datagramSocketunicast.send(datagramPacketUnicast3);//send the packet
							clnt.setStatus(1);//the server is ready to receive 
							
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
class UniCastThreadRun implements Runnable, serverInterface{ //client
	
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
		
		String clientIPString=client.getClientIP();
		InetAddress clientIP = null;
			try {
				clientIP = InetAddress.getByName(clientIPString);
			} catch (UnknownHostException e3) {
				e3.printStackTrace();
			}//converting the string format to inetaddress format
		String clientPortString=client.getClientPort();
		int clientPortInteger=Integer.parseInt(clientPortString);//converting the string format to integer format
		
		//int portunicast=2000;
		DatagramSocket datagramSocketunicast = null;
		try {
		datagramSocketunicast = new DatagramSocket(clientPortInteger);
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
		MulticastSocket multicastSocket = null;//for disconnecting
		try {
			multicastSocket = new MulticastSocket(3456);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		InetAddress group = null;
		try {
			group = InetAddress.getByName("225.4.5.6");
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		}
		while(client.getStatus() == 1){
				//Receiving the Sound States,(-->datagramPacketSoundStates4)
				byte [] bsoundstates=new byte[100];
				DatagramPacket datagramPacketSoundStates4=new DatagramPacket(bsoundstates, bsoundstates.length);
				
				try {
					datagramSocketunicast.receive(datagramPacketSoundStates4);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				String soundStateMessageRecieved=new String(bsoundstates);
				//InetAddress clientIP=datagramPacketSoundStates4.getAddress();//getting the IP of the client side in bytes format
				//client.setClientIP(clientIP.toString());//  ask juan carlos about it
				
				System.out.println(soundStateMessageRecieved);
				
				//Sending Acknowledgment to the client to let him know that the server received the Sound State Message,(-->datagramPacketUnicastSoundState5)
				byte [] byteAcknowledgementSoundState=acknowledgementSoundState.getBytes();//Transferring the Strings to Bytes
				DatagramPacket datagramPacketUnicastSoundState5=new DatagramPacket(byteAcknowledgementSoundState,byteAcknowledgementSoundState.length,clientIP,clientPortInteger);//creating the packet
				
				try {
					datagramSocketunicast.send(datagramPacketUnicastSoundState5);//send the packet
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//Identifying the received message
				String soundState="";
					if(soundStateMessageRecieved.charAt(3)=='0'){
						soundState="Speech";//Speech=SND0
						System.out.println(soundState);
					}else if(soundStateMessageRecieved.charAt(3)=='1'){
						soundState="Alarm";//Alarm=SND1
						System.out.println(soundState);
					}else if(soundStateMessageRecieved.charAt(3)=='2'){
						soundState="Silence";//Silence==SND2
						System.out.println(soundState);
					}else if(soundStateMessageRecieved.charAt(2)=='Q'){
						//Receiving "DRQ" from the client means that he will disconnect
						//close and disconnect the datagramSocketForUniCast
						datagramSocketunicast.close();
						datagramSocketunicast.disconnect();
						//leave the multicastSocket
						try {
							multicastSocket.leaveGroup(group);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
						//===================check this step with juan carlos
						ClientIpArrayList.remove(clientIP);//removing the client IP from the ArrayList
						
						client.setStatus(0);//setting the flag 0 to not access the if condition again
					}else{
						//if the client sends something else rather than the sound states or the disconnect message we will send him "500" message,(-->datagramPacketUnicastunknownCommandMessage6)
						System.out.println("UnKnown Command !!!");
						byte [] byteunknownCommandMessage=unknownCommandMessage.getBytes();//Transferring the Strings to Bytes
						DatagramPacket datagramPacketUnicastunknownCommandMessage6=new DatagramPacket(byteunknownCommandMessage,byteunknownCommandMessage.length,clientIP,clientPortInteger);//creating the packet
						
						try {
							datagramSocketunicast.send(datagramPacketUnicastunknownCommandMessage6);//send the packet
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			
					//String Contains the received sound state,the date and time of receiving it and the IP of the client
					String currentState=dateformat.format(currentdate)+" "+clientIP+" "+soundState;
		
					//Write the received state in The Log File Of The Server
					try {
						FileWriter fileWriterSoundStates=new FileWriter(file,true);
						fileWriterSoundStates.write(currentState+"\r\n");
						fileWriterSoundStates.flush();
						fileWriterSoundStates.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
							
		}//the end of the attention loop it finishes when the client status goes to 0
		//TODO other cleaning operations if needed
		
		
	}//the end of the run loop
	
	
	
}//the end of the UniCastThreadRun class
}//end multicast