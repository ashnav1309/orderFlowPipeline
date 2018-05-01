package com.ashnav.pipeline.orderstate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import macros.ConstantLiterals;
import utilities.ConnectToGSheet;
import utilities.ConnectToMysql;
import utilities.ConnectToServer;

public class ExecuteQuery {

	private static Logger logger = LogManager.getLogger(ExecuteQuery.class.getName());

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
					sshConnection.startEnvironment();
					sshConnection.createSSHSession();
					if(sshConnection.isSessionConnected()) {
						mysqlConnection.createMYSQLConnection();
						if(mysqlConnection.isConnected()){
							mysqlConnection.executeSqlScript();
							mysqlConnection.destroyMySqlConnection();
						}
						sshConnection.destroyLPort();
						sshConnection.destroySSHSession();
						sshConnection.endEnvironment();
						break;
					}
					break;
				}
			}		
		} 
	}

	public static void main(String args[]) {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date(System.currentTimeMillis()));
		logger.trace("~~~~~~~~~~~~~~~STARTING-"+timeStamp+"~~~~~~~~~~~~~~~");
		ExecuteQuery.executeOnAllEvironments(ConstantLiterals.GSheetSpreadsheetId, ConstantLiterals.GSheetCellRange, ConstantLiterals.Localhost, ConstantLiterals.RPort, ConstantLiterals.SSHPort);
		timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date(System.currentTimeMillis()));
		logger.trace("~~~~~~~~~~~~~~~ENDING-"+timeStamp+"~~~~~~~~~~~~~~~");
	}
}

