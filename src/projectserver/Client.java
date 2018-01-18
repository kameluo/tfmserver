package projectserver;

class Client{
	//=============================ask juan carlos about the static
	String clientIP;
	int status=0;// 
	//
	String clientport;
	
	

	public void setClientIPandPort(String clientIP,String clientport) {
		this.clientIP = clientIP;
		this.clientport=clientport;
	}
	
	public String getClientPort() {
		return clientport;
	}

	
	
	
	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
	public Client(int status){
		this.status=status;
	}
	
	public Client(String ip){
		clientIP=ip;
	}
	
	public Client(String ip,String port){
		clientIP=ip;
		clientport=port;
	}
	/*
	public void setclientIP(String clientip){
		this.ClientIP=clientip;
		ClientIpArrayList.add(ClientIP);
	}
	
	public String getClientIP(){
		return ClientIP;
	}
	*/
}