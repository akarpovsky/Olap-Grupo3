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
	
	public String getNameWithTypeAndPK(){
		if(isPK()){
			return this.name + " ["+ getType() +"] " + " (PK)"; 
		}
		return this.name + " ["+ getType() +"] ";
	}
	
	
	public String getType(){
		return this.type;
	}
	public void setType(String type){
		this.type = type;
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
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		return ((DBColumn)obj).getName().equals(getName());
	}
	
	public void update(String newName){
		oldName = name;
		name = newName;
	}
}
