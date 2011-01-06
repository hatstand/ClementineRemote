package com.purplehatstands.clementine.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class NowPlayingActivity extends Activity {
	private static final String TAG = "NowPlayingActivity";
	TextView track_;
	
	private class JsonFetcher extends AsyncTask<Server, Integer, JSONObject> implements ResponseHandler<String> {

		@Override
		protected JSONObject doInBackground(Server... params) {
			Server server = params[0];
			AndroidHttpClient http = AndroidHttpClient.newInstance("Clementine Remote");
			String response;
			try {
				response = http.execute(new HttpHost(server.getAddress(), server.getPort()), new HttpGet("/"), this);
				JSONTokener tokener = new JSONTokener(response);
				JSONObject object;
				object = (JSONObject)tokener.nextValue();
				return object;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new JSONObject();
		}
		
		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				JSONObject song = json.getJSONObject("song");
				TextView track = (TextView) findViewById(R.id.track);
				track.setText(song.getString("title"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public String handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			InputStream content = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			return builder.toString();
		}
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.now_playing);
		track_ = (TextView) findViewById(R.id.track);
		
		Intent intent = getIntent();
		Server server = (Server) intent.getExtras().get("server");
		
		new JsonFetcher().execute(server);
	}

	

}
