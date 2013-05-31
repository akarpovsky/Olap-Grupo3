package olap.olap.project.db;

import java.sql.Connection;

import junit.framework.Assert;
import olap.olap.project.model.db.ConnectionManagerPostgre;

public class ConnectionManagerPostgreTest {

	private ConnectionManagerPostgre manager;

	@Before
	public void initTest() {
		this.manager = ConnectionManagerPostgre.getConnectionManager();

	}

	@Test
	public void getConnectionTest() {

		boolean thrown = false;
		try {
			final Connection conn = this.manager.getConnection();
			conn.close();
		} catch (final Exception e) {
			thrown = true;
			e.printStackTrace();
		}
		Assert.assertFalse(thrown);

	}
}
