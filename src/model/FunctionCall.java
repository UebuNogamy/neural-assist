package model;

import java.util.Map;

public class FunctionCall {
	private String name; 
	private Map<String, String> arguments;
	
	public FunctionCall(String name, Map<String, String> arguments) {
		super();
		this.name = name;
		this.arguments = arguments;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String, String> arguments) {
		this.arguments = arguments;
	}	
}
