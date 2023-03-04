package com.yeeframework.automate.util;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.exception.ReachTimeoutException;

/**
 * Used to query database
 * 
 * @author ari.patriana
 *
 */
public class DBQuery {

	private static Logger log = LoggerFactory.getLogger(DBQuery.class);
	
	private static DBConnection dbConnection;
	
	private static DBConnection getConnection() {
		dbConnection = DBConnection.getConnection();
		return dbConnection;
	}
	
	public static void executeUpdate(String query) {
		Statement stmt = null;
		try {
			stmt = getConnection().connect().createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			log.error("ERROR ", e);
		} finally {
			closeStatement(stmt, null);
		}
	}
	
	public static <T> T selectOneQuery(String query, Class<T> type) {
		log.info("Select query -> " + query);
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getConnection().connect().createStatement();
			rs=stmt.executeQuery(query);
			if (rs.next()) {
				return convertType(rs, type, 0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.error("ERROR ", e);
		} finally {
			closeStatement(stmt, rs);
		}
		return null;
	}
	
	public static <T> List<Map<String, Object>> selectQuery(String simpleQuery, Class<T>[] types) throws SQLException {
		log.info("Select query -> " + simpleQuery);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getConnection().connect().createStatement();
			rs=stmt.executeQuery(simpleQuery);
			ResultSetMetaData rsMetaData = rs.getMetaData();
			while(rs.next()) {  
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i=0; i<types.length; i++) {
					row.put(rsMetaData.getColumnName(i), convertType(rs, types[i], i));					
				}
				results.add(row);
			}
		} catch (SQLException e) {
			log.error("ERROR ", e);
			throw e;
		} finally {
			closeStatement(stmt, rs);
		}
		return results;
	}
	
	public static List<Object[]> selectQuery(String simpleQuery, String[] columns) throws SQLException {
		log.info("Select query -> " + simpleQuery);
		List<Object[]> results = new ArrayList<Object[]>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getConnection().connect().createStatement();
			rs=stmt.executeQuery(simpleQuery);  
			while(rs.next()) {  
				Object[] result = new Object[columns.length];
				for (int i=0; i<columns.length; i++) {
					result[i] = convertType(rs, Object.class, columns[i]);	
				}
				results.add(result);
			}
		} catch (SQLException e) {
			log.error("ERROR ", e);
			throw e;
		} finally {
			closeStatement(stmt, rs);
		}
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T[]> selectQuery(String simpleQuery, String[] columns, Class<T> clazz) throws SQLException {
		log.info("Select query -> " + simpleQuery);
		List<T[]> results = new ArrayList<T[]>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getConnection().connect().createStatement();
			rs=stmt.executeQuery(simpleQuery);  
			while(rs.next()) {  
				T[] result = (T[]) Array.newInstance(clazz, columns.length);
				for (int i=0; i<columns.length; i++) {
					result[i] = convertType(rs, clazz, columns[i]);	
				}
				results.add(result);
			}
		} catch (SQLException e) {
			log.error("ERROR ", e);
			throw e;
		} finally {
			closeStatement(stmt, rs);
		}
		return results;
	}
	
	
	public static <T> List<Map<String, Object>> selectQueryAndWait(String simpleQuery, Class<T>[] types, int timeout) throws ReachTimeoutException, SQLException {
		List<Map<String, Object>> resultList = selectQuery(simpleQuery, types);
		long start = System.currentTimeMillis();
		while(resultList == null || resultList.isEmpty()) {
			resultList = selectQuery(simpleQuery, types);
			
			long diff = System.currentTimeMillis() - start;
			if (diff > (timeout * 1000)) {
				throw new ReachTimeoutException("Reach time out after " + timeout + " seconds for " + simpleQuery);
			}
			Sleep.wait(1000);
		}
		return resultList;
	}
	
	public static <T> T selectOneQueryAndWait(String simpleQuery, Class<T> type, int timeout) throws ReachTimeoutException, SQLException {
		T result = selectOneQuery(simpleQuery, type);
		long start = System.currentTimeMillis();
		while(result == null) {
			result = selectOneQuery(simpleQuery, type);
			
			long diff = System.currentTimeMillis() - start;
			if (diff > (timeout * 1000)) {
				throw new ReachTimeoutException("Reach time out after " + timeout + " seconds for " + simpleQuery);
			}
			Sleep.wait(1000);
		}
		return result;
	}
	
	private static <T> T convertType(ResultSet rs, Class<T> type, int index) throws SQLException {
		if (type.isInstance(Integer.class)) {
			return type.cast(rs.getInt(index));
		} else if (type.isInstance(String.class)) {
			return type.cast(rs.getString(index));
		} else if (type.isInstance(Long.class)) {
			return type.cast(rs.getLong(index));
		} else if (type.isInstance(Date.class)) {
			return type.cast(rs.getDate(index));
		} else if (type.isInstance(BigDecimal.class)) {
			return type.cast(rs.getBigDecimal(index));
		}
		return null;
	}
	
	private static <T> T convertType(ResultSet rs, Class<T> type, String name) throws SQLException {
		if (type.isInstance(Integer.class)) {
			return type.cast(rs.getInt(name));
		} else if (type.isInstance(String.class)) {
			return type.cast(rs.getString(name));
		} else if (type.isInstance(Long.class)) {
			return type.cast(rs.getLong(name));
		} else if (type.isInstance(Date.class)) {
			return type.cast(rs.getDate(name));
		} else if (type.isInstance(BigDecimal.class)) {
			return type.cast(rs.getBigDecimal(name));
		}
		return null;
	}
		
	private static void closeStatement(Statement stmt, ResultSet rs) {
		if (stmt != null) try {stmt.close();} catch (Exception e) {}
		if (rs != null) try {rs.close();} catch (Exception e) {}
	}
	
	public static void close() {
		if (dbConnection != null) {
			try {
				if (!getConnection().connect().isClosed()) {
					getConnection().connect().close();
					dbConnection = null;
				}
			} catch (SQLException e) {
				log.error("ERROR ", e);
			}
		}
	}
}
