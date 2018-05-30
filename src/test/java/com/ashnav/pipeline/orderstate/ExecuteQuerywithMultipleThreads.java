package com.ashnav.pipeline.orderstate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import macros.ConstantLiterals;
import utilities.ConnectToGSheet;

public class ExecuteQuerywithMultipleThreads {

	private static Logger logger = LogManager.getLogger(ExecuteQuerywithMultipleThreads.class.getName());

	@Test
	public static void executeQuerywithMultipleThreads() {

		List<List<Object>> values 	= ConnectToGSheet.getCellValues(ConstantLiterals.GSheetSpreadsheetId, ConstantLiterals.GSheetCellRange, ConstantLiterals.MajorDimension_Column);
		int threadSize 				= 0;
		for(List<Object> value : values) {
			if(((String)value.get(1)).equalsIgnoreCase("true")) {
				threadSize++;
			}
		}
		if(threadSize > 0) {
			logger.info("Created "+threadSize+" threads for each environment");
			ExecutorService executor 	= Executors.newFixedThreadPool(threadSize);
			String domain 				= System.getProperty("domain");
			for(List<Object> value : values) {
				if(value.size() > 1) { //Size is greater than 1 because top row will always be present
					if(((String)value.get(1)).equalsIgnoreCase("true")) {
						String environment      = value.get(0).toString();
						String rHost 			= value.get(2).toString();
						rHost 					= rHost.replace("$#$#", domain);
						String mySqlUser 		= value.get(3).toString();
						String sshHost 			= value.get(4).toString();
						String sshUser 			= value.get(5).toString();
						String lPort			= value.get(6).toString();
						String fileName			= value.get(7).toString();
						String sshRequired		= value.get(8).toString();
						String mySqlPassword 	= value.get(9).toString();
						String sshPassword 		= value.get(10).toString();
						Runnable workerThread = new WorkerThread(environment, rHost, mySqlUser, mySqlPassword, sshHost, sshUser, sshPassword,fileName, sshRequired, lPort);
						executor.execute(workerThread);
					}
				}
				else {
					logger.info("GSheet has not enough information for executing query");
				}
			}
			executor.shutdown();
			while(!executor.isTerminated()){}
			logger.info("Ended "+threadSize+" threads for each environment");
		}
		else {
			logger.info("Threads count is "+threadSize+"!!!");
		}
	}
}