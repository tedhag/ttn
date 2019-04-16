package se.progatec.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DbConnection {

	final static Logger logger = Logger.getLogger(DbConnection.class.getName());

	private static HikariDataSource ds;
	
	private static DbConnection dbconn;
	
	private DbConnection(){}
	
	public static DbConnection instance() {
		if (ds==null) {
			dbconn = new DbConnection();
			dbconn.init();
		};
		
		return dbconn;
	}
	
	private void init() {
		try {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:mysql://progatec.myftp.org:3306/ttn?serverTimezone=Europe/Stockholm");
			config.setUsername("ttn");
			config.setPassword("herrebro");
			config.setMaximumPoolSize(10);
			config.setMinimumIdle(4);
			config.setIdleTimeout(120000);

			ds = new HikariDataSource(config);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "failed to set DB connection properties", e);
		}
	}

	public Connection connection() throws SQLException {
		return ds.getConnection();
	}

	public void shutdown() {
		if (ds!=null)
			ds.close();
		
	}


}
