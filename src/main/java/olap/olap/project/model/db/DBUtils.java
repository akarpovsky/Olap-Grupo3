package olap.olap.project.model.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBUtils {

	/**
	 * Get the tables in the DB with the user provided connection
	 */
	public static List<DBTable> getTablesInDB(Connection conn) throws SQLException {
		if (conn == null) {
			throw new RuntimeException("Invalid or null connection provided.");
		}

		List<DBTable> ans = new ArrayList<DBTable>();
		
		// Gets the metadata of the database
		DatabaseMetaData dbmd = conn.getMetaData();

		// Get all the table names
		String[] types = { "TABLE" };
		ResultSet tables = dbmd.getTables(null, null, "%", types);
		while (tables.next()) {
			
			DBTable currentTable = new DBTable(tables.getString("TABLE_NAME"));
			
            // Get primary keys
            ResultSet tablePrimaryKeys = dbmd.getPrimaryKeys(null, null, tables.getString("TABLE_NAME"));

            List<String> currentTablePrimaryKeys = new ArrayList<String>();
            
            while (tablePrimaryKeys.next()) {
            	String columnPKName = tablePrimaryKeys.getString("COLUMN_NAME");
            	currentTablePrimaryKeys.add(columnPKName);
            }
            
            ResultSet tableData = dbmd.getColumns(null, null,
					tables.getString("TABLE_NAME"), null);
			
			// Iterate over columns for getting column data
			while (tableData.next()) {
				
				String name = tableData.getString("COLUMN_NAME");
				String type = tableData.getString("TYPE_NAME");
				int size = tableData.getInt("COLUMN_SIZE");

				currentTable.addColumn(new DBColumn(name, type, currentTablePrimaryKeys.contains(name)));
			}
			
			ans.add(currentTable);
		}
		
		for(DBTable t: ans){
			printTableData(t);
		}
		
		return ans;
	}
	
	/**
	 * Get specific table in the DB with the user provided connection
	 */
	public static DBTable getTableInDB(Connection conn, String tableName) throws SQLException {
		if (conn == null) {
			throw new RuntimeException("Invalid or null connection provided.");
		}

		// Gets the metadata of the database
		DatabaseMetaData dbmd = conn.getMetaData();

		// Get all the table names
		String[] types = { "TABLE" };
		ResultSet tables = dbmd.getTables(null, null, tableName, types);
		while (tables.next()) {
			
			DBTable currentTable = new DBTable(tables.getString("TABLE_NAME"));
			
            // Get primary keys
            ResultSet tablePrimaryKeys = dbmd.getPrimaryKeys(null, null, tables.getString("TABLE_NAME"));

            List<String> currentTablePrimaryKeys = new ArrayList<String>();
            
            while (tablePrimaryKeys.next()) {
            	String columnPKName = tablePrimaryKeys.getString("COLUMN_NAME");
            	currentTablePrimaryKeys.add(columnPKName);
            }
            
            ResultSet tableData = dbmd.getColumns(null, null,
					tables.getString("TABLE_NAME"), null);
			
			// Iterate over columns for getting column data
			while (tableData.next()) {
				
				String name = tableData.getString("COLUMN_NAME");
				String type = tableData.getString("TYPE_NAME");
				int size = tableData.getInt("COLUMN_SIZE");

				currentTable.addColumn(new DBColumn(name, type, currentTablePrimaryKeys.contains(name)));
			}
			
			return currentTable;
		}
		
		return null;
		
	}

	public static void printTableData(DBTable table){
		System.out.println("\nTABLE: " + table.getName());
		
		for(DBColumn c: table.getColumns()){
			System.out.println("COLUMN: " + c.getName() + " [" + c.getType() +"] " + (c.isPK()?"PRIMARY KEY":""));
		}
	}
}
