package com.yeeframework.automate.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.ConfigLoader;
import com.yeeframework.automate.annotation.PropertyValue;

/**
 * Used to connect database
 * 
 * @author ari.patriana
 *
 */
public class DBConnection {

	private static Logger log = LoggerFactory.getLogger(DBConnection.class);
	
	@PropertyValue(value = "simple.datasource.url")
	private String url = "jdbc:oracle:thin:@10.10.105.41:1521:fasdb";
	
	@PropertyValue(value = "simple.datasource.username")
	private String username = "EAEPME";
	
	@PropertyValue(value = "simple.datasource.password")
	private String password = "EAEPME";
	
	@PropertyValue(value = "simple.datasource.driverClassName")
	private String driverClassName = "oracle.jdbc.driver.OracleDriver";
	
	private static DBConnection dbConnection;
	
	private Connection connection;
	
	protected synchronized static DBConnection getConnection() {
		if (dbConnection == null) {
			dbConnection = new DBConnection();
			InjectionUtils.setObjectWithCustom(dbConnection, ConfigLoader.getConfigMap());	
		}
		return dbConnection;
	}
	
	protected Connection connect() {
		if (connection == null) {
			//step1 load the driver class  
			try {
				Class.forName(driverClassName);
			} catch (ClassNotFoundException e1) {
				log.error("ERROR ", e1);
			}  
			  
			//step2 create  the connection object  
			try {
				connection = java.sql.DriverManager.getConnection(url, username, password);
			} catch (SQLException e) {
				log.error("ERROR ", e);
			}
		}
		return connection;
	}
	
	protected static void executeUpdate(String query) {
		Statement stmt = null;
		try {
			stmt = getConnection().connect().createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			log.error("ERROR ", e);
		} finally {
			if (stmt != null) try {stmt.close();} catch (Exception e) {}
		}
	}
	
	protected void close() {
		if (dbConnection != null) {
			try {
				if (!dbConnection.connect().isClosed()) {
					dbConnection.connect().close();
				}
				dbConnection = null;
			} catch (SQLException e) {
				log.error("ERROR ", e);
			}
		}
	}
}
