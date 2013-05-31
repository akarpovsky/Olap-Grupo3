package olap.olap.project.model.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionManager {

	public Connection getConnection() throws Exception;

	public void closeConnection(Connection conn) throws SQLException;

}
