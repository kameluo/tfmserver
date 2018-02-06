package projectserver;
class Client{
	//=============================ask juan carlos about the static in the 3 variables
	private static String clientIP;
	private static String clientport;
	private static String status;

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
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
}