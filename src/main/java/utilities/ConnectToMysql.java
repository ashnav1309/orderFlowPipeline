package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

public class ConnectToMysql {

	private Connection connection 	= null;
	private String MYSQLHost 		= null;
	private String MYSQLUser 		= null;
	private String MYSQLPassword 	= null;
	private String fileName 		= null;
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

		String url = "jdbc:mysql://"+MYSQLHost+":"+LPort;
		Properties properties = new Properties();
		properties.setProperty("user", MYSQLUser);
		properties.setProperty("password", MYSQLPassword);
		properties.setProperty("useSSL", "false");
		properties.setProperty("autoReconnect", "true");
		try {
			connection = DriverManager.getConnection(url, properties);
			System.out.printf("========================================================================\n");
			System.out.printf("JDBC Connection to Database host %s established? "+ !connection.isClosed()+"\n", url);
			System.out.printf("========================================================================\n\n");
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void destroyMySqlConnection() {
		if(connection != null) {
			try {
				connection.close();
				System.out.println("Database Connection destroyed");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("resource") //resources are closed safely in finally block
	public void executeSqlScript() {
		String filePath = "src/main/resources/"+fileName;
		File file = new File(filePath);
		String delimiter = ";";
		Scanner scanner;
		try {
			scanner = new Scanner(file).useDelimiter(delimiter);
		} 
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
		Statement currentStatement = null;
		while(scanner.hasNext()) {
			String rawStatement = scanner.next() + delimiter;
			if(!rawStatement.contains("\n;")) {
				try {
					System.out.println("++++++++++++++++++++++++++++++QUERY-START++++++++++++++++++++++++++++++");
					System.out.println(rawStatement);
					System.out.println("+++++++++++++++++++++++++++++++QUERY-END+++++++++++++++++++++++++++++++");
					currentStatement = connection.createStatement();
					currentStatement.execute(rawStatement);
					System.out.printf("%d row(s) fetched\n",currentStatement.getResultSet().getFetchSize());
					System.out.printf("%d row(s) updated\n",currentStatement.getUpdateCount());
				} 
				catch (SQLException e) {
					e.printStackTrace();
				} 
				finally {
					if (currentStatement != null) {
						try {
							currentStatement.close();
						} 
						catch (SQLException e) {
							e.printStackTrace();
						}
					}
					currentStatement = null;
				}
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
	//				System.out.printf("Sleeping for %d minutes.\n", pollAfter);
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


