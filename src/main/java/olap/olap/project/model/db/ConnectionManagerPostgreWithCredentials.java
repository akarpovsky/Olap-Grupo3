package olap.olap.project.model.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManagerPostgreWithCredentials implements
		ConnectionManager {

	private static ConnectionManagerPostgreWithCredentials connectionManager;

	private String connectionString;
	private Driver driver = new org.postgresql.Driver();
	private String username;
	private String password;

	public static ConnectionManagerPostgreWithCredentials getConnectionManagerWithCredentials(
			String url, String user, String password) {
		if (connectionManager == null) {
			connectionManager = new ConnectionManagerPostgreWithCredentials(
					url, user, password);
		}
		return connectionManager;
	}
	
	public static ConnectionManagerPostgreWithCredentials getConnectionManagerWithCredentials() {
		return connectionManager;
	}
	
	
	public static ConnectionManagerPostgreWithCredentials setConnectionManagerWithCredentials(
			String url, String user, String password) {
		connectionManager = new ConnectionManagerPostgreWithCredentials(
					url, user, password);
		return connectionManager;
	}

	private ConnectionManagerPostgreWithCredentials(String url, String user,
			String password) {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (final ClassNotFoundException e) {
			return;
		}

		this.username = user;
		this.password = password;
		this.connectionString = "jdbc:postgresql://" + url;

	}

	public Connection getConnectionWithCredentials() throws SQLException  {
		Connection connection;
		final Properties props = new Properties();
		props.setProperty("user", this.username);
		props.setProperty("password", this.password);
		connection = this.driver.connect(this.connectionString, props);
		return connection;
	}

	public void closeConnection(final Connection conn) throws SQLException {
		if (conn != null) {
			conn.close();
		}

	}

	
	public String getConnectionString() {
		return this.connectionString;
	}

}