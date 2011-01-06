package com.purplehatstands.clementine.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.http.AndroidHttpClient;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ClementineRemote extends Activity implements ServiceListener, ResponseHandler<String> {
	private static final String TAG = "ClementineRemote";
	
	static final int ADD_SERVER_REQUEST = 0;
	
    private MulticastLock lock_;
    private JmDNS mdns_ = null;
    private WifiManager wifi_;
    private ServerListAdapter servers_;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        servers_ = new ServerListAdapter(this);
        
        ListView listview = (ListView) findViewById(R.id.servers);

        final TextView button = new TextView(this);
        final Context context = this;
        button.setText("Add server");
        button.setHeight(50);
        button.setTextSize(30);
        button.setTextColor(Color.WHITE);
        button.setBackgroundResource(android.R.drawable.list_selector_background);
        button.setGravity(Gravity.FILL_HORIZONTAL);
        button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "clicked");
				startActivityForResult(new Intent(context, AddServerActivity.class), ADD_SERVER_REQUEST);
			}
		});

        listview.addHeaderView(button);
        listview.setAdapter(servers_);
        
        wifi_ = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        lock_ = wifi_.createMulticastLock("fliing_lock");
        lock_.setReferenceCounted(true);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode != ADD_SERVER_REQUEST) {
    		return;
    	}

    	if (resultCode == RESULT_OK) {
    		Bundle extras = data.getExtras();
    		Server server = (Server) extras.get(Server.class.getName());
    		servers_.addServer(server);
    	}
    }
    
    @Override
    public void onResume() {
        lock_.acquire();
        if (mdns_ == null) {
        	Log.d(TAG, "Creating MDNS listener");
	        try {
	        	WifiInfo info = wifi_.getConnectionInfo();
	        	int intaddr = info.getIpAddress();
	        	
	        	byte[] byteaddr = new byte[] { (byte)(intaddr & 0xff), (byte)(intaddr >> 8 & 0xff), (byte)(intaddr >> 16 & 0xff), (byte)(intaddr >> 24 & 0xff) };
	        	InetAddress addr = InetAddress.getByAddress(byteaddr);
	        	
	        	Log.d(TAG, String.format("found intaddr=%d, addr=%s", intaddr, addr.toString()));
	        	
				mdns_ = JmDNS.create(addr, "foobar");
				mdns_.addServiceListener("_clementine._tcp.local.", this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        super.onResume();
    }
    
    @Override
    public void onPause() {
    	lock_.release();
    	super.onPause();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menu_info) {
    	super.onCreateContextMenu(menu, v, menu_info);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.server_context, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	// TODO: Implement remove.
    	return true;
    }


	public void serviceAdded(ServiceEvent event) {
		Log.d(TAG, event.getName());
		// Lookup full TXT record.
		ServiceInfo info = mdns_.getServiceInfo("_clementine._tcp.local.", event.getName());
		Log.d(TAG, info.toString());
		
		String address = info.getHostAddress();
		int port = info.getPort();
		
		servers_.addServer(new Server(address, address, port));
		
		AndroidHttpClient http = AndroidHttpClient.newInstance("Clementine Remote");
		try {
			String response = http.execute(new HttpHost(address, port), new HttpGet("/"), this);
			JSONTokener tokener = new JSONTokener(response);
			JSONObject object;
			object = (JSONObject)tokener.nextValue();
			String content = object.getString("song");
			Log.d(TAG, "Json:" + content);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public void serviceRemoved(ServiceEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void serviceResolved(ServiceEvent event) {
		// TODO Auto-generated method stub
	}

	public String handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		InputStream content = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		String line;
		StringBuilder builder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		return builder.toString();
	}
}