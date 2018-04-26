package utilities;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectToServer {

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

	public Boolean createSSHSession() {
		Boolean sshConnection = false;
		JSch jsch = new JSch();
		try {
			session	= jsch.getSession(SSHUser, SSHHost, SSHPort);
			session.setConfig("StrictHostKeyChecking", "No");
			session.setPassword(SSHPassword);
			session.connect(60000);
			System.out.printf("===============================%s===================================\n",environment);
			System.out.printf("ssh %s@%s:%d \n",SSHUser,SSHHost, SSHPort);
			sshConnection = isSessionConnected();
			lPortRhostRPort();
			System.out.printf("===============================%s===================================\n",environment);
			return sshConnection;
		} 
		catch (JSchException e) {
			e.printStackTrace();
			System.out.printf("==================================%s======================================\n",environment);
			System.out.printf("ssh %s@%s:%d \n",SSHUser,SSHHost, SSHPort);
			System.out.println("ssh established? "+sshConnection);
			System.out.printf("==================================%s======================================\n",environment);
			return sshConnection;
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

	private Boolean isSessionConnected() {
		Boolean isSessionConnected = false;
		if(session != null) {
			isSessionConnected = session.isConnected();
			System.out.println("ssh established? "+isSessionConnected);
		}
		return isSessionConnected;
	}

	private void lPortRhostRPort() {
		if(session !=null) {
			try {
				session.setPortForwardingL(LPort, RHost, RPort);
				for(String value : session.getPortForwardingL()) {
					System.out.println(value);
				}
			} 
			catch (JSchException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public void destroySSHSession() {
		if(session != null) {
			session.disconnect();
			System.out.println("SSH Connection destroyed");
		}
	}

	public void destroyLPort() {
		if(session != null) {
			try {
				session.delPortForwardingL(LPort);
				System.out.println("LPort destroyed");
			} 
			catch (JSchException e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
