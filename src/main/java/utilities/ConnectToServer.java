package utilities;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectToServer {

	private Session session = null;
	private int LPort = 0;
	private int RPort = 0;
	private String RHost = null;

	public ConnectToServer(String SSHUser, String SSHHost, String SSHPassword, int SSHPort, int LPort, String RHost, int RPort) {

		this.RHost = RHost;
		this.LPort = LPort;
		this.RPort = RPort;
		JSch jsch = new JSch();
		try {
			session	= jsch.getSession(SSHUser, SSHHost, SSHPort);
			session.setConfig("StrictHostKeyChecking", "No");
			session.setPassword(SSHPassword);
			session.connect(60000);
		} catch (JSchException e) {
			e.printStackTrace();
		}
		System.out.printf("========================================================================\n");
		System.out.printf("ssh %s@%s:%d \n",SSHUser,SSHHost, SSHPort);
		isSessionConnected();
		lPortRhostRPort();
		System.out.printf("========================================================================\n\n");
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
			System.out.println("ssh established?"+isSessionConnected);
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
			} catch (JSchException e) {
				e.printStackTrace();
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
			} catch (JSchException e) {
				e.printStackTrace();
			}
		}
	}
}
