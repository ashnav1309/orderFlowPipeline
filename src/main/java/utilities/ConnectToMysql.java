package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Reporter;

public class ConnectToMysql {

	private static Logger logger = LogManager.getLogger(ConnectToMysql.class.getName());

	private Connection connection 	= null;
	private final String MYSQLUser;
	private final String MYSQLPassword;
	private final String fileName;
	private final String url;

	public ConnectToMysql(String MYSQLHost, String MYSQLUser, String MYSQLPassword, String LPort, String fileName) {
		url 				  	= "jdbc:mysql://"+MYSQLHost+":"+LPort;
		this.MYSQLUser 			= MYSQLUser;
		this.MYSQLPassword 		= MYSQLPassword;
		this.fileName 			= fileName;
	}

	public void createMYSQLConnection() {
		Properties properties = new Properties();
		properties.setProperty("user", MYSQLUser);
		properties.setProperty("password", MYSQLPassword);
		properties.setProperty("useSSL", "false");
		properties.setProperty("autoReconnect", "false");
		try {
			connection = DriverManager.getConnection(url, properties);
			logger.info("JDBC Connection to "+url+" established? "+ isConnected());
		} 
		catch (SQLException e) {
			Reporter.log(e.getMessage());
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
				Reporter.log(e.getMessage());
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
				Reporter.log(e.getMessage());
				logger.error(e);
				return;
			}
		}
	}

	@SuppressWarnings("resource") //resources are closed safely in finally block
	public Boolean executeSqlScript() {
		Boolean condition = false;
		String filePath = fileName;
		File file = new File(filePath);
		String delimiter = ";";
		Scanner scanner;
		try {
			scanner = new Scanner(file).useDelimiter(delimiter);
			logger.info("Executing scripts from "+fileName);
		} 
		catch (FileNotFoundException e1) {
			Reporter.log("Invalid file name or location.");
			logger.error("Invalid file name or location. ", e1);
			return false;
		}
		Statement currentStatement = null;
		while(scanner.hasNext()) {
			String rawStatement = scanner.next();
			rawStatement 		= rawStatement.replaceAll("\n", " ");
			rawStatement 		= rawStatement.replaceAll("( )+", " ");
			rawStatement 		= rawStatement.trim();
			rawStatement 		= rawStatement + delimiter;
			logger.trace("++++++++++START-QUERY++++++++++");
			if(rawStatement.isEmpty() || rawStatement.startsWith(";") || rawStatement.equals(" ")){
				//				logger.trace("Empty Query");
				//				logger.trace("++++++++++END-QUERY++++++++++++");
				continue;
			}
			try {
				logger.trace(rawStatement);
				currentStatement = connection.createStatement();
				currentStatement.execute(rawStatement);
				condition = true;
				Reporter.log(currentStatement.getUpdateCount()+" row(s) updated");
				logger.info(currentStatement.getUpdateCount()+" row(s) updated");
			} 
			catch (SQLException e) {
				Reporter.log(e.getMessage());
				logger.error(e);
			} 
			catch (NullPointerException e) {
				Reporter.log(e.getMessage());
				logger.error(e);
			}
			finally {
				logger.trace("++++++++++END-QUERY++++++++++++");
				if (currentStatement != null) {
					try {
						currentStatement.close();
					} 
					catch (SQLException e) {
						Reporter.log(e.getMessage());
						logger.error(e);
					}
				}
				currentStatement = null;
			}
		}
		scanner.close();
		return condition;
	}
}


