package com.purplehatstands.clementine.remote;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ServerListAdapter implements ListAdapter {
	private static final String SERVER_PREFS = "SERVER_PREFS";
	private static final String TAG = "ServerListAdapter";
	
	private ArrayList<Server> detected_servers_;
	private ArrayList<Server> stored_servers_;
	
	private Context context_;
	private SharedPreferences prefs_;
	
	public ServerListAdapter(Context context) {
		context_ = context;
		stored_servers_ = new ArrayList<Server>();
		detected_servers_ = new ArrayList<Server>();
		
		prefs_ = context_.getSharedPreferences(SERVER_PREFS, 0);
		Log.d(TAG, "Loading preferences...");
		String stored_servers = prefs_.getString("stored_servers", null);
		
		if (stored_servers != null) {
			Log.d(TAG, stored_servers);
			String[] servers = stored_servers.split("\\|");
			for (String server : servers) {
				Log.d(TAG, server);
				if (server.matches("[^:]+:[^:]+:[^:]+")) {
					String[] s = server.split(":");
					try {
						Log.d(TAG, "Adding");
						stored_servers_.add(new Server(s[0], s[1], Integer.parseInt(s[2])));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void addServer(Server server) {
		stored_servers_.add(server);

		StringBuilder builder = new StringBuilder();
		for (Server s : stored_servers_) {
			builder.append(s.getName());
			builder.append(':');
			builder.append(s.getAddress());
			builder.append(':');
			builder.append(s.getPort());
			builder.append('|');
		}
		
		Editor editor = prefs_.edit();
		editor.putString("stored_servers", builder.toString());
		editor.commit();
	}

	public int getCount() {
		return detected_servers_.size() + stored_servers_.size() + 2;
	}

	public Object getItem(int position) {
		if (position == 0 || position == stored_servers_.size() + 1) {
			return null;
		}
		return position <= stored_servers_.size() ? stored_servers_.get(position - 1) : detected_servers_.get(position - 2 - stored_servers_.size());
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getItemViewType(int position) {
		return IGNORE_ITEM_VIEW_TYPE;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// Use the default list item style.
		LayoutInflater inflater = (LayoutInflater)context_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		TextView view;
		if (getItem(position) == null) {
			view = new TextView(context_);
		} else {
			view = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
		}
		
		if (position == 0) {
			view.setText("Stored Servers:");
		} else if (position == stored_servers_.size() + 1) {
			view.setText("Detected servers:");
		} else {
			view.setText(((Server)getItem(position)).getName());
			view.setPadding(0, 2, 0, 3);
			view.setOnClickListener(new View.OnClickListener() {	
				public void onClick(View v) {
					context_.startActivity(new Intent());
				}
			});
		}
		return view;
	}

	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	public boolean areAllItemsEnabled() {
		return false;
	}

	public boolean isEnabled(int position) {
		// First position before each list is a header.
		return position != 0 && position != stored_servers_.size() + 1;
	}
}
