package net.ddns.iztok.tanki;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.sql.Date;

public class ServerActivity extends Activity {

	private Button send;
	private EditText in, server;
	private TextView out;
	private ScrollView serverScroll;
	private String output;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);

		send = findViewById(R.id.send);
		in = findViewById(R.id.in);
		server = findViewById(R.id.server);
		out = findViewById(R.id.out);
		serverScroll = findViewById(R.id.serverScroll);

		clear(send);
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
	}

	public void send(View v) {
		send.setEnabled(false);
		String s = in.getText().toString();
		in.setText("");
		//output += "<span style=\"color: yellow;\">[Uporabnik]</span> <span style=\"color: gray;\">" + s + "</span><br>";
		if (s.toUpperCase().equals("PING")) s = "ping&t=" + System.currentTimeMillis();
		HttpRequest request = new HttpRequest(this) {
			@Override
			public void onResponse(int c, String s) {
				if (c == 0) {
					if (s == null) {  // Strežnik ni dosegljiv
						output += "<span style=\"color: yellow;\">[Tanki]&nbsp;&nbsp;&nbsp;&nbsp;</span> <span style=\"color: red;\">Strežnik ni dosegljiv, poskusite znova kasneje.</span><br>";
					} else { // Timeout
						output += "<span style=\"color: yellow;\">[Tanki]&nbsp;&nbsp;&nbsp;&nbsp;</span> <span style=\"color: red;\">Timeout</span><br>";
					}
				} else if (c != 200) {
					output += "<span style=\"color: yellow;\">[Tanki]&nbsp;&nbsp;&nbsp;&nbsp;</span> <span style=\"color: red;\">Napaka " + c + "</span><br>";
				} else {
					if (s.length() > 0 && !s.split(" ")[0].equals("PING")) {
						output += "<span style=\"color: yellow;\">[Strežnik]&nbsp;</span> " + s + "<br>";
					}
					if (s.split(" ")[0].equals("PING")) {
						long d = System.currentTimeMillis() - Long.parseLong(s.split(" ")[1]);
						output += "<span style=\"color: yellow;\">[Tanki]&nbsp;&nbsp;&nbsp;&nbsp;</span> Trenutni ping je <span style=\"color: aqua;\">" + d + "</span> ms.<br>";
					}
					serverScroll.post(new Runnable() {
						@Override
						public void run() {
							serverScroll.fullScroll(View.FOCUS_DOWN);
						}
					});
				}
				parseHTML();
				send.setEnabled(true);
			}
		};
		request.execute(server.getText().toString() + "/tanki/server.php?action=" + s);
	}

	public void parseHTML() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			out.setText(Html.fromHtml(output, Html.FROM_HTML_MODE_LEGACY));
		} else {
			out.setText(Html.fromHtml(output));
		}
	}

	public void clear(View v) {
		output = "<span style=\"color: yellow;\">[Tanki]&nbsp;&nbsp;&nbsp;&nbsp;</span> Začetek komunikacije s strežnikom<br>";
		parseHTML();
	}

	public void mp(View v) {
		Intent intent = new Intent();
		intent.setClass(this, MultiplayerActivity.class);
		startActivity(intent);
	}
}
