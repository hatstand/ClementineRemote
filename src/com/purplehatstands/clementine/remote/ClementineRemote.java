package com.purplehatstands.clementine.remote;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ClementineRemote extends Activity implements AuthTokenReceiver {
  private static final String TAG = "ClementineRemote";
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
        
        public void OnConnected(String full_jid) {
          Intent intent = new Intent(ClementineRemote.this, NowPlayingActivity.class);
          intent.putExtra("full_jid", full_jid);
          ClementineRemote.this.startActivity(intent);
        }
      });
      
      service_.Connect(ClementineRemote.this);
    }

    public void onServiceDisconnected(ComponentName className) {
    }
  };

  @Override
  public void onResume() {
    RegisterC2DM();
    super.onResume();
  }
  
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bindService(new Intent(this, RemoteControlService.class), connection_, BIND_AUTO_CREATE);
  }

  protected void onDestroy() {
    unbindService(connection_);
    super.onDestroy();
  }
  
  // C2DM
  private void RegisterC2DM() {
    Log.d(TAG, "RegisterC2DM");
    // We're gonna need an Appengine auth token soon enough. Do it here as it may prompt the user.
    AccountManager manager = AccountManager.get(this);
    Account google_account = manager.getAccountsByType("com.google")[0];
    GetAuthTokenCallback callback = new GetAuthTokenCallback("ah", this, this);
    manager.getAuthToken(google_account, "ah", true, callback, null);
    
    SharedPreferences prefs = getSharedPreferences("c2dm", MODE_PRIVATE);
    if (prefs.contains("c2dm_reg")) {
      Log.d(TAG, prefs.getString("c2dm_reg", ""));
      return;
    }
    Log.d(TAG, "Sending intent");
    Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
    registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0)); // boilerplate
    registrationIntent.putExtra("sender", "c2dm@clementine-player.org");
    startService(registrationIntent);
  }
  
  public void OnAuthTokenReceived(String service, String token) {
    if (service == "ah") {
      // Don't do anything.
      Log.d(TAG, "Got Appengine auth");
    }
  }
}
