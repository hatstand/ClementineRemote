package com.purplehatstands.clementine.remote;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.purplehatstands.libxrme.RemoteControlInterface;
import com.purplehatstands.libxrme.State;


public class NowPlayingFragment extends Fragment {
  private static final String TAG = NowPlayingFragment.class.getName();
  
  TextView track_;
  TextView artist_;
  TextView album_;
  ImageView album_cover_;
  ImageButton play_;
  ImageButton next_;
  ImageButton prev_;
  private String full_jid_;

  private RemoteControlService service_;
  private RemoteControlInterface remote_control_;
  
  public NowPlayingFragment() {
  }
  
  private void UpdateState(final State state) {
    track_.setText(state.metadata.title);
    artist_.setText(state.metadata.artist);
    album_.setText(state.metadata.album);
    
    int id = (state.playback_state == State.PLAYBACKSTATE_PLAYING) ? R.drawable.media_playback_pause : R.drawable.media_playback_start;
    play_.setImageResource(id);
  }

  private ServiceConnection connection_ = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      service_ = ((RemoteControlService.LocalBinder) service).getService();

      service_
          .AddMediaStateHandler(new RemoteControlService.MediaStateHandler() {
            public void OnStateChanged(final State state) {
              NowPlayingFragment.this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                  UpdateState(state);
                }
              });
            }

            public void OnAlbumArtChanged(final Bitmap image) {
              NowPlayingFragment.this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                  album_cover_.setImageBitmap(image);
                }
              });
            }
          });

      RemoteControlInterface iface = service_.GetRemoteControl();
      iface.QueryState(full_jid_);
      remote_control_ = iface;
    }

    public void onServiceDisconnected(ComponentName className) {
    }
  };
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getActivity().bindService(new Intent(getActivity(), RemoteControlService.class), connection_,
        Activity.BIND_AUTO_CREATE);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.now_playing, container);
    InitialiseUI(view);
    return view;
  }

  private void InitialiseUI(View view) {
    track_ = (TextView) view.findViewById(R.id.track);
    artist_ = (TextView) view.findViewById(R.id.artist);
    album_ = (TextView) view.findViewById(R.id.album);
    album_cover_ = (ImageView) view.findViewById(R.id.album_cover);
    album_cover_.setScaleType(ImageView.ScaleType.FIT_CENTER);
    album_cover_.setAdjustViewBounds(true);
    album_cover_.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nocover));
    
    play_ = (ImageButton) view.findViewById(R.id.play_button);
    next_ = (ImageButton) view.findViewById(R.id.next_button);
    prev_ = (ImageButton) view.findViewById(R.id.previous_button);
    play_.setBackgroundColor(0);
    next_.setBackgroundColor(0);
    prev_.setBackgroundColor(0);
    
    play_.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if (remote_control_ != null) {
          remote_control_.PlayPause(full_jid_);
        }
      }
    });
  }

  @Override
  public void onResume() {
    Intent intent = getActivity().getIntent();
    full_jid_ = (String) intent.getExtras().get("full_jid");
    super.onResume();
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
    getActivity().unbindService(connection_);
  }
}
