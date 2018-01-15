package projectserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ServerDisconnect extends Thread implements serverInterface{
	
	
	

	@Override
	public void run() {
		Client client = new Client(0);//Set the Status to "2"
		String ClientIP=client.getClientIP();
		int portunicast=2000;
		byte [] byteDisconnectMessage=serverWantsDisconnect.getBytes();//the server wants to disconnect so he is going to send "555"
		try {
			DatagramSocket datagramSocketunicast=new DatagramSocket(portunicast);
			DatagramPacket datagramPacketUnicastMessage=new DatagramPacket(byteDisconnectMessage,byteDisconnectMessage.length,InetAddress.getByName(ClientIP),portunicast);//creating the packet
			datagramSocketunicast.send(datagramPacketUnicastMessage);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Ask juan carlos about the clientIP conversion in the packet right or wrong
	//we will call that thread in android once we press the disconnect button

	
}
