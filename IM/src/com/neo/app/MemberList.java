package com.neo.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import com.neo.app.Chatting.Msg;
import com.neo.neo.R;
import com.neo.xmpp.TimeRender;
import com.neo.xmpp.XmppTool;

import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class MemberList extends ExpandableListActivity {
	private static final int MENU_LOGOUT = Menu.FIRST;
	private ArrayList<ArrayList<String>> nameNum = new ArrayList<ArrayList<String>>();
	private ArrayList<String> groupName = new ArrayList<String>();

	private List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
	private List<List<Map<String, String>>> childs = new ArrayList<List<Map<String, String>>>();
	public NotificationManager nm = null;
	private String pUSERID = null;
	private ExAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memberlist);
		this.pUSERID = getIntent().getStringExtra("USERID");
		Roster roster = XmppTool.getConnection().getRoster();
		Collection<RosterGroup> entriesGroup = roster.getGroups();

		for (RosterGroup group : entriesGroup) {
			Collection<RosterEntry> entries = group.getEntries();
			Log.i("---", group.getName());
			ArrayList<String> namelist = new ArrayList<String>();
			for (RosterEntry entry : entries) {
				// Presence presence = roster.getPresence(entry.getUser());
				// Log.i("---", "user: "+entry.getUser());
				Log.i("---", "name: " + entry.getName());
				// Log.i("---", "tyep: "+entry.getType());
				// Log.i("---", "status: "+entry.getStatus());
				// Log.i("---", "groups: "+entry.getGroups());
				namelist.add(entry.getName());
			}
			nameNum.add(namelist);
			groupName.add(group.getName());
		}

		groups = new ArrayList<Map<String, String>>();
		childs = new ArrayList<List<Map<String, String>>>();

		for (int i = 0; i < (nameNum.size()); i++) {
			Map<String, String> group = new HashMap<String, String>();
			group.put("group", groupName.get(i));
			groups.add(group);
			ArrayList<String> childlist = nameNum.get(i);
			List<Map<String, String>> child = new ArrayList<Map<String, String>>();
			for (int j = 0; j < (childlist.size()); j++) {
				Map<String, String> childdata = new HashMap<String, String>();
				childdata.put("child", childlist.get(j));
				child.add(childdata);
			}
			childs.add(child);
		}
		// SimpleExpandableListAdapter adapter = new
		// SimpleExpandableListAdapter(
		// this, groups, R.layout.group, new String[] { "group" },
		// new int[] { R.id.group }, childs, R.layout.child,
		// new String[] { "child" }, new int[] { R.id.child });
		adapter = new ExAdapter(MemberList.this);
		MemberList.this.getExpandableListView().setAdapter(adapter);
		MemberList.this.getExpandableListView().setGroupIndicator(null);
		// MemberList.this.getExpandableListView().setDivider(null);

		PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
		XmppTool.getConnection().addPacketListener(new PacketListener() {
			public void processPacket(Packet packet) {
				Message message = (Message) packet;
				if (message.getBody() != null) {
					String fromName = StringUtils.parseBareAddress(message
							.getFrom());
					String chatname = fromName.split("@")[0];
					Log.i("XMPPClient", "Got text [" + message.getBody()
							+ "] from [" + fromName + "]");
					if (((IMapplication) MemberList.this.getApplication()).getchatstate()) {

					} else {
						showNotification(chatname, message.getBody());
						// clearNotification();
						Log.d("oberserve", "show");
					}
				}
			}
		}, filter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		((IMapplication) MemberList.this.getApplication()).setchatstate(false);
		super.onResume();
	}

	// 删除通知
	public void clearNotification() {
		// 启动后删除之前我们定义的通知
		nm.cancel(R.string.app_name);

	}

	private void showNotification(String chatname, String message) {
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.chinaz5, chatname,
				System.currentTimeMillis());
		n.flags = Notification.FLAG_AUTO_CANCEL;
		Bundle bundle = new Bundle();
		bundle.putString("chatname", chatname);
		bundle.putString("USERID", pUSERID);
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtras(bundle);
		i.setClass(MemberList.this, Chatting.class);
		// PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(
				MemberList.this, R.string.app_name, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		n.setLatestEventInfo(MemberList.this, chatname, message, contentIntent);
		nm.notify(R.string.app_name, n);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		XmppTool.closeConnection();
		MemberList.this.finish();
		super.onBackPressed();
	}

	class ExAdapter extends BaseExpandableListAdapter {
		MemberList exlistview;

		public ExAdapter(MemberList elv) {
			super();
			exlistview = elv;
		}

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.member_listview, null);
			}

			TextView title = (TextView) view.findViewById(R.id.content_001);
			title.setText(getGroup(groupPosition).toString());

			ImageView image = (ImageView) view.findViewById(R.id.tubiao);
			if (isExpanded)
				image.setBackgroundResource(R.drawable.login_more_up);
			else
				image.setBackgroundResource(R.drawable.login_more);

			return view;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public Object getGroup(int groupPosition) {
			return groups.get(groupPosition).get("group");
		}

		public int getGroupCount() {
			return groups.size();

		}

		// **************************************
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.member_childitem, null);
			}
			final TextView title = (TextView) view
					.findViewById(R.id.child_text);
			title.setText(childs.get(groupPosition).get(childPosition)
					.get("child"));
			final TextView title2 = (TextView) view
					.findViewById(R.id.child_text2);
			title2.setText(childs.get(groupPosition).get(childPosition)
					.get("child"));

			return view;
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public Object getChild(int groupPosition, int childPosition) {
			return childs.get(groupPosition).get(childPosition).get("child");
		}

		public int getChildrenCount(int groupPosition) {
			return childs.get(groupPosition).size();
		}

		// **************************************
		public boolean hasStableIds() {
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	/**
	 * 设置哪个二级目录被默认选中
	 */
	@Override
	public boolean setSelectedChild(int groupPosition, int childPosition,
			boolean shouldExpandGroup) {
		// do something
		return super.setSelectedChild(groupPosition, childPosition,
				shouldExpandGroup);
	}

	/**
	 * 设置哪个一级目录被默认选中
	 */
	@Override
	public void setSelectedGroup(int groupPosition) {
		// do something
		super.setSelectedGroup(groupPosition);
	}

	/**
	 * 当二级条目被点击时响应、、
	 */
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// do something
		Log.d("groupPos", groupPosition + "");
		Log.d("childPos", childPosition + "");
		String chatname = nameNum.get(groupPosition).get(childPosition);
		Log.d("chatname", chatname);
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("chatname", chatname);
		bundle.putString("USERID", pUSERID);
		intent.putExtras(bundle);
		intent.setClass(MemberList.this, Chatting.class);
		startActivity(intent);
		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}

	public void onSettingClick(View view) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, MENU_LOGOUT, 0, "注销帐号");

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case MENU_LOGOUT:
			Dialog dialog = null;
			com.neo.ui.CustomDialog.Builder customBuilder = new com.neo.ui.CustomDialog.Builder(
					MemberList.this);
			customBuilder
					.setMessage("确定要注销您的登录信息吗？")
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							})
					.setPositiveButton(" 退出登录 ",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									try {
										FileOutputStream outStream = MemberList.this
												.openFileOutput("userInfo.txt",
														Context.MODE_PRIVATE);
										String content = "" + ";" + "" + ";"
												+ "";
										outStream.write(content.getBytes());
										outStream.close();
									} catch (IOException ex) {

									}
									// File file = new File(URL,
									// CodeDBHelper.DATABASE_NAME);
									// db = SQLiteDatabase.openOrCreateDatabase(
									// file, null);
									// db.delete(CodeDBHelper.CODE_TABLE_NAME,
									// null, null);
									// db.close();
									Intent intent = new Intent(
											getApplication(), Login.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									XmppTool.closeConnection();
									MemberList.this.finish();
									dialog.dismiss();
								}
							});
			dialog = customBuilder.create();
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
