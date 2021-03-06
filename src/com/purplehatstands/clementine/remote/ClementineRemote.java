package com.purplehatstands.clementine.remote;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.purplehatstands.libxrme.PeerDiscoveryInterface.Peer;

public class ClementineRemote extends ListActivity implements AuthTokenReceiver {
  private static final String TAG = "ClementineRemote";
  private RemoteControlService service_;
  
  private ArrayAdapter<Peer> peers_ = null;
  private ServiceConnection connection_ = null;

  @Override
  public void onResume() {
    RegisterC2DM();
    super.onResume();
  }
  
  @Override
  public void onConfigurationChanged(Configuration new_config) {
    super.onConfigurationChanged(new_config);
  }

  
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    connection_ = new ServiceConnection() {
      public void onServiceConnected(ComponentName className, IBinder service) {
        service_ = ((RemoteControlService.LocalBinder)service).getService();
        
        runOnUiThread(new Runnable() {
          public void run() {
            peers_.clear();
            for (Peer p: service_.GetPeers()) {
              peers_.add(p);
            }
          }
        });

        service_.AddConnectionHandler(new RemoteControlService.ConnectionHandler() {
          public void OnDisconnected() {
            Toast.makeText(ClementineRemote.this, "Disconnected from XMPP", Toast.LENGTH_LONG).show();
          }
          
          public void OnConnectionFailure(String message) {
            Toast.makeText(ClementineRemote.this, "Failed to connect to XMPP: " + message, Toast.LENGTH_LONG).show();
          }
          
          public void OnConnected(final Peer peer) {
            Log.d(TAG, "Adding peer: " + peer.toString());
            Log.d(TAG, "foo");
            
            runOnUiThread(new Runnable() {   
              public void run() {
                peers_.add(peer);
              }
            });
          }
        });
      }

      public void onServiceDisconnected(ComponentName name) {
        // TODO Auto-generated method stub
        
      }
    };
    peers_ = new ArrayAdapter<Peer>(this, android.R.layout.simple_list_item_1);
    bindService(new Intent(this, RemoteControlService.class), connection_, BIND_AUTO_CREATE);
    startService(new Intent(this, RemoteControlService.class));
    setListAdapter(peers_);
  }
  
  protected void onListItemClick(ListView l, View v, int position, long id) {
    Peer peer = peers_.getItem(position);
    Intent intent = new Intent(ClementineRemote.this, NowPlayingActivity.class);
    intent.putExtra("full_jid", peer.full_jid_);
    intent.putExtra("identity", peer.identity_);
    ClementineRemote.this.startActivity(intent);
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
