package olap.olap.project.model;

public class Property {

	private String type, name;
	private boolean isPK;
	
	public Property(String name, String type, boolean isPK) {
		this.type = type;
		this.name = name;
		this.isPK = isPK;
	}

	public String getType() {
		return type;
	}
	public String getName() {
		return name;
	}
	public boolean isPK() {
		return isPK;
	}
	
	public void print() {
		System.out.println("PROP: "+name+", type:" + type + (isPK? " PK" :""));
	}
	
}

