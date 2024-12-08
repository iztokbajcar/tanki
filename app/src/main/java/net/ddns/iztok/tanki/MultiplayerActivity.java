package net.ddns.iztok.tanki;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.Arrays;

public class MultiplayerActivity extends Activity {

	private HttpRequest listRequest;

	private Button joinButton, refreshButton;
	private ListView gameList;
	private TextView status;
	private String[] games;
	private int selected = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multiplayer);

		status = findViewById(R.id.status);
		joinButton = findViewById(R.id.join);
		refreshButton = findViewById(R.id.refresh);

		gameList = findViewById(R.id.gameList);
		gameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				joinButton.setEnabled(true);
				select(i);
			}
		});

		refresh(refreshButton);

	}

	public void refresh(View v) {
		// Sprazni seznam
		gameList.setAdapter(null);
		// Preveri za igre na strežniku
		listRequest = new HttpRequest(MultiplayerActivity.this) {
			@Override
			public void onResponse(int responseCode, String result) {
				if (responseCode == 0) {
					if (result == null) {
						status.post(new Runnable() {
							@Override
							public void run() {
								status.setText(getApplicationContext().getResources().getString(R.string.status_noconnection));
							}
						});
					} else if (result.equals("\0")) {
						status.post(new Runnable() {
							@Override
							public void run() {
								status.setText(getApplicationContext().getResources().getString(R.string.status_timeout));
							}
						});
					}
				} else if (responseCode != 200) {
					serverError(responseCode);
				} else {
					String[] a = result.split(" ");
					if (a[0].equals("LIST")) {
						status.post(new Runnable() {
							@Override
							public void run() {
								status.setText(getApplicationContext().getResources().getString(R.string.status_ok));
							}
						});
						refreshButton.setEnabled(true);
						for (int i = 1; i < a.length; i++) {
							Log.w("Tanki", "Igra: " + a[i]);
							games = Arrays.copyOfRange(a, 1, a.length);
							ArrayAdapter<String> adapter = new ArrayAdapter<String>(MultiplayerActivity.this, android.R.layout.simple_list_item_1, games);
							gameList.setAdapter(adapter);

							if (games.length > 0) {
								//select(0);
							}
						}
					} else {
						serverError(-1);
					}
				}
			}
		};
		listRequest.execute("https://sc.iztokbajcar.tk/tanki/server.php?action=list");
		refreshButton.setEnabled(false);
	}

	private void select(int pos) {
		gameList.getChildAt(selected).setBackgroundColor(Color.TRANSPARENT);
		gameList.getChildAt(pos).setBackgroundColor(Color.CYAN);
		Log.w("Tanki", "Dolžina (adapter): " + gameList.getAdapter().getCount());
		selected = pos;
	}

	private void noConnection() {
		// Strežnik ni dosegljiv
		/*final AlertDialog.Builder builder  = new AlertDialog.Builder(MultiplayerActivity.this);
		builder.setTitle(getResources().getString(R.string.server_unreachable_title));
		builder.setMessage(getResources().getString(R.string.server_unreachable_text));
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
			}
		});
		builder.show();*/
	}

	private void serverError(int errorCode) {
		// Strežnik vrača kodo napake, ki ni 200
		final AlertDialog.Builder builder  = new AlertDialog.Builder(MultiplayerActivity.this);
		builder.setTitle(getResources().getString(R.string.server_error_title, errorCode));
		builder.setMessage(getResources().getString(R.string.server_error_text));
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
				finish();
			}
		});
		builder.show();
	}

	// TODO premakni to v glavni meni ali v posebej aktivnost za multiplayer
	public void host(View v) {
		Intent intent = new Intent();
		intent.setClass(this, LobbyActivity.class);
		startActivity(intent);
	}

	// Pridruži se izbrani igri na seznamu
	public void join(View v) {
		Toast.makeText(this, "Igra: " + games[selected], Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.setClass(this, LobbyActivity.class);
		intent.putExtra("serverName", "https://sc.iztokbajcar.ml");  // TODO podpora za več strežnikov
		intent.putExtra("lobbyName", games[selected]);
		startActivity(intent);
	}
}
