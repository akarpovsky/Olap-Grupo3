package olap.olap.project.model.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionManager {

	public Connection getConnectionWithCredentials() throws SQLException;

	public void closeConnection(Connection conn) throws SQLException;

	public String getConnectionString();

}
