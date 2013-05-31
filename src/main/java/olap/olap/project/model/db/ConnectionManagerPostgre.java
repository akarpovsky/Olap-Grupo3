package olap.olap.project.model.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;


public class ConnectionManagerPostgre implements ConnectionManager {

	private static ConnectionManagerPostgre connectionManager;
	private static String applicationPath;

	private String connectionString;
	private Driver driver = new org.postgresql.Driver();
	private String username;
	private String password;

	public static void setApplicationPath(final String applicationPath) {
		ConnectionManagerPostgre.applicationPath = applicationPath;
	}

	public static ConnectionManagerPostgre getConnectionManager() {
		if (connectionManager == null) {
			connectionManager = new ConnectionManagerPostgre(applicationPath);
		}
		return connectionManager;
	}

	private ConnectionManagerPostgre(final String applicationPath) {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (final ClassNotFoundException e) {
			return;
		}

		// this.connection = null;

		/*
		 * Reading data from properties file
		 */
		boolean error = false;
		final Properties props = new Properties();
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(applicationPath
					+ "/WEB-INF/database.properties");
			try {
				props.load(inputStream);
			} catch (final IOException e) {
				e.printStackTrace();
				error = true;
			}
		} catch (final FileNotFoundException e) {
			error = true;
		}

		if (!error) {
			this.username = props.getProperty("db.username");
			this.password = props.getProperty("db.password");
			this.connectionString = props.getProperty("db.connectionString");
		} else {
			System.err.println("Error loading the db from file");
			this.username = "postgres";
			this.password = "postgres";
			this.connectionString = "jdbc:postgresql://localhost:5432/postgres";
		}

	}

	public Connection getConnection() throws Exception {
		// if (this.connection == null || this.connection.isClosed()) {
		Connection connection;
		final Properties props = new Properties();
		props.setProperty("user", this.username);
		props.setProperty("password", this.password);
		connection = this.driver.connect(this.connectionString, props);
		// }
		return connection;
	}

	public void closeConnection(final Connection conn) throws SQLException {
		if (conn != null) {
			conn.close();
		}

	}

}