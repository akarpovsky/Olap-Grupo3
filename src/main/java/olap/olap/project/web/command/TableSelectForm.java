package olap.olap.project.web.command;

import java.util.HashMap;
import java.util.Map;



public class TableSelectForm {

	private Map<String, String> tablesMap = new HashMap<String,String>();
	
	public Map<String, String> getTablesMap(){
		return tablesMap;
	}

	public void setTablesMap(Map<String,String> m){
		tablesMap = m;
	}
	
}
