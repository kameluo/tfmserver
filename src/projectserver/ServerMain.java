package projectserver;

public class ServerMain {

		public static void main(String[] args) {

			Thread multiCastThread=new Thread(new MulticastthreadRun());
			
			multiCastThread.start();
			
		}


}
