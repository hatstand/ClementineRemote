package com.purplehatstands.clementine.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class C2DMReceiver extends BroadcastReceiver {
  private static final String TAG = "C2DMReceiver";

  // C2DM
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "onReceive");
    C2DMService.runIntentInService(context, intent);
  }

 

}
