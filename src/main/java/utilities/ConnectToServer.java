package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectToServer {

	private static Logger logger = LogManager.getLogger(ConnectToServer.class.getName());

	private Session session = null;
	private int LPort 		= 0;
	private int RPort 		= 0;
	private int SSHPort 	= 0;
	private String RHost 	= null;
	private String SSHUser, SSHHost, SSHPassword, environment;

	public ConnectToServer(String environment, String SSHUser, String SSHHost, String SSHPassword, int SSHPort, int LPort, String RHost, int RPort) {
		this.environment = environment;
		this.SSHUser = SSHUser;
		this.SSHHost = SSHHost;
		this.SSHPassword = SSHPassword;
		this.SSHPort = SSHPort;
		this.RHost = RHost;
		this.LPort = LPort;
		this.RPort = RPort;
	}

	public void createSSHSession() {
		JSch jsch = new JSch();
		try {
			session	= jsch.getSession(SSHUser, SSHHost, SSHPort);
			session.setConfig("StrictHostKeyChecking", "No");
			session.setPassword(SSHPassword);
			session.connect(60000);
			logger.info("ssh {}@{}:{}",SSHUser,SSHHost, SSHPort);
			lPortRhostRPort();
		} 
		catch (JSchException e) {
			e.printStackTrace();
			logger.info("ssh {}@{}:{}",SSHUser,SSHHost, SSHPort);
			logger.info("ssh established? "+isSessionConnected());
		}
	}

	public int getLPort() {	
		return LPort;	
	}
	public int getRPort() {	
		return RPort;	
	}
	public String getRHost() {	
		return RHost;	
	}
	public void startEnvironment() {
		logger.trace("====================START-{}====================",environment.toUpperCase());
	}
	public void endEnvironment() {
		logger.trace("====================END-{}====================",environment.toUpperCase());
	}

	public Boolean isSessionConnected() {
		Boolean isSessionConnected = false;
		if(session != null) {
			isSessionConnected = session.isConnected();
			logger.info("ssh established? "+isSessionConnected);
		}
		return isSessionConnected;
	}

	private void lPortRhostRPort() {
		if(session !=null) {
			try {
				session.setPortForwardingL(LPort, RHost, RPort);
				for(String value : session.getPortForwardingL()) {
					logger.info("Local port forwarding as {}",value);
				}
			} 
			catch (JSchException e) {
				logger.error(e);
				return;
			}
		}
	}

	public void destroySSHSession() {
		if(session != null) {
			session.disconnect();
			logger.info("{}@{}:{} logged out",SSHUser,SSHHost, SSHPort);
		}
	}

	public void destroyLPort() {
		if(session != null) {
			try {
				session.delPortForwardingL(LPort);
				logger.info("{} local port forwarding destroyed",LPort);
			} 
			catch (JSchException e) {
				logger.error(e);
				return;
			}
		}
	}
}
