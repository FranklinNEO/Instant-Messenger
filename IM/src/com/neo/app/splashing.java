package com.neo.app;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.neo.neo.R;
import com.neo.xmpp.XmppTool;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.util.Log;

public class splashing extends Activity {
	private String LogName = "";
	private String PassWord = "";
	private String login = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 全屏
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(R.layout.splashing);

		FileInputStream inStream = null;
		ByteArrayOutputStream outStream = null;

		// 提取上次登录的信息
		try {
			inStream = this.openFileInput("userInfo.txt");
			outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
			String content = outStream.toString();
			Log.v("content", content);
			if (content != null && content.indexOf(";") > 1) {
				LogName = content.split(";")[0];
				PassWord = content.split(";")[1];
				login = content.split(";")[2];
			}
			outStream.close();
			inStream.close();
		} catch (IOException ex) {
		}

		new Thread() {
			public void run() {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				check();
				// Intent intent = new Intent();
				// intent.setClass(splashing.this, Login.class);
				// startActivity(intent);
				// splashing.this.finish();
			}
		}.start();

	}

	private void check() {
		if (login != null && login.equalsIgnoreCase("1")) {
			try {
				XmppTool.getConnection().login(LogName, PassWord);
				Log.i("XMPPClient", "Logged in as "
						+ XmppTool.getConnection().getUser());
				// status
				Presence presence = new Presence(Presence.Type.available);
				XmppTool.getConnection().sendPacket(presence);
				Intent intent = new Intent(splashing.this, MemberList.class);
				intent.putExtra("USERID", LogName);
				startActivity(intent);
				splashing.this.finish();
				Log.d("USERID", LogName);
			} catch (XMPPException e) {
				XmppTool.closeConnection();
			} catch (Exception e) {
				// TODO: handle exception
			}

			// ((IMapplication) this.getApplication()).setUserId(LogName);
			// ((IMapplication) this.getApplication()).setPwd(PassWord);
			// Intent intent = new Intent();
			// intent.putExtra("USERID", LogName);
			// intent.setClass(splashing.this, MemberList.class);
			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// startActivity(intent);
			// splashing.this.finish();
		} else {
			Intent intent = new Intent();
			intent.setClass(splashing.this, Login.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			splashing.this.finish();
		}
	}
}
