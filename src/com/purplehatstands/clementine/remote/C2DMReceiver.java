package com.purplehatstands.clementine.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class C2DMReceiver extends BroadcastReceiver {
  private static final String TAG = "C2DMReceiver";

  // C2DM
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "onReceive");
    if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
        HandleRegistration(context, intent);
    } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
        HandleMessage(context, intent);
     }
  }

  // C2DM
  private void HandleRegistration(Context context, Intent intent) {
    String registration = intent.getStringExtra("registration_id"); 
    if (intent.getStringExtra("error") != null) {
        // Registration failed, should try again later.
    } else if (intent.getStringExtra("unregistered") != null) {
        // unregistration done, new messages from the authorized sender will be rejected
    } else if (registration != null) {
       // Send the registration ID to the 3rd party site that is sending the messages.
       // This should be done in a separate thread.
       // When done, remember that all registration is done.
      Log.d(TAG, "C2DM Registration: " + registration);

      SharedPreferences prefs = context.getSharedPreferences("c2dm", Context.MODE_PRIVATE);
      Editor editor = prefs.edit();
      editor.putString("c2dm_reg", registration);
    }
  }
  
  // C2DM
  private void HandleMessage(Context context, Intent intent) {
    String accountName = intent.getExtras().getString("account");
    String message = intent.getExtras().getString("message");
    
    Log.d(TAG, "Message received from: " + accountName);
    Log.d(TAG, "Message: " + message);
  }

}
