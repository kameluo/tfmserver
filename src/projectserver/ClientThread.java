package projectserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientThread extends Thread implements serverInterface{
	
	
	

	@Override
	public void run() {
		Client client = new Client(0);//Set the Status to "2"
		String ClientIP=client.getClientIP();
		String ClientPort=client.getClientPort();
		int portunicast=Integer.parseInt(ClientPort);;
		byte [] byteDisconnectMessage=serverWantsDisconnect.getBytes();//the server wants to disconnect so he is going to send "555"
		
		//Handshake
		try {
			DatagramSocket datagramSocketunicast=new DatagramSocket(portunicast);
			DatagramPacket datagramPacketUnicastMessage=new DatagramPacket(byteDisconnectMessage,byteDisconnectMessage.length,InetAddress.getByName(ClientIP),portunicast);//creating the packet
			datagramSocketunicast.send(datagramPacketUnicastMessage);// I am your attention server. then the client stores the ip and port of the client
			
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		boolean end=false;
		do{
			//wait for a message from the client
			DatagramSocket datagramSocketunicast = null;
				try {
					datagramSocketunicast = new DatagramSocket(portunicast);
				} catch (SocketException e) {
					e.printStackTrace();
				}
		   	byte [] byteDisconnect=new byte[100];
			DatagramPacket datagramPacket=new DatagramPacket(byteDisconnect, byteDisconnect.length);
				try {
					datagramSocketunicast.receive(datagramPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			byteDisconnect=datagramPacket.getData();
			String messagereceived=new String (byteDisconnect);
			
			if(messagereceived.equals("DRQ"))
				end=true;
		   
	   }while(!end);
	   }
	}
	//we will call that thread in android once we press the disconnect button

	

