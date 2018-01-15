package projectserver;

public interface serverInterface {
	String loginMessage="SEVRON";// "SEVON" is sent to the client as a log in message
	String acknowledgementSoundState="200";//Acknowledgment message sent to the client to let him know that the server received the Sound State Message
	String unknownCommandMessage="500";//if the client sends something else rather than the sound states or the disconnect message we will send him "500" message
	String serverWantsDisconnect="555";//the Server wants to disconnect
}
