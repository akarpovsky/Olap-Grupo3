package olap.olap.project.model.db;


public class DBColumn {
	
	private String name;
	private String oldName;
	private String type;
	private boolean isPK;

	
	public DBColumn(String name, String type, boolean isPK){
		this.name = name;
		this.type = type;
		this.isPK = isPK;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getType(){
		return this.type;
	}

	public boolean isPK(){
		return this.isPK;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}
}
