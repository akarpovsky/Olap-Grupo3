package olap.olap.project.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TableCreator {

	
	private Connection conn;
	
	public TableCreator(Connection conn){
		this.conn = conn;
	}
	
	public void createTables(String tables) throws SQLException{
		PreparedStatement statement = this.conn.prepareStatement(tables);
		statement.execute();
	}
	
	public TableCreator(String url, String user, String password) throws Exception{
		
			PreparedStatement statement;
			final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
					.setConnectionManagerWithCredentials(url, user, password);
			final Connection conn = connectionManager.getConnectionWithCredentials();
			System.out.println(connectionManager.toString());
			connectionManager.closeConnection(conn);
	}
}
