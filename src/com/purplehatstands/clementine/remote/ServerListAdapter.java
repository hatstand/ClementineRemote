package com.purplehatstands.clementine.remote;

import java.util.ArrayList;

import android.app.Activity;
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
	
	private ArrayList<DataSetObserver> observers_ = new ArrayList<DataSetObserver>();
	
	private Activity activity_;
	private SharedPreferences prefs_;
	
	public ServerListAdapter(Activity activity) {
		activity_ = activity;
		stored_servers_ = new ArrayList<Server>();
		detected_servers_ = new ArrayList<Server>();
		
		prefs_ = activity_.getSharedPreferences(SERVER_PREFS, 0);
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
		notifyObservers();
		storeServers();
	}
	
	public void removeServer(Server server) {
		stored_servers_.remove(server);
		notifyObservers();
		storeServers();
	}
	
	public void removeServer(int position) {
		Server s = (Server) getItem(position);
		if (s != null) {
			removeServer(s);
		}
	}
	
	private void notifyObservers() {
		for (DataSetObserver observer : observers_) {
			observer.onChanged();
		}
	}
	
	private void storeServers() {
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
		LayoutInflater inflater = (LayoutInflater)activity_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		TextView view;
		if (getItem(position) == null) {
			view = new TextView(activity_);
		} else {
			view = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
		}
		
		if (position == 0) {
			view.setText("Stored Servers:");
		} else if (position == stored_servers_.size() + 1) {
			view.setText("Detected servers:");
		} else {
			final Server server = (Server) getItem(position);
			view.setText(server.getName());
			view.setPadding(0, 2, 0, 3);
			view.setClickable(true);
			view.setBackgroundResource(android.R.drawable.list_selector_background);
			view.setOnClickListener(new View.OnClickListener() {	
				public void onClick(View v) {
					Intent intent = new Intent(activity_, NowPlayingActivity.class);
					intent.putExtra("server", server);
					activity_.startActivity(intent);
				}
			});
			activity_.registerForContextMenu(view);
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
		observers_.add(observer);
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		observers_.remove(observer);
	}

	public boolean areAllItemsEnabled() {
		return false;
	}

	public boolean isEnabled(int position) {
		// First position before each list is a header.
		return position != 0 && position != stored_servers_.size() + 1;
	}
}
