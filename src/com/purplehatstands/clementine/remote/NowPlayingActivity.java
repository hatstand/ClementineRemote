package com.purplehatstands.clementine.remote;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class NowPlayingActivity extends FragmentActivity {
  private static final String TAG = "NowPlayingActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.now_playing_phone);
  }
}
