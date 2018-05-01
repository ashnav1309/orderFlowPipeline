package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import macros.ConstantLiterals;

public class ConnectToMysql {

	private static Logger logger = LogManager.getLogger(ConnectToMysql.class.getName());

	private Connection connection 	= null;
	private String MYSQLHost 		= null;
	private String MYSQLUser 		= null;
	private String MYSQLPassword 	= null;
	private String fileName 		= null;
	String url 						= null;
	private int LPort 				= 0;

	public ConnectToMysql(String MYSQLHost, String MYSQLUser, String MYSQLPassword, int LPort, String fileName) {
		this.MYSQLHost 			= MYSQLHost;
		this.MYSQLUser 			= MYSQLUser;
		this.MYSQLPassword 		= MYSQLPassword;
		this.LPort 				= LPort;
		this.fileName 			= fileName;
	}

	public int getLPort() {
		return LPort;
	}

	public void createMYSQLConnection() {
		url 				  = "jdbc:mysql://"+MYSQLHost+":"+LPort;
		Properties properties = new Properties();
		properties.setProperty("user", MYSQLUser);
		properties.setProperty("password", MYSQLPassword);
		properties.setProperty("useSSL", "false");
		properties.setProperty("autoReconnect", "true");
		try {
			connection = DriverManager.getConnection(url, properties);
			logger.info("JDBC Connection to "+url+" established? "+ isConnected());
		} 
		catch (SQLException e) {
			logger.error(e);
		}
	}

	public Boolean isConnected() {
		Boolean connected = false;
		if(connection != null) {
			try {
				if(!connection.isClosed()) {
					connected = true;
				}
			} 
			catch (SQLException e) {
				logger.error(e);
				return false;
			}
		}
		return connected;
	}

	public void destroyMySqlConnection() {
		if(connection != null) {
			try {
				connection.close();
				logger.info("JDBC Connection to "+url+" destroyed? {}", !isConnected());
			} 
			catch (SQLException e) {
				logger.error(e);
				return;
			}
		}
	}

	@SuppressWarnings("resource") //resources are closed safely in finally block
	public void executeSqlScript() {
		String filePath = ConstantLiterals.ResourcesPath+fileName;
		File file = new File(filePath);
		String delimiter = ";";
		Scanner scanner;
		try {
			scanner = new Scanner(file).useDelimiter(delimiter);
			logger.info("Executing scripts from "+fileName);
		} 
		catch (FileNotFoundException e1) {
			logger.error("Invalid file name or location. ", e1);
			return;
		}
		Statement currentStatement = null;
		while(scanner.hasNext()) {
			String rawStatement = scanner.next();
			rawStatement = rawStatement.replaceAll("\n", " ");
			rawStatement = rawStatement.replaceAll("( )+", " ");
			rawStatement = rawStatement.trim();
			rawStatement = rawStatement + delimiter;
			logger.trace("++++++++++START-QUERY++++++++++");
			if(rawStatement.isEmpty() || rawStatement.startsWith(";") || rawStatement.equals(" ")){
				logger.trace("Empty Query");
				logger.trace("++++++++++END-QUERY++++++++++++");
				continue;
			}
			try {
				logger.trace(rawStatement);
				logger.trace("++++++++++END-QUERY++++++++++++");
				currentStatement = connection.createStatement();
				currentStatement.execute(rawStatement);
				logger.info(currentStatement.getUpdateCount()+" row(s) updated");
			} 
			catch (SQLException e) {
				logger.error(e);
			} 
			catch (NullPointerException e) {
				logger.error(e);
			}
			finally {
				if (currentStatement != null) {
					try {
						currentStatement.close();
					} 
					catch (SQLException e) {
						logger.error(e);
					}
				}
				currentStatement = null;
			}
		}
		scanner.close();
	}

	@SuppressWarnings("resource")
	public void executeSqlScript(String value, String... columnNames) {
		String filePath = "src/main/resources/"+fileName;
		ResultSet rs = null;
		File file = new File(filePath);
		String delimiter = ";";
		Scanner scanner = null;
		try {
			scanner = new Scanner(file).useDelimiter(delimiter);
		} 
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Statement currentStatement = null;
		while(scanner.hasNext()) {
			String rawStatement = scanner.next();
			rawStatement = rawStatement.replaceAll("\n", " ");
			rawStatement = rawStatement.replaceAll("( )+", " ");
			rawStatement = rawStatement.trim();
			rawStatement = rawStatement + delimiter;
			rawStatement = rawStatement.replaceAll("####", value);
			try {
				logger.info("++++++++++++++++++++++++++++++QUERY-START++++++++++++++++++++++++++++++");
				logger.info(rawStatement);
				logger.info("+++++++++++++++++++++++++++++++QUERY-END+++++++++++++++++++++++++++++++");
				currentStatement = connection.createStatement();
				rs = currentStatement.executeQuery(rawStatement);
				int count = 0;
				while(rs.next()) {
					count++;
					StringBuilder data = new StringBuilder();
					for(String columnName : columnNames) {
						data.append(rs.getString(columnName));
						data.append(", ");
					}
					data.deleteCharAt(data.lastIndexOf(", "));
					String row = data.toString().trim();
					logger.info(row);
				}
				logger.info(count+" row(s) returned");

			} 
			catch (SQLException e) {
				logger.error(e);
				return;
			} 
			catch (ArrayIndexOutOfBoundsException e) {
				logger.error(e);
				return;
			}
			finally {
				if (currentStatement != null) {
					try {
						currentStatement.close();
					} 
					catch (SQLException e) {
						logger.error(e);
						return;
					}
				}
				currentStatement = null;
			}
		}
		scanner.close();
	}

	//	public static Boolean getWatchDogOn(int pollAfter, int timeOutAfter) {
	//
	//		int count = 0;
	//		Boolean isTrue = false;
	//
	//		try {
	//			do{
	//				if(false) {
	//
	//					isTrue = true;
	//					break;	
	//
	//				}
	//				logger.info("Sleeping for %d minutes.\n", pollAfter);
	//				Thread.sleep(pollAfter*1000);
	//				count = count+pollAfter;
	//			}while(!isTrue && timeOutAfter>count);
	//			throw new InterruptedByTimeoutException();
	//		}
	//		catch(InterruptedByTimeoutException e) {
	//
	//			e.printStackTrace();
	//			isTrue = false;
	//			
	//		}
	//		catch(InterruptedException e) {
	//
	//			e.printStackTrace();
	//			isTrue = false;
	//			
	//		}
	//		return isTrue;
	//	}
}


