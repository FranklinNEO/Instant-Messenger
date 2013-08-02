package com.neo.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class XmppTool {

	private static XMPPConnection con = null;
	private static String host = "192.168.100.166";
	private static String port = "5222";

	private static void openConnection() {
		try {
			ConnectionConfiguration connConfig = new ConnectionConfiguration(
					host, Integer.parseInt(port));
			con = new XMPPConnection(connConfig);
			con.connect();
		} catch (XMPPException xe) {
			xe.printStackTrace();
		}
	}

	public static XMPPConnection getConnection() {
		if (con == null) {
			openConnection();
		}
		return con;
	}

	public static void closeConnection() {
		con.disconnect();
		con = null;
	}
}
