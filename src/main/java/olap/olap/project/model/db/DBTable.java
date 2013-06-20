package olap.olap.project.model.db;

import java.util.ArrayList;
import java.util.List;


public class DBTable {
	
	private String name;
	private List<DBColumn> columns;
	
	public DBTable(String name){
		this.name = name;
		this.columns = new ArrayList<DBColumn>();
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
}
