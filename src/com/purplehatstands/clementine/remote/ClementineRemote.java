package com.purplehatstands.clementine.remote;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class ClementineRemote extends Activity {
  private RemoteControlService service_;

  private ServiceConnection connection_ = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      service_ = ((RemoteControlService.LocalBinder)service).getService();

      service_.AddConnectionHandler(new RemoteControlService.ConnectionHandler() {
        public void OnDisconnected() {
          Toast.makeText(ClementineRemote.this, "Disconnected from XMPP", Toast.LENGTH_LONG).show();
        }
        
        public void OnConnectionFailure(String message) {
          Toast.makeText(ClementineRemote.this, "Failed to connect to XMPP: " + message, Toast.LENGTH_LONG).show();
        }
        
        public void OnConnected() {
          Intent intent = new Intent(ClementineRemote.this, NowPlayingActivity.class);
          ClementineRemote.this.startActivity(intent);
        }
      });
      
      service_.Connect(ClementineRemote.this);
    }

    public void onServiceDisconnected(ComponentName className) {
    }
  };
  
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    bindService(new Intent(this, RemoteControlService.class), connection_, BIND_AUTO_CREATE);
  }

  protected void onDestroy() {
    unbindService(connection_);
    super.onDestroy();
  }
}
