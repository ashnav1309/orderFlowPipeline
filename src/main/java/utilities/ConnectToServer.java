package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectToServer {

	private static Logger logger = LogManager.getLogger(ConnectToServer.class.getName());

	private Session session	= null;
	private final int LPort;
	private final int RPort;
	private final int SSHPort;
	private final String RHost;
	private final String SSHUser, SSHHost, SSHPassword;

	public ConnectToServer(String SSHUser, String SSHHost, String SSHPassword, String SSHPort, String LPort, String RHost, String RPort) {
		this.SSHUser = SSHUser;
		this.SSHHost = SSHHost;
		this.SSHPassword = SSHPassword;
		this.SSHPort = Integer.parseInt(SSHPort);
		this.RHost = RHost;
		this.LPort = Integer.parseInt(LPort);
		this.RPort = Integer.parseInt(RPort);
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
		}
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
