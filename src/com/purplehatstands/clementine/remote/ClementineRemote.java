package com.purplehatstands.clementine.remote;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ClementineRemote extends Activity implements ServiceListener {
  private static final String TAG = "ClementineRemote";
  static final int ADD_SERVER_REQUEST = 0;

  private MulticastLock lock_;
  private JmDNS mdns_ = null;
  private WifiManager wifi_;
  private ServerListAdapter servers_;

  private XMPPConnection xmpp_;
  
  private String auth_token_;

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

    RegisterC2DM();

    AccountManager manager = AccountManager.get(this);
    Account google_account = manager.getAccountsByType("com.google")[0];
    manager.getAuthToken(google_account, "mail", true, new AccountManagerCallback<Bundle>() {
      public void run(AccountManagerFuture<Bundle> arg0) {
        try {
          Bundle bundle = arg0.getResult();
          if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
            auth_token_ = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            Log.d(TAG, "Auth token:" + auth_token_);
            Connect();
          } else if (bundle.containsKey(AccountManager.KEY_INTENT)) {
            Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
            startActivity(intent);
          }
        } catch (OperationCanceledException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (AuthenticatorException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
      }
    }, null);


    super.onResume();
  }
  
  private void Connect() {
    SASLAuthentication.registerSASLMechanism("X-GOOGLE-TOKEN", GoogleTokenAuthenticator.class);
    
    
    SASLAuthentication.supportSASLMechanism("X-GOOGLE-TOKEN", 0);

    ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
    config.setDebuggerEnabled(false);
    xmpp_ = new XMPPConnection(config);
    try {
      xmpp_.connect();
      xmpp_.login("john.maguire@gmail.com", auth_token_);
      Presence presence = new Presence(Presence.Type.available);
      presence.setStatus("Hello World!");
      xmpp_.sendPacket(presence);
    } catch (XMPPException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private void SendMessage() {
    ChatManager chat_manager = xmpp_.getChatManager();
    Chat chat = chat_manager.createChat("john.maguire@gmail.com/foobar1A6235CD", new MessageListener() {
      public void processMessage(Chat chat, Message message) {
        Log.d(TAG, "Received message:" + message.getBody() + " from:" + message.getFrom());
      }
    });

    try {
      Message message = new Message();
      message.setTo("john.maguire@gmail.com/foobar1A6235CD");
      message.addBody("en", "Lo World!");
      Log.d(TAG, message.toXML());
      chat.sendMessage(message);
    } catch (XMPPException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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

    servers_.addDetectedServer(new Server(address, address, port));
  }

  public void serviceRemoved(ServiceEvent event) {
    // TODO Auto-generated method stub
  }

  public void serviceResolved(ServiceEvent event) {
    // TODO Auto-generated method stub
  }
  
  // C2DM
  private void RegisterC2DM() {
    Log.d(TAG, "RegisterC2DM");
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
  

}
