package olap.olap.project.model.db;

import java.util.ArrayList;
import java.util.List;


public class DBTable {
	
	private String name;
	private String oldName;
	private List<DBColumn> columns;
	private boolean isFactTable;
	
	public DBTable(String name){
		this.name = name;
		this.columns = new ArrayList<DBColumn>();
		this.setFactTable(false);
	}
	
	public DBTable(String name, boolean isFactTable){
		this.name = name;
		this.columns = new ArrayList<DBColumn>();
		this.setFactTable(isFactTable);
	}
	
	public String getName(){
		return this.name;
	}

	public List<DBColumn> getColumns(){
		return this.columns;
	}
	
	public void addColumn(DBColumn column){
		this.columns.add(column);
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}
	public void update(String newName){
		this.oldName=name;
		this.name=newName;
	}

	public boolean isFactTable() {
		return isFactTable;
	}

	public void setFactTable(boolean isFactTable) {
		this.isFactTable = isFactTable;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		return ((DBTable)obj).getName().equals(getName());
	}

	public void updateColumn(String original, String newValue) {
		for(DBColumn c: getColumns()){
			if(c.getName().equals(original)){
				c.update(newValue);
			}
		}
	}
}
