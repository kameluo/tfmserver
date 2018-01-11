package projectserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

class multicastthreadRun implements Runnable,serverInterface{
	//ArrayList<Client> ClientIpArrayList=new ArrayList<Client>();//Array List For Saving The IPs of the Clients
	
	
	
	Client clnt=new Client();
	
	/*
	public void setMyList(ArrayList<Client> ClientIpArrayList){
		this.ClientIpArrayList=ClientIpArrayList;
	}
	
	public ArrayList getMyList(){
		return ClientIpArrayList;
	}
	*/
	
	
	
	
	static int serverstate;//flag 
	
	@Override
	public void run() {
		
				
				try {
					
					//First step is to send a multicast message for all the clients
					int portmulticast=3456;
					InetAddress group=InetAddress.getByName("225.4.5.6");//creating a multicast IP address
					MulticastSocket multicastSocket=new MulticastSocket(portmulticast);//opening a multicast socket port
					multicastSocket.joinGroup(group);//subscribing the multicast IP address to  that socket port,listening to the messages of that IP address from that port
					
					//Sending the log In message to the whole group by a multicast datagram object,(-->datagrampacketsentmulticastmessage1)
					DatagramPacket datagrampacketsentmulticastmessage1=new DatagramPacket(loginMessage.getBytes(),loginMessage.length(),group,portmulticast);
					multicastSocket.send(datagrampacketsentmulticastmessage1);
					
					//Receiving the "CRQ" message from the Client by a unicast datagram object,(-->datagrampacketsentmulticastmessage2)
					int portunicast=2000;
					byte [] b2=new byte[100];
					DatagramSocket datagramSocketunicast=new DatagramSocket(portunicast);//creating an object from the datasocket class to send all the unicast packages through it
					DatagramPacket datagramPacketunicastmessage2=new DatagramPacket(b2, b2.length);
					datagramSocketunicast.receive(datagramPacketunicastmessage2);
					InetAddress clientIP=datagramPacketunicastmessage2.getAddress();//getting the IP of the client side in bytes format
					String clientIPString=clientIP.toString();//converting the IP from Bytes format to String format to access the client IPs Array list
					
					
					//================the new part
					ArrayList<String> myarray=clnt.getMyList();
					if(!myarray.contains(clientIPString)){//checking if the array list contains that IP address or not,if not we will add it to it
						myarray.add(clientIPString);	
						
						clnt.setMyList(myarray);
					}
					
					b2=datagramPacketunicastmessage2.getData();
					String messagereceived=new String (b2);
					System.out.println(messagereceived);
					if(messagereceived.equals("CRQ")){
						//Sending the "200" means that the server has received the "CRQ" message unicast datagram object,(-->datagrampacketsentmulticastmessage3)
						String acknowledgement="200";
						byte [] byteAcknowledgement=acknowledgement.getBytes();//Transferring the Strings to Bytes
						DatagramPacket datagramPacketUnicast3=new DatagramPacket(byteAcknowledgement,byteAcknowledgement.length,clientIP,portunicast);//creating the packet
						datagramSocketunicast.send(datagramPacketUnicast3);//send the packet
						serverstate=1;//the server is ready to receive 
						
						//thread to start the unicast sending and recieving messages
						
						
						
						
						Thread uniCastThread =new Thread(new uniCastThreadRun(serverstate));
						uniCastThread.start();

						
					}
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
		
	}
}

class uniCastThreadRun implements Runnable, serverInterface{ //client
	int stateclient;
	uniCastThreadRun(int serverstate){
		stateclient= serverstate;
	}
	
	@Override

	public void run() {
		
		
		int portunicast=2000;
		DatagramSocket datagramSocketunicast = null;
		try {
		datagramSocketunicast = new DatagramSocket(portunicast);
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
		MulticastSocket multicastSocket = null;
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
		while(true){
			if(stateclient == 1){   
				//Receiving the Sound States,(-->datagramPacketSoundStates4)
				byte [] bsoundstates=new byte[100];
				DatagramPacket datagramPacketSoundStates4=new DatagramPacket(bsoundstates, bsoundstates.length);
				
				try {
					datagramSocketunicast.receive(datagramPacketSoundStates4);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				String soundStateMessageRecieved=new String(bsoundstates);
				InetAddress clientIP=datagramPacketSoundStates4.getAddress();//getting the IP of the client side in bytes format

				System.out.println(soundStateMessageRecieved);
				
				//Sending Acknowledgment to the client to let him know that the server received the Sound State Message,(-->datagramPacketUnicastSoundState5)
				byte [] byteAcknowledgementSoundState=acknowledgementSoundState.getBytes();//Transferring the Strings to Bytes
				DatagramPacket datagramPacketUnicastSoundState5=new DatagramPacket(byteAcknowledgementSoundState,byteAcknowledgementSoundState.length,clientIP,portunicast);//creating the packet
				
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
						//multiCastObject.ClientIpArrayList.remove(clientIP);//Removing the the Client IP from the array list
						
						//==========================================not sure about it
						Client clnt=new Client();
						ArrayList<String> myarray=clnt.getMyList();
						myarray.remove(clientIP);
						clnt.setMyList(myarray);
						
						stateclient = 0;//setting the flag 0 to not access the if condition again
						try {
							multicastSocket.leaveGroup(group);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else{
						//if the client sends something else rather than the sound states or the disconnect message we will send him "500" message,(-->datagramPacketUnicastunknownCommandMessage6)
						System.out.println("UnKnown Command !!!");
						byte [] byteunknownCommandMessage=unknownCommandMessage.getBytes();//Transferring the Strings to Bytes
						DatagramPacket datagramPacketUnicastunknownCommandMessage6=new DatagramPacket(byteunknownCommandMessage,byteunknownCommandMessage.length,clientIP,portunicast);//creating the packet
						
						try {
							datagramSocketunicast.send(datagramPacketUnicastunknownCommandMessage6);//send the packet
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			}
		}//the end of the infinite while loop
		
	}//the end of the run loop
}

class Client{
	//=============================ask juan carlos about the static
	String ClientIP;
	ArrayList<String> ClientIpArrayList=new ArrayList<String>();//Array List For Saving The IPs of the Clients
	
	/*
	public void setclientIP(String clientip){
		this.ClientIP=clientip;
		ClientIpArrayList.add(ClientIP);
	}
	
	public String getClientIP(){
		return ClientIP;
	}
	*/
	
	public void setMyList(ArrayList<String> ClientIpArrayList){
		this.ClientIpArrayList=ClientIpArrayList;
	}
	
	
	public ArrayList getMyList(){
		return ClientIpArrayList;
	}
	
}

public class multicastthread {

	public static void main(String[] args) {

		Thread multiCastThread=new Thread(new multicastthreadRun());
		
		multiCastThread.start();
		
	}

}

