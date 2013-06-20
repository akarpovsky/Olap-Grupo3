package olap.olap.project.model;

public enum Attribute {

	STRING("string"), NUMERIC("numeric"), INTEGER("integer"), BOOLEAN("boolean"), DATE("date"), TIME("time"), TIMESTAMP("timestamp"),GEOMETRY("integer");
	
	private String name;
	
	Attribute(String name){
		this.name = name;
	}
	
	public String toString(){
		return this.name;
	}
}
