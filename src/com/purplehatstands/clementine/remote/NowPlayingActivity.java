package com.purplehatstands.clementine.remote;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import com.purplehatstands.libxrme.State;

public class NowPlayingActivity extends Activity {
	private static final String TAG = "NowPlayingActivity";
	TextView track_;
	TextView artist_;
	TextView album_;
	ImageView album_cover_;
	MediaController controls_;
	
	private RemoteControlService service_;

  private ServiceConnection connection_ = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      service_ = ((RemoteControlService.LocalBinder)service).getService();

      service_.AddMediaStateHandler(new RemoteControlService.MediaStateHandler() {
        public void OnStateChanged(final State state) {
          NowPlayingActivity.this.runOnUiThread(new Runnable() {
            public void run() {
              track_.setText(state.metadata.title);
              artist_.setText(state.metadata.artist);
              album_.setText(state.metadata.album);
            }
          });
        }
        
        public void OnAlbumArtChanged(final Bitmap image) {
          NowPlayingActivity.this.runOnUiThread(new Runnable() {
            public void run() {
              album_cover_.setImageBitmap(image);
            }
          });
        }
      });
    }

    public void onServiceDisconnected(ComponentName className) {
    }
  };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		InitialiseUI();
		
		bindService(new Intent(this, RemoteControlService.class), connection_, BIND_AUTO_CREATE);
	}
	
	private void InitialiseUI() {
		setContentView(R.layout.now_playing);
		track_ = (TextView) findViewById(R.id.track);
		artist_ = (TextView) findViewById(R.id.artist);
		album_ = (TextView) findViewById(R.id.album);
		album_cover_ = (ImageView) findViewById(R.id.album_cover);
		
		controls_ = new MediaController(this);
		controls_.setAnchorView(findViewById(R.id.now_playing_layout));
		controls_.setMediaPlayer(new Controls());
		controls_.setEnabled(true);
	}
	
	private static class Controls implements MediaPlayerControl {

		public boolean canPause() {
			// TODO Auto-generated method stub
			return true;
		}

		public boolean canSeekBackward() {
			// TODO Auto-generated method stub
			return true;
		}

		public boolean canSeekForward() {
			// TODO Auto-generated method stub
			return true;
		}

		public int getBufferPercentage() {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getCurrentPosition() {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getDuration() {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean isPlaying() {
			// TODO Auto-generated method stub
			return true;
		}

		public void pause() {
			// TODO Auto-generated method stub
			
		}

		public void seekTo(int pos) {
			// TODO Auto-generated method stub
			
		}

		public void start() {
			// TODO Auto-generated method stub
			
		}
		
	}
	

}
