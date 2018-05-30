package com.ashnav.pipeline.orderstate;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import macros.ConstantLiterals;
import utilities.ConnectToGSheet;
import utilities.ConnectToMysql;
import utilities.ConnectToServer;

class WorkerThread implements Runnable {

	private static Logger logger = LogManager.getLogger(WorkerThread.class.getName());

	String environment, rHost, mySqlUser, mySqlPassword, sshHost, sshUser, sshPassword,fileName, sshRequired, lPort;
	
	public WorkerThread(String environment, String rHost, String mySqlUser, String mySqlPassword, String sshHost,
			String sshUser, String sshPassword, String fileName, String sshRequired, String lPort) {
		this.environment = environment;
		this.rHost = rHost;
		this.mySqlUser = mySqlUser;
		this.mySqlPassword = mySqlPassword;
		this.sshHost = sshHost;
		this.sshUser = sshUser;
		this.sshPassword = sshPassword;
		this.fileName = fileName;
		this.sshRequired = sshRequired;
		this.lPort = lPort;
	}

	@Override
	public void run() {
		executeQuery();
	}

	private void executeQuery() {

		LocalTime timeStamp = LocalTime.now();
		logger.trace("~~~~~~~~~~~~~~~STARTING on-"+environment+" @"+timeStamp+"~~~~~~~~~~~~~~~");
		if(Boolean.valueOf(sshRequired)) {
			ConnectToServer connectToServer = new ConnectToServer(sshUser, sshHost, sshPassword, ConstantLiterals.SSHPort, lPort, rHost, ConstantLiterals.RPort);
			ConnectToMysql connectToMysql = new ConnectToMysql(ConstantLiterals.Localhost, mySqlUser, mySqlPassword, lPort, fileName);
			connectToServer.createSSHSession();
			if(connectToServer.isSessionConnected()) {
				connectToMysql.createMYSQLConnection();
				if(connectToMysql.isConnected()){
					connectToMysql.executeSqlScript();
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
				connectToMysql.executeSqlScript();
				connectToMysql.destroyMySqlConnection();
			}
		}
		timeStamp = LocalTime.now();
		logger.trace("~~~~~~~~~~~~~~~ENDING on-"+environment+" @"+timeStamp+"~~~~~~~~~~~~~~~");
	}
}

public class ExecuteQuerywithMultipleThreads {

	private static Logger logger = LogManager.getLogger(ExecuteQuerywithMultipleThreads.class.getName());

	public static void main(String[] args) {

		List<List<Object>> values 	= ConnectToGSheet.getCellValues(ConstantLiterals.GSheetSpreadsheetId, ConstantLiterals.GSheetCellRange, ConstantLiterals.MajorDimension_Column);
		int threadSize 				= values.size();
		logger.info("Created "+threadSize+" threads for each environment");
		ExecutorService executor 	= Executors.newFixedThreadPool(threadSize);
		for(List<Object> value : values) {
			if(value.size() > 1) { //Size is greater than 1 because top row will always be present
				String environment		= value.get(0).toString();
				String rHost 			= value.get(1).toString();
				String mySqlUser 		= value.get(2).toString();
				String sshHost 			= value.get(3).toString();
				String sshUser 			= value.get(4).toString();
				String lPort			= value.get(5).toString();
				String fileName			= value.get(6).toString();
				String sshRequired		= value.get(7).toString();
				String mySqlPassword 	= value.get(8).toString();
				String sshPassword 		= value.get(9).toString();
				
				Runnable workerThread = new WorkerThread(environment, rHost, mySqlUser, mySqlPassword, sshHost, sshUser, sshPassword,fileName, sshRequired, lPort);
				executor.execute(workerThread);
			}
			else {
				logger.info("GSheet has not enough information for executing query");
			}
		}
		executor.shutdown();
		while(!executor.isTerminated()){}
		logger.info("Ended "+threadSize+" threads for each environment");
	}
}