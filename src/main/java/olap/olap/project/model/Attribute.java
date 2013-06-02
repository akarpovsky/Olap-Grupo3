package olap.olap.project.model;

public enum Attribute {

	STRING("String"), NUMERIC("Numeric"), INTEGER("Integer"), BOOLEAN("Boolean"), DATE("Date"), TIME("Time"), TIMESTAMP("Timestamp");
	
	private String name;
	
	Attribute(String name){
		this.name = name;
	}
	
	public String toString(){
		return this.name;
	}
}
