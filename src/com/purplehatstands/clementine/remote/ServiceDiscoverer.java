package com.purplehatstands.clementine.remote;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import android.util.Log;
import android.widget.TextView;

public class ServiceDiscoverer implements ServiceListener {
	
	ServiceDiscoverer(TextView tv) {
		tv_ = tv;
	}

	public void serviceAdded(ServiceEvent event) {
		Log.d("FOO", event.toString());
		tv_.setText("added:" + event.getName());
	}

	public void serviceRemoved(ServiceEvent event) {
		Log.d("FOO", event.toString());
		tv_.setText("removed:" + event.getName());

	}

	public void serviceResolved(ServiceEvent event) {
		Log.d("FOO", event.toString());
		tv_.setText("resolved:" + event.getName());
	}
	
	private TextView tv_;

}
