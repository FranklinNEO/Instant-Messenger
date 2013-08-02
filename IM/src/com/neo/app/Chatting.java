package com.neo.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import com.neo.neo.R;
import com.neo.xmpp.TimeRender;
import com.neo.xmpp.XmppTool;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Chatting extends Activity {

	private MyAdapter adapter;
	private List<Msg> listMsg = new ArrayList<Msg>();
	private String pUSERID;
	private EditText msgText;
	private ProgressBar pb;
	private ListView listview = null;
	private final static int CHANGED = 1;
	private String chatname = null;

	public class Msg {
		String userid;
		String msg;
		String date;
		String from;

		public Msg(String userid, String msg, String date, String from) {
			this.userid = userid;
			this.msg = msg;
			this.date = date;
			this.from = from;
		}
	}

	Handler myHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case CHANGED:
				adapter.notifyDataSetChanged();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatting);
		Bundle bundle = this.getIntent().getExtras();
		chatname = bundle.getString("chatname");
		this.pUSERID = getIntent().getStringExtra("USERID");
		listview = (ListView) findViewById(R.id.formclient_listview);
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		this.adapter = new MyAdapter(this);
		listview.setAdapter(adapter);
		this.msgText = (EditText) findViewById(R.id.formclient_text);
		this.pb = (ProgressBar) findViewById(R.id.formclient_pb);
		TextView person = (TextView) findViewById(R.id.person_id);
		person.setText(chatname);
		((IMapplication)Chatting.this.getApplication()).setchatstate(true);
		// message listener
		PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
		XmppTool.getConnection().addPacketListener(new PacketListener() {
			public void processPacket(Packet packet) {
				Message message = (Message) packet;
				if (message.getBody() != null) {
					String fromName = StringUtils.parseBareAddress(message
							.getFrom());
					Log.i("XMPPClient", "Got text [" + message.getBody()
							+ "] from [" + fromName + "]");
					// listMsg.add(new Msg(fromName, message.getBody(),
					// TimeRender
					// .getDate(), "IN"));
					listMsg.add(new Msg(chatname, message.getBody(), TimeRender
							.getDate(), "IN"));
					myHandler.sendEmptyMessage(CHANGED);
					// adapter.notifyDataSetChanged();
				}
			}
		}, filter);

		ChatManager cm = XmppTool.getConnection().getChatManager();
		final Chat newchat = cm.createChat(chatname + "@FranklinNEO-PC", null);

		// send file
		Button btattach = (Button) findViewById(R.id.formclient_btattach);
		btattach.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Chatting.this, FormFiles.class);
				startActivityForResult(intent, 2);
			}
		});

		// send message
		Button btsend = (Button) findViewById(R.id.formclient_btsend);
		btsend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = msgText.getText().toString();
				if (msg.length() > 0) {
					listMsg.add(new Msg(pUSERID, msg, TimeRender.getDate(),
							"OUT"));
					adapter.notifyDataSetChanged();
					try {
						newchat.sendMessage(msg);
					} catch (XMPPException e) {
						e.printStackTrace();
					} catch (Exception e) {
						// TODO: handle exception
						Toast.makeText(Chatting.this, "与服务器已断开连接",
								Toast.LENGTH_SHORT).show();
					}
				}
				msgText.setText("");
			}
		});

		// receive file
		FileTransferManager fileTransferManager = new FileTransferManager(
				XmppTool.getConnection());
		fileTransferManager
				.addFileTransferListener(new RecFileTransferListener());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 2 && resultCode == 2 && data != null) {
			String filepath = data.getStringExtra("filepath");
			if (filepath.length() > 0) {
				sendFile(filepath);
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void sendFile(String filepath) {
		// ServiceDiscoveryManager sdm = new
		// ServiceDiscoveryManager(connection);
		final FileTransferManager fileTransferManager = new FileTransferManager(
				XmppTool.getConnection());
		final OutgoingFileTransfer fileTransfer = fileTransferManager
				.createOutgoingFileTransfer(chatname + "@FranklinNEO-PC/Smack");
		final File file = new File(filepath);
		try {
			fileTransfer.sendFile(file, "Sending");
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(500L);
						Status status = fileTransfer.getStatus();
						if ((status == FileTransfer.Status.error)
								|| (status == FileTransfer.Status.complete)
								|| (status == FileTransfer.Status.cancelled)
								|| (status == FileTransfer.Status.refused)) {
							handler.sendEmptyMessage(4);
							break;
						} else if (status == FileTransfer.Status.negotiating_transfer) {
							// ..
						} else if (status == FileTransfer.Status.negotiated) {
							// ..
						} else if (status == FileTransfer.Status.initial) {
							// ..
						} else if (status == FileTransfer.Status.negotiating_stream) {
							// ..
						} else if (status == FileTransfer.Status.in_progress) {
							handler.sendEmptyMessage(2);
							long p = fileTransfer.getBytesSent() * 100L
									/ fileTransfer.getFileSize();
							android.os.Message message = handler
									.obtainMessage();
							message.arg1 = Math.round((float) p);
							message.what = 3;
							message.sendToTarget();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private FileTransferRequest request;
	private File file;

	class RecFileTransferListener implements FileTransferListener {
		@Override
		public void fileTransferRequest(FileTransferRequest prequest) {
			System.out.println("The file received from: "
					+ prequest.getRequestor());
			file = new File("mnt/sdcard/" + prequest.getFileName());
			request = prequest;
			handler.sendEmptyMessage(5);
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				String[] args = (String[]) msg.obj;
				listMsg.add(new Msg(args[0], args[1], args[2], args[3]));
				adapter.notifyDataSetChanged();
				break;
			case 2:
				if (pb.getVisibility() == View.GONE) {
					pb.setMax(100);
					pb.setProgress(0);
					pb.setVisibility(View.VISIBLE);
				}
				break;
			case 3:
				pb.setProgress(msg.arg1);
				break;
			case 4:
				pb.setVisibility(View.GONE);
				break;
			case 5:
				final IncomingFileTransfer infiletransfer = request.accept();

				Dialog dialog = null;
				com.neo.ui.CustomDialog.Builder customBuilder = new com.neo.ui.CustomDialog.Builder(
						Chatting.this);
				customBuilder
						.setMessage("接收文件")
						.setNegativeButton(" 取消 ",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										request.reject();
										dialog.dismiss();
									}
								})
						.setPositiveButton(" 接收 ",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											infiletransfer.recieveFile(file);
										} catch (XMPPException e) {
											e.printStackTrace();
										}
										handler.sendEmptyMessage(2);
										Timer timer = new Timer();
										TimerTask updateProgessBar = new TimerTask() {
											public void run() {
												if ((infiletransfer
														.getAmountWritten() >= request
														.getFileSize())
														|| (infiletransfer
																.getStatus() == FileTransfer.Status.error)
														|| (infiletransfer
																.getStatus() == FileTransfer.Status.refused)
														|| (infiletransfer
																.getStatus() == FileTransfer.Status.cancelled)
														|| (infiletransfer
																.getStatus() == FileTransfer.Status.complete)) {
													cancel();
													handler.sendEmptyMessage(4);
												} else {
													long p = infiletransfer
															.getAmountWritten()
															* 100L
															/ infiletransfer
																	.getFileSize();
													android.os.Message message = handler
															.obtainMessage();
													message.arg1 = Math
															.round((float) p);
													message.what = 3;
													message.sendToTarget();
												}
											}
										};
										timer.scheduleAtFixedRate(
												updateProgessBar, 10L, 10L);
										dialog.dismiss();
									}
								});
				dialog = customBuilder.create();
				dialog.show();

				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// XmppTool.closeConnection();
		Chatting.this.finish();
		// System.exit(0);
	}

	class MyAdapter extends BaseAdapter {

		private Context cxt;
		private LayoutInflater inflater;

		public MyAdapter(Chatting formClient) {
			this.cxt = formClient;
		}

		@Override
		public int getCount() {
			return listMsg.size();
		}

		@Override
		public Object getItem(int position) {
			return listMsg.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			this.inflater = (LayoutInflater) this.cxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (listMsg.get(position).from.equals("IN")) {
				convertView = this.inflater.inflate(
						R.layout.formclient_chat_in, null);
			} else {
				convertView = this.inflater.inflate(
						R.layout.formclient_chat_out, null);
			}
			TextView useridView = (TextView) convertView
					.findViewById(R.id.formclient_row_userid);
			TextView dateView = (TextView) convertView
					.findViewById(R.id.formclient_row_date);
			TextView msgView = (TextView) convertView
					.findViewById(R.id.formclient_row_msg);
			useridView.setText(listMsg.get(position).userid);
			dateView.setText(listMsg.get(position).date);
			msgView.setText(listMsg.get(position).msg);
			return convertView;
		}
	}

}
