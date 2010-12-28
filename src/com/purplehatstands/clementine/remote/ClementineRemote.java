package com.purplehatstands.clementine.remote;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ClementineRemote extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv_ = new TextView(this);
        tv_.setText("Hello, World!");
        setContentView(tv_);
        
        wifi_ = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        lock_ = wifi_.createMulticastLock("fliing_lock");
        lock_.setReferenceCounted(true);
    }
    
    @Override
    public void onResume() {
        lock_.acquire();
        if (mdns_ == null) {
        	Log.d("FOO", "Creating MDNS listener");
	        try {
	        	WifiInfo info = wifi_.getConnectionInfo();
	        	int intaddr = info.getIpAddress();
	        	
	        	byte[] byteaddr = new byte[] { (byte)(intaddr & 0xff), (byte)(intaddr >> 8 & 0xff), (byte)(intaddr >> 16 & 0xff), (byte)(intaddr >> 24 & 0xff) };
	        	InetAddress addr = InetAddress.getByAddress(byteaddr);
	        	
	        	Log.d("FOO", String.format("found intaddr=%d, addr=%s", intaddr, addr.toString()));
	        	
				mdns_ = JmDNS.create(addr, "foobar");
				mdns_.addServiceListener("_clementine._tcp.local.", new ServiceDiscoverer(tv_));
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
    
    private MulticastLock lock_;
    private TextView tv_;
    private JmDNS mdns_ = null;
    private WifiManager wifi_;
}