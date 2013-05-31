package olap.olap.project.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class TableCreator {

	
	
	public TableCreator(){
		
		try {
			PreparedStatement statement;
			final ConnectionManager connectionManager = ConnectionManagerPostgre
					.getConnectionManager();
			final Connection conn = connectionManager.getConnection();
			System.out.println(connectionManager.toString());
//			if (notif.isNew()) {
//				statement = conn
//						.prepareStatement(
//								"INSERT INTO NOTIFICATIONS("
//										+ "user_from, user_to, read, type) VALUES (?, ?, ?, ?)",
//								PreparedStatement.RETURN_GENERATED_KEYS);
//				statement.setInt(1, notif.getFrom().getId());
//				statement.setInt(2, notif.getTo().getId());
//				statement.setBoolean(3, notif.isRead());
//				statement.setString(4, notif.getType().toString());
//				statement.execute();
//				final ResultSet set = statement.getGeneratedKeys();
//				set.next();
//				notif.setId(set.getInt("id"));
//			} else {
//				statement = conn.prepareStatement("UPDATE NOTIFICATIONS "
//						+ "SET user_from = ?, user_to = ?, "
//						+ "read = ?, type = ? " + "WHERE id = ?");
//				statement.setInt(1, notif.getFrom().getId());
//				statement.setInt(2, notif.getTo().getId());
//				statement.setBoolean(3, notif.isRead());
//				statement.setString(4, notif.getType().toString());
//				statement.setInt(5, notif.getId());
//				statement.execute();
//			}
			connectionManager.closeConnection(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
