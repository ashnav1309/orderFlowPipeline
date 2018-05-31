package com.ashnav.pipeline.orderstate;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import macros.ConstantLiterals;
import utilities.AdvancedEncryptionStandard;
import utilities.ConnectToGSheet;
import utilities.ConnectToMysql;
import utilities.ConnectToServer;

public class ExecuteQuerywithMultipleThreads {

	private static Logger logger 							= LogManager.getLogger(ExecuteQuerywithMultipleThreads.class.getName());

	@DataProvider(name = "fetchEnvironmentsData", parallel= true)
	public Object[][] getTestData()
	{	
		List<List<Object>> values 								= ConnectToGSheet.getCellValues(ConstantLiterals.GSheetSpreadsheetId, ConstantLiterals.GSheetCellRange, ConstantLiterals.MajorDimension_Column);
		List<HashMap<String, Object>> arrayMapList 				= new ArrayList<HashMap<String, Object>>();

		for(List<Object> value : values) {
			HashMap<String, Object> hashMapItems 				= new HashMap<String, Object>();
			if(value.size() > 1) { //Size is greater than 1 because top row will always be present
				if(((String)value.get(1)).equalsIgnoreCase("true")) {				
					hashMapItems.put("environment", (String)value.get(0));
					String rHost = ((String)value.get(2)).replace("$#$#", System.getProperty("Component"));
					hashMapItems.put("rHost", rHost);
					hashMapItems.put("mySqlUser", (String)value.get(3));
					hashMapItems.put("sshHost", (String)value.get(4));
					hashMapItems.put("sshUser", (String)value.get(5));
					hashMapItems.put("lPort", (String)value.get(6));
					hashMapItems.put("fileName", (String)value.get(7));
					hashMapItems.put("sshRequired", (String)value.get(8));
					byte[] mySqlPasswordEncrypt = AdvancedEncryptionStandard.encrypt(((String)value.get(9)).getBytes(StandardCharsets.UTF_8));
					hashMapItems.put("mySqlPassword", mySqlPasswordEncrypt);
					byte[] sshPasswordEncrypt = AdvancedEncryptionStandard.encrypt(((String)value.get(10)).getBytes(StandardCharsets.UTF_8));
					hashMapItems.put("sshPassword", sshPasswordEncrypt);
				}
			}
			if(!hashMapItems.isEmpty()) {
				arrayMapList.add(hashMapItems);
			}
		}
		Object [][] hashMapObj = new Object [arrayMapList.size()][1];
		for(int i=0; i<arrayMapList.size() ; i++) {
			hashMapObj[i][0] = arrayMapList.get(i);
		}
		return hashMapObj;
	}
	
	@Test(dataProvider="fetchEnvironmentsData", threadPoolSize = 6)
	public void executeQuery(HashMap<String, Object> hashMapValue) {
		String environment 					= (String)hashMapValue.get("environment");
		String rHost 						= (String)hashMapValue.get("rHost");
		String mySqlUser 					= (String)hashMapValue.get("mySqlUser");
		String sshHost 						= (String)hashMapValue.get("sshHost");
		String sshUser 						= (String)hashMapValue.get("sshUser");
		String lPort 						= (String)hashMapValue.get("lPort");
		String fileName 					= (String)hashMapValue.get("fileName");
		String sshRequired 					= (String)hashMapValue.get("sshRequired");
		byte[] mySqlPasswordDataProvider 	= (byte[])hashMapValue.get("mySqlPassword");
		byte[] sshPasswordDataProvider 		= (byte[])hashMapValue.get("sshPassword");
		byte[] mySqlPasswordDecrypt 		= AdvancedEncryptionStandard.decrypt(mySqlPasswordDataProvider);
		byte[] sshPasswordDecrypt 			= AdvancedEncryptionStandard.decrypt(sshPasswordDataProvider);
		
		String mySqlPassword 	= new String(mySqlPasswordDecrypt);
		String sshPassword 		= new String(sshPasswordDecrypt);

		Boolean condition 		= false;
		LocalTime timeStamp 	= LocalTime.now();
		logger.trace("STARTING on-"+environment+" @"+timeStamp);
		if(Boolean.valueOf(sshRequired)) {
			ConnectToServer connectToServer = new ConnectToServer(sshUser, sshHost, sshPassword, ConstantLiterals.SSHPort, lPort, rHost, ConstantLiterals.RPort);
			ConnectToMysql connectToMysql = new ConnectToMysql(ConstantLiterals.Localhost, mySqlUser, mySqlPassword, lPort, fileName);
			connectToServer.createSSHSession();
			if(connectToServer.isSessionConnected()) {
				connectToMysql.createMYSQLConnection();
				if(connectToMysql.isConnected()){
					condition = connectToMysql.executeSqlScript();
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
				condition = connectToMysql.executeSqlScript();
				connectToMysql.destroyMySqlConnection();
			}
		}
		timeStamp = LocalTime.now();
		logger.trace("ENDING on-"+environment+" @"+timeStamp);
		Reporter.log("Query is executed successfully on "+environment+"?: "+condition);
		Assert.assertTrue(condition, "Query is NOT executed successfully on "+environment);
	}
}