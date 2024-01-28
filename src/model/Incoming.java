package model;

public class Incoming{
    public Type type;
    private String payload;
    
	public Incoming(Type type, String payload) {
		this.type = type;
		this.payload = payload;
	}

	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public String getPayload() {
		return payload;
	}
	
	public void setPayload(String payload) {
		this.payload = payload;
	}
}
