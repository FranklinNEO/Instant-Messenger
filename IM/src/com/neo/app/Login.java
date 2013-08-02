package com.neo.app;

import java.io.FileOutputStream;
import java.io.IOException;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.neo.neo.R;
import com.neo.xmpp.XmppTool;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener {

	private EditText userEditText = null;
	private EditText passwordEditText = null;
	private Button loginButton = null;
	public Dialog logindialog = null;
	private boolean flag = false;
	private CheckBox saveUserInfoCbox = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		userEditText = (EditText) findViewById(R.id.et_account);
		passwordEditText = (EditText) findViewById(R.id.et_password);
		loginButton = (Button) findViewById(R.id.btn_login);
		loginButton.setOnClickListener(this);
		logindialog = new Dialog(Login.this, R.style.mmdialog);
		logindialog.setContentView(R.layout.login_dialog);
		this.saveUserInfoCbox = (CheckBox) this
				.findViewById(R.id.remember_user_checkbox);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_login:
			final String account = userEditText.getText().toString();
			final String password = passwordEditText.getText().toString();
			if (account.equals("") || password.equals("")) {
				Toast.makeText(Login.this, "账号或密码不能为空！", Toast.LENGTH_SHORT)
						.show();
			} else {

				new Thread(new Runnable() {
					@Override
					public void run() {
						flag = true;
						handler.sendEmptyMessage(1);
						try {
							XmppTool.getConnection().login(account, password);
							Log.i("XMPPClient", "Logged in as "
									+ XmppTool.getConnection().getUser());
							// status
							Presence presence = new Presence(
									Presence.Type.available);
							XmppTool.getConnection().sendPacket(presence);
							Intent intent = new Intent(Login.this,
									MemberList.class);
							intent.putExtra("USERID", account);
							startActivity(intent);
							Login.this.finish();
							saveUserInfo(account, password, ("1").trim());
							handler.sendEmptyMessage(2);
						} catch (XMPPException e) {
							XmppTool.closeConnection();
							handler.sendEmptyMessage(3);
						} catch (Exception e) {
							// TODO: handle exception
							handler.sendEmptyMessage(4);
						}
					}

				}).start();
			}
			break;
		default:
			break;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				logindialog.show();
				break;
			case 2:
				logindialog.dismiss();
				break;
			case 3:
				logindialog.dismiss();
				break;
			case 4:
				logindialog.dismiss();
				Toast.makeText(Login.this, "登录失败", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	public void saveUserInfo(String uid, String pwd, String login) {
		if (!this.saveUserInfoCbox.isChecked())
			return;
		try {
			FileOutputStream outStream = this.openFileOutput("userInfo.txt",
					Context.MODE_PRIVATE);
			String content = uid + ";" + pwd + ";" + login;
			outStream.write(content.getBytes());
			outStream.close();
		} catch (IOException ex) {

		}
	}
}
