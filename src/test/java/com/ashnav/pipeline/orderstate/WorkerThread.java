package com.ashnav.pipeline.orderstate;

import java.time.LocalTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import macros.ConstantLiterals;
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
