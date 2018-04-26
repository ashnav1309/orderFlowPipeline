package com.ashnav.pipeline.orderstate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import macros.ConstantLiterals;
import utilities.ConnectToCSV;
import utilities.ConnectToGSheet;
import utilities.ConnectToMysql;
import utilities.ConnectToServer;

public class LocalFitting_Test {

	private static Logger logger = LogManager.getLogger(LocalFitting_Test.class);
	
	public static String getDataUsingGSheet(String spreadsheetId, String range) throws IOException {
			List<List<Object>> values = ConnectToGSheet.getCellValues(spreadsheetId, range, ConstantLiterals.MajorDimension_Row);
		StringBuilder data = new StringBuilder();
		Iterator<List<Object>> iterator = values.iterator();
		while(iterator.hasNext()) { 
			String value = iterator.next().get(0).toString();
			data.append(value);
			data.append(", ");
		}
		int lastcomma = data.lastIndexOf(",");
		data.deleteCharAt(lastcomma);
		logger.info("getDataUsingGSheet method");
		return data.toString();
	}

	public static String getDataUsingCSV(String fileName, String columnName) {
		List<Object> values = ConnectToCSV.removeDuplicateRows(ConnectToCSV.readCSV(fileName, columnName));
		StringBuilder data = new StringBuilder();
		Iterator<Object> iterator = values.iterator();
		while(iterator.hasNext()) { 
			String value = iterator.next().toString();
			data.append(value);
			data.append(", ");
		}
		int lastcomma = data.lastIndexOf(",");
		data.deleteCharAt(lastcomma);
		return data.toString();
	}

	public static void main(String args[]) throws IOException, SQLException {
		logger.info("Hello3");
		final String range 			= "ServersList!B:G";
		final String spreadsheetId 	= "1ppbqQhNDplcH906K3oLFeLYd2d1jfQizfdr02DqpgeQ";
		final  int 	 RPort 			= 3306;
		final  int 	 SSHPort 		= 22;
		String MYSQLHost 			= "127.0.0.1";
		List<List<Object>> values = ConnectToGSheet.getCellValues(spreadsheetId, range, ConstantLiterals.MajorDimension_Column);
		List<ConnectToServer> sshConnections = new ArrayList<>();
		List<ConnectToMysql> mysqlConnections = new ArrayList<>();

		for(List<Object> value : values) {
			if(value.size() > 1){ //Size is greater than 1 because top row will always be present
				String environment		= value.get(0).toString();
				String RHost 			= value.get(1).toString();
				String MYSQLUser 		= value.get(2).toString();
				String MYSQLPassword 	= value.get(3).toString();
				String SSHHost 			= value.get(4).toString();
				String SSHUser 			= value.get(5).toString();
				String SSHPassword 		= value.get(6).toString();
				String fileName			= value.get(8).toString();
				int LPort				= Integer.parseInt(value.get(7).toString());
				sshConnections.add(new ConnectToServer(environment, SSHUser, SSHHost, SSHPassword, SSHPort, LPort, RHost, RPort));
				mysqlConnections.add(new ConnectToMysql(MYSQLHost, MYSQLUser, MYSQLPassword, LPort, fileName));
			}
		}
		
		for(ConnectToServer sshConnection : sshConnections) {
			for(ConnectToMysql mysqlConnection : mysqlConnections) {
				if(mysqlConnection.getLPort() == sshConnection.getLPort()) {
					if(sshConnection.createSSHSession()) {
						mysqlConnection.createMYSQLConnection();
						// mysqlConnection.executeSqlScript("", "Field", "Type");
						System.out.printf("========================================================================\n");
						mysqlConnection.destroyMySqlConnection();
						sshConnection.destroyLPort();
						sshConnection.destroySSHSession();
						System.out.printf("========================================================================\n\n");
						break;
					}
					break;
				}
			}		
		} 
	}
}

