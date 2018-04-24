package com.agfa.med.EIPS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class PGdbconnector{
	/**
	 * 
	 */
	public Connection amt_con;
	public String LAST_TEST_RESULT;
	public String[] STORAGE_ID,ARCHIVES;
	final static Logger logger = Logger.getLogger(PGdbconnector.class);
	
	public PGdbconnector () {
		STORAGE_ID = new String[0];
		ARCHIVES=new String[0];
	try {
		amt_con = this.createDBcon();
		} catch (SQLException e) {
		logger.fatal("Error creating amt db connectiong with string: "+MainWindow.db, e);
		}
	}

	public Connection createDBcon() throws SQLException {
		Connection connection = null;
		connection= DriverManager.getConnection(MainWindow.db);
		return connection;
	}
	
	public void CreateSchema() {
	try {
		logger.debug("Creating EI_API database schema.");
		Statement amt_stmt = amt_con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		logger.debug("CREATE SCHEMA if not exists EI_API");
		amt_stmt.executeUpdate("CREATE SCHEMA if not exists EI_API");
		logger.debug("CREATE TABLE if not exists EI_API.CONFIG (FQDN VARCHAR,USERNAME VARCHAR,PASSWORD VARCHAR, LAST_TEST_RESULT VARCHAR)");
		amt_stmt.executeUpdate("CREATE TABLE if not exists EI_API.CONFIG (FQDN VARCHAR,USERNAME VARCHAR,PASSWORD VARCHAR, LAST_TEST_RESULT VARCHAR)");
		logger.debug("Checking for existing database entries for credentials.");
		ResultSet entries = amt_stmt.executeQuery("select FQDN,USERNAME,PASSWORD,LAST_TEST_RESULT from ei_api.config") ;
		if(entries.next()) {
			logger.debug("Using existing credentials from database.");
			entries.first();
			MainWindow.FQDN=entries.getString(1);
			MainWindow.username=entries.getString(2);
			MainWindow.password=entries.getString(3);
			LAST_TEST_RESULT=entries.getString(4);
			entries.close();
		} else {
			logger.debug("No credentials exist in database. Populate default values.");
			amt_stmt.executeUpdate("INSERT INTO EI_API.CONFIG (FQDN,USERNAME,PASSWORD,LAST_TEST_RESULT) VALUES ('ENTER LB FQDN OR IP','GRACE','Service.1','N/A')");
			entries.close();
			MainWindow.FQDN="ENTER LB FQDN";
			MainWindow.username="GRACE";
			MainWindow.password="Service.1";
			LAST_TEST_RESULT="N/A";
		}
		logger.debug("CREATE TABLE if not exists EI_API.EI_STORAGE (TYPE VARCHAR,NAME VARCHAR,POOL_ID VARCHAR, LAST_FS FLOAT)");
		amt_stmt.executeUpdate("CREATE TABLE if not exists EI_API.EI_STORAGE (TYPE VARCHAR,NAME VARCHAR,POOL_ID VARCHAR, LAST_FS FLOAT)");
		logger.debug("Checking for existing database entries for storage locations.");
		entries = amt_stmt.executeQuery("select TYPE||\', \'||NAME FROM EI_API.EI_STORAGE");
		if(entries.next()) {
			logger.debug("Using existing storage locations from database.");
			entries.last();
			STORAGE_ID = new String[entries.getRow()];
			entries.beforeFirst();
			int i = 0;
			while(entries.next()) {
				STORAGE_ID[i]=entries.getString(1);
				i++;
			}
		}
		entries.close();
		logger.debug("CREATE TABLE if not exists EI_API.EI_ARCHIVES (NAME VARCHAR,QID VARCHAR)");
		amt_stmt.executeUpdate("CREATE TABLE if not exists EI_API.EI_ARCHIVES (NAME VARCHAR, QID VARCHAR)");
		logger.debug("Checking for existing database entries for archives.");
		entries = amt_stmt.executeQuery("select NAME FROM EI_API.EI_ARCHIVES");
		if(entries.next()) {
			logger.debug("Using existing archives from database.");
			entries.last();
			ARCHIVES = new String[entries.getRow()];
			entries.beforeFirst();
			int i = 0;
			while(entries.next()) {
				ARCHIVES[i]=entries.getString(1);
				i++;
			}
		}
		entries.close();
		logger.debug("CREATE TABLE if not exists EI_API.STUDY_DELETE_UIDS (ID SERIAL PRIMARY KEY,UID VARCHAR,STATUS VARCHAR DEFAULT 'READY',VALIDATED VARCHAR DEFAULT 'NOT VALIDATED')");
		amt_stmt.executeUpdate("CREATE TABLE if not exists EI_API.STUDY_DELETE_UIDS (ID SERIAL PRIMARY KEY,UID VARCHAR,STATUS VARCHAR DEFAULT \'READY\',VALIDATED VARCHAR DEFAULT \'NOT VALIDATED\')");
		logger.debug("CREATE TABLE if not exists EI_API.STUDY_MOVE_UIDS (ID SERIAL PRIMARY KEY,UID VARCHAR,STATUS VARCHAR DEFAULT 'READY', VALIDATED VARCHAR DEFAULT 'NOT VALIDATED')");
		amt_stmt.executeUpdate("CREATE TABLE if not exists EI_API.STUDY_MOVE_UIDS (ID SERIAL PRIMARY KEY,UID VARCHAR,STATUS VARCHAR DEFAULT \'READY\',VALIDATED VARCHAR DEFAULT \'NOT VALIDATED\')");
		amt_stmt.close();
		} catch (Exception e) {
		logger.fatal("Error when creating database schema: ",e);
		return;
		}
	}
	
	public void StoreCredentials() throws SQLException {
		Statement amt_stmt = amt_con.createStatement();
		String sqls = "UPDATE EI_API.CONFIG SET FQDN='"+MainWindow.FQDN+"'";
		amt_stmt.executeUpdate(sqls);
		sqls = "UPDATE EI_API.CONFIG SET USERNAME='"+MainWindow.username+"'";
		amt_stmt.executeUpdate(sqls);
		sqls = "UPDATE EI_API.CONFIG SET PASSWORD='"+MainWindow.password+"'";
		amt_stmt.executeUpdate(sqls);
		amt_stmt.close();
	}
	
	public void closeShop() throws Exception {
		amt_con.close();
	}

	
}
