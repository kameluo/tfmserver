package projectserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class serverexample {

	public static void main(String[] args) {
		String message="hello from server";
		try {
			DatagramPacket datagrampacket=new DatagramPacket(message.getBytes(),message.length(),InetAddress.getByName("192.168.1.62"),4000);
			DatagramSocket socket=new DatagramSocket();
			socket.send(datagrampacket);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
