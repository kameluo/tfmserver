package projectserver;

public class ServerMain {

		public static void main(String[] args) {

			//Thread multiCastThread=new Thread(new MulticastthreadRun2());
			
			//multiCastThread.start();
			
			Thread BroadcastServer=new Thread(new BroadcastServer());
			
			BroadcastServer.start();
			
			
			
		}


}
