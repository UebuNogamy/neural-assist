package services;


public class Function {
	private String name;
	private String description;
	private Parameters parameters;
	
	public Function(String name, String description, Parameters parameters) {
		super();
		this.name = name;
		this.description = description;
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
}
