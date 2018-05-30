package com.ashnav.pipeline.orderstate;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import junit.framework.Assert;
import macros.ConstantLiterals;
import utilities.ConnectToGSheet;
import utilities.ConnectToMysql;
import utilities.ConnectToServer;

public class ExecuteQuerywithMultipleThreads2 {

	private static Logger logger 							= LogManager.getLogger(ExecuteQuerywithMultipleThreads2.class.getName());
	private static ArrayList<Object[]> environmentData 		= 	new ArrayList<>();

	@DataProvider(name = "fetchEnvironmentsData", parallel = true)
	public static Iterator<Object[]> supplyData_input() throws InterruptedException  {
		List<List<Object>> values 	= ConnectToGSheet.getCellValues(ConstantLiterals.GSheetSpreadsheetId, ConstantLiterals.GSheetCellRange, ConstantLiterals.MajorDimension_Column);
		for(List<Object> value : values) {
			if(value.size() > 1) { //Size is greater than 1 because top row will always be present
				if(((String)value.get(1)).equalsIgnoreCase("true")) {
					Object[] environmentDatum = (Object[])value.toArray();
					environmentData.add(environmentDatum);
				}
			}
			else {
				logger.info("GSheet does not enough information to execute query");
			}
		}
		Iterator<Object[]> allDataIterator = environmentData.iterator();
		return allDataIterator;
	}

	@Test(dataProvider="fetchEnvironmentsData", threadPoolSize = 6)
	public void executeQuery(String environment, String create, String rHost, String mySqlUser, String sshHost, String sshUser,
			String lPort, String fileName, String sshRequired, String mySqlPassword, String sshPassword) {
		LocalTime timeStamp = LocalTime.now();
		logger.trace("~~~~~~~~~~~~~~~STARTING on-"+environment+" @"+timeStamp+"~~~~~~~~~~~~~~~");
		String domain 				= System.getProperty("Component");
		rHost 						= rHost.replace("$#$#", domain);
		if(Boolean.valueOf(sshRequired)) {
			ConnectToServer connectToServer = new ConnectToServer(sshUser, sshHost, sshPassword, ConstantLiterals.SSHPort, lPort, rHost, ConstantLiterals.RPort);
			ConnectToMysql connectToMysql = new ConnectToMysql(ConstantLiterals.Localhost, mySqlUser, mySqlPassword, lPort, fileName);
			connectToServer.createSSHSession();
			if(connectToServer.isSessionConnected()) {
				connectToMysql.createMYSQLConnection();
				if(connectToMysql.isConnected()){
					Assert.assertTrue(connectToMysql.executeSqlScript());
					connectToMysql.destroyMySqlConnection();
				}
				connectToServer.destroyLPort();
				connectToServer.destroySSHSession();
			}
		}
		else {
			ConnectToMysql connectToMysql = new ConnectToMysql(rHost, mySqlUser, mySqlPassword, ConstantLiterals.RPort, fileName);
			connectToMysql.createMYSQLConnection();
			if(connectToMysql.isConnected()){
				Assert.assertTrue(connectToMysql.executeSqlScript());
				connectToMysql.destroyMySqlConnection();
			}
		}
		timeStamp = LocalTime.now();
		logger.trace("~~~~~~~~~~~~~~~ENDING on-"+environment+" @"+timeStamp+"~~~~~~~~~~~~~~~");
	}
}