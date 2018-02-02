package projectserver;
class Client{
	//=============================ask juan carlos about the static in the 3 variables
	private static String clientIP;
	private static String clientport;
	int status=0;

	public Client(String ip,String port){
		clientIP=ip;
		clientport=port;
	}
	public String getClientPort() {
		return clientport;
	}
	public String getClientIP() {
		return clientIP;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getStatus() {
		return status;
	}
}