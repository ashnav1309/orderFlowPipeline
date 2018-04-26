package com.ashnav.pipeline.orderstate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import macros.ConstantLiterals;
import utilities.ConnectToGSheet;
import utilities.ConnectToMysql;
import utilities.ConnectToServer;

public class ExecuteQuery {

	public static void executeOnAllEvironments(String spreadsheetId, String range, String MYSQLHost, int MYSQLPort, int SSHPort)  {

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
				sshConnections.add(new ConnectToServer(environment, SSHUser, SSHHost, SSHPassword, SSHPort, LPort, RHost, MYSQLPort));
				mysqlConnections.add(new ConnectToMysql(MYSQLHost, MYSQLUser, MYSQLPassword, LPort, fileName));
			}
		}

		for(ConnectToServer sshConnection : sshConnections) {
			for(ConnectToMysql mysqlConnection : mysqlConnections) {
				if(mysqlConnection.getLPort() == sshConnection.getLPort()) {
					if(sshConnection.createSSHSession()) {
						mysqlConnection.createMYSQLConnection();
						mysqlConnection.executeSqlScript();
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

	public static void main(String args[]) throws IOException {

		final String range 			= "ServersList!B:G";
		final String spreadsheetId 	= "1ppbqQhNDplcH906K3oLFeLYd2d1jfQizfdr02DqpgeQ";
		final  int 	 RPort 			= 3306;
		final  int 	 SSHPort 		= 22;
		String MYSQLHost 			= "127.0.0.1";

		ExecuteQuery.executeOnAllEvironments(spreadsheetId, range, MYSQLHost, RPort, SSHPort);
	}
}

