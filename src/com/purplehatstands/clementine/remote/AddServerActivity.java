package com.purplehatstands.clementine.remote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddServerActivity extends Activity {
	
	private Button ok_;
	private Button cancel_;
	private EditText server_name_;
	private EditText address_;
	private EditText port_;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add);
		
		ok_ = (Button) findViewById(R.id.ok);
		cancel_ = (Button) findViewById(R.id.cancel);
		server_name_ = (EditText) findViewById(R.id.name);
		address_ = (EditText) findViewById(R.id.address);
		port_ = (EditText) findViewById(R.id.port);
		
		ok_.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				try {
					String name = server_name_.getText().toString();
					String address = address_.getText().toString();
					int port = Integer.parseInt(port_.getText().toString());
					
					Intent data = new Intent();
					data.putExtra(Server.class.getName(), new Server(name, address, port));
					setResult(RESULT_OK, data);
					finish();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
