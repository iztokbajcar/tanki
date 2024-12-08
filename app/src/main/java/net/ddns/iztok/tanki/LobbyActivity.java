package net.ddns.iztok.tanki;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.ByteArrayInputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class LobbyActivity extends Activity {

	private static HttpRequest connTest, newGameRequest, newPlayerRequest, quitRequest, joinRequest, startRequest;
	private String serverName, lobbyName, playerName;
	private byte playerNum, playerCount, readyCount;
	private LinearLayout l;
	private TextView p1, p2, p3, p4, p5, p6, p7, p8, p9, server;
	private Switch ready;
	private Button start;

	private long lastBackPressed;
	private byte numBackPressed = 0;

	private static byte NUM_OF_PLAYERS = 9;

	private Handler handler = new Handler();
	private Runnable lobbyRunnable;

	@Override
	public void onBackPressed() {
		if (numBackPressed == 1 && System.currentTimeMillis() - lastBackPressed < 1000) {
			quit();
			super.onBackPressed();
		} else {
			numBackPressed = 1;
			lastBackPressed = System.currentTimeMillis();
			Toast.makeText(this, R.string.tap_again_to_exit, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		quit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);

		l = findViewById(R.id.root);
		p1 = findViewById(R.id.p1);
		p2 = findViewById(R.id.p2);
		p3 = findViewById(R.id.p3);
		p4 = findViewById(R.id.p4);
		p5 = findViewById(R.id.p5);
		p6 = findViewById(R.id.p6);
		p7 = findViewById(R.id.p7);
		p8 = findViewById(R.id.p8);
		p9 = findViewById(R.id.p9);
		server = findViewById(R.id.server);

		ready = findViewById(R.id.ready);
		ready.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				ready(b);
			}
		});

		start = findViewById(R.id.start);

		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

		Intent intent = getIntent();
		if (intent.getStringExtra("serverName") == null) {
			// Igralec bo ustvaril novo igro
			queryServer();
		} else {
			// Igralec se bo pridružil igri
			serverName = intent.getStringExtra("serverName");
			lobbyName = intent.getStringExtra("lobbyName");
			connTest = new HttpRequest(LobbyActivity.this) {
				@Override
				public void onResponse(int responseCode, String result) {
					if (responseCode == 0) {
						if (result == null) {
							noConnection();
						} else if (result.equals("\0")) {
							timeout();
						}
					} else if (responseCode != 200) {
						serverError(responseCode);
					} else {
						if (result.equals("NOOP")) {
							Toast.makeText(LobbyActivity.this, getResources().getString(R.string.conn_succesful), Toast.LENGTH_SHORT).show();
							server.post(new Runnable() {
								@Override
								public void run() {
									server.setText(getResources().getString(R.string.server) + serverName);
								}
							});
							joinLobby(lobbyName);
						} else {
							serverError(-1);
						}
					}
				}
			};
			connTest.execute(serverName + "/tanki/server.php?action=noop");
		}
	}

	private void queryServer() {
		// Vpraša po serverju
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.server_address_title));

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10, 10, 10, 10);
		final TextView text = new TextView(this);
		final EditText serverInput = new EditText(this);
		text.setText(getResources().getString(R.string.server_address_text));
		layout.addView(text);
		layout.addView(serverInput);
		builder.setView(layout);
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				serverName = serverInput.getText().toString();
				if (serverName.equals(""))
					serverName = "https://sc.iztokbajcar.tk";

				connTest = new HttpRequest(LobbyActivity.this) {
					@Override
					public void onResponse(int responseCode, String result) {
						if (responseCode == 0) {
							if (result == null) {
								noConnection();
							} else if (result.equals("\0")) {
								timeout();
							}
						} else if (responseCode != 200) {
							serverError(responseCode);
						} else {
							if (result.equals("NOOP")) {
								Toast.makeText(LobbyActivity.this, getResources().getString(R.string.conn_succesful), Toast.LENGTH_SHORT).show();
								server.post(new Runnable() {
									@Override
									public void run() {
										server.setText(getResources().getString(R.string.server) + serverName);
									}
								});
								queryGameName();
							} else {
								serverError(-1);
							}
						}
					}
				};
				connTest.execute(serverName + "/tanki/server.php?action=noop");

				// Poskuša prenesti sliko ozadja in s tem tudi preveri dosegljivost strežnika
				/*bgRequest = new HttpRequest(LobbyActivity.this) {
					@Override
					public void onResponse(int responseCode, String result) {
						if (responseCode == 0) {
							if (result == null) {
								noConnection();
							} else if (result.equals("\0")) {
								timeout();
							}
						} else if (responseCode != 200) {
							serverError(responseCode);
						} else {
							ByteArrayInputStream s = new ByteArrayInputStream(result.getBytes());
							Bitmap bg = BitmapFactory.decodeStream(s);
							l.setBackground(new BitmapDrawable(getResources(), bg));
							server.post(new Runnable() {
								@Override
								public void run() {
									server.setText(getResources().getString(R.string.server) + serverName);
								}
							});
							queryGameName();
						}
					}
				};
				bgRequest.execute(serverName + "/tanki/bg.bmp");*/
			}
		});
		builder.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
				finish();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private void queryGameName() {
		// Vpraša za ime igre
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.lobby_name_title));

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10, 10, 10, 10);
		final TextView text = new TextView(this);
		final EditText lobbyInput = new EditText(this);
		text.setText(getResources().getString(R.string.lobby_name_text));
		layout.addView(text);
		layout.addView(lobbyInput);
		builder.setView(layout);
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				lobbyName = lobbyInput.getText().toString();

				newGameRequest = new HttpRequest(LobbyActivity.this) {
					@Override
					public void onResponse(int responseCode, String result) {
						if (result == null) {
							noConnection();
						} else if (!result.equals("\0")) {
							if (responseCode != 200) {
								serverError(responseCode);
							} else {
								String[] a = result.split(" ");
								if (a[0].equals("N")) {
									if (a[1].equals("0")) {
										gameFail();
									} else {
										queryPlayerName();
									}
								}
							}
						} else {
							timeout();
						}
					}
				};
				newGameRequest.execute(serverName + "/tanki/server.php?action=new&n=" + lobbyName);
			}
		});
		builder.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
				quit();
				finish();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private void queryPlayerName() {
		// Vpraša za ime igralca
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.player_name_title));

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10, 10, 10, 10);
		final TextView text = new TextView(this);
		final EditText playerInput = new EditText(this);
		text.setText(getResources().getString(R.string.player_name_text));
		layout.addView(text);
		layout.addView(playerInput);
		builder.setView(layout);
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				playerName = playerInput.getText().toString();
				newPlayerRequest = new HttpRequest(LobbyActivity.this) {
					@Override
					public void onResponse(int responseCode, String result) {
						if (result == null) {
							noConnection();
						} else if (!result.equals("\0")) {
							if (responseCode != 200) {
								serverError(responseCode);
							} else {
								String[] a = result.split(" ");
								Log.w("Tanki", a[1]);
								if (a[0].equals("P")) {
									if (a[1].equals("-1")) {
										Log.w("Tanki", "playerFail()");
										playerFail();
									} else {
										playerNum = Byte.parseByte(a[1]);
										// Nadaljuje
										init(Integer.parseInt(a[1]));
									}
								} else {
									serverError(0);
								}
							}
						} else {
							timeout();
						}
					}
				};
				newPlayerRequest.execute(serverName + "/tanki/server.php?action=pl&n=" + playerName);
			}
		});
		builder.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
				quit();
				finish();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private void joinLobby(String lobby) {
		joinRequest = new HttpRequest(LobbyActivity.this) {
			@Override
			public void onResponse(int responseCode, String result) {
				if (responseCode == 0) {
					if (result == null) {
						noConnection();
					} else if (result.equals("\0")) {
						timeout();
					}
				} else if (responseCode != 200) {
					serverError(responseCode);
				} else {
					if (result.equals("J 1")) {
						queryPlayerName();
					} else {
						serverError(-1);
					}
				}
			}
		};
		joinRequest.execute(serverName + "/tanki/server.php?action=join&n=" + lobbyName);
	}

	private void noConnection() {
		// Strežnik ni dosegljiv
		final AlertDialog.Builder builder  = new AlertDialog.Builder(LobbyActivity.this);
		builder.setTitle(getResources().getString(R.string.server_unreachable_title));
		builder.setMessage(getResources().getString(R.string.server_unreachable_text));
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
				queryServer();
			}
		});
		builder.show();
	}

	private void timeout() {
		/*final AlertDialog.Builder builder  = new AlertDialog.Builder(LobbyActivity.this);
		builder.setTitle(getResources().getString(R.string.server_timeout_title));
		builder.setMessage(getResources().getString(R.string.server_timeout_text));
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
			}
		});
		builder.show();*/
		Toast.makeText(this, getResources().getString(R.string.server_timeout_text), Toast.LENGTH_SHORT).show();
	}

	private void serverError(int errorCode) {
		// Strežnik vrača kodo napake, ki ni 200
		final AlertDialog.Builder builder  = new AlertDialog.Builder(LobbyActivity.this);
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

	private void gameFail() {
		// Napačno ime igre ali pa strežnik ne more ustvariti mape
		final AlertDialog.Builder builder  = new AlertDialog.Builder(LobbyActivity.this);
		builder.setTitle(getResources().getString(R.string.game_error_title));
		builder.setMessage(getResources().getString(R.string.game_error_text));
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
				queryGameName();  // Še enkrat vpraša za ime sobe
			}
		});
		builder.show();
	}

	private void playerFail() {
		// Napačno ime igralca
		final AlertDialog.Builder builder  = new AlertDialog.Builder(LobbyActivity.this);
		builder.setTitle(getResources().getString(R.string.player_error_title));
		builder.setMessage(getResources().getString(R.string.player_error_text));
		builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
				queryPlayerName();  // Še enkrat vpraša za ime igralca
			}
		});
		builder.show();
	}

	private void quit() {
		// Igralec je zapustil sobo
		quitRequest = new HttpRequest(this) {
			@Override
			public void onResponse(int responseCode, String result) {

			}
		};
		quitRequest.execute("http://sc.iztokbajcar.ml/tanki/server.php?action=quit");
	}

	private void init(int pNum) {
		Toast.makeText(this, playerName + " (" + pNum + ")", Toast.LENGTH_SHORT).show();
		showPlayer(pNum, playerName, false);

		lobbyRunnable = new Runnable() {
			@Override
			public void run() {
				HttpRequest lobbyRequest = new HttpRequest(LobbyActivity.this) {
					@Override
					public void onResponse(int responseCode, String result) {
						if (result == null) {
							noConnection();
						} else if (!result.equals("\0")) {
							if (responseCode != 200) {
								serverError(responseCode);
							} else {
								String[] a = result.split(" ");
								if (a[0].equals("LB")) {
									readyCount = 0;
									playerCount = 0;
									for (int i = 0; i < NUM_OF_PLAYERS; i++) {
										if (!a[i + 1].equals("")) {
											playerCount++;
											showPlayer(i + 1, a[i + 1], Boolean.parseBoolean(a[i + NUM_OF_PLAYERS + 1]));
										}
									}
									Log.w("Tanki", "playerCount: " + playerCount);
									Log.w("Tanki", "readyCount: " + readyCount);
									if (playerNum == 1) {
										if (readyCount == playerCount) {
											start.setEnabled(true);
											if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
												start.setBackground(getResources().getDrawable(R.drawable.gumb, null));
											} else {
												start.setBackground(getResources().getDrawable(R.drawable.gumb));
											}
										} else {
											start.setEnabled(false);
											if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
												start.setBackground(getResources().getDrawable(R.drawable.gumb_off, null));
											} else {
												start.setBackground(getResources().getDrawable(R.drawable.gumb_off));
											}
										}
									}
								}
							}
						} else {
							timeout();
						}
					}
				};
				lobbyRequest.execute(serverName + "/tanki/server.php?action=lb");
				handler.postDelayed(lobbyRunnable, 100);
			}
		};
		handler.post(lobbyRunnable);
		Log.w("Tanki", "Init ok: " + pNum);
	}

	private void ready(boolean b) {
		HttpRequest rdyRequest = new HttpRequest(LobbyActivity.this) {
			@Override
			public void onResponse(int responseCode, String result) {
				if (result == null) {
					noConnection();
				} else if (!result.equals("\0")) {
					if (responseCode != 200) {
						serverError(responseCode);
					}
				} else {
					timeout();
				}
			}
		};
		rdyRequest.execute(serverName + "/tanki/server.php?action=rdy&n=" + b);
	}

	private void showPlayer(int pNum, String pName, boolean ready) {
		TextView[] ta = {p1, p2, p3, p4, p5, p6, p7, p8, p9};
		TextView tv = ta[pNum - 1];
		String text = "(" + pNum + ") " + pName;
		if (ready) {
			text += "&nbsp;<span style=\"color: aqua;\">" + getResources().getString(R.string.player_ready) + "</span>";
			readyCount++;
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			tv.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
		} else {
			tv.setText(Html.fromHtml(text));
		}
	}

	public void startButton(View v) {
		// TODO Treba bo dobiti število igralcev (morata biti vsaj 2), vsi igralci pa morajo biti pripravljeni na igro
		if (readyCount == playerCount) {
			startGame();
		}
	}

	public void startGame() {
		startRequest = new HttpRequest(LobbyActivity.this) {
			@Override
			public void onResponse(int responseCode, String result) {
				if (responseCode == 0) {
					if (result == null) {
						noConnection();
					} else if (result.equals("\0")) {
						timeout();
					}
				} else if (responseCode != 200) {
					serverError(responseCode);
				} else {
					if (result.equals("J 1")) {
						Intent intent = new Intent();
						intent.setClass(LobbyActivity.this, GameActivity.class);
						intent.putExtra("onlinePlay", true);
						intent.putExtra("playerNum", playerNum);

						// Prekliče lobby requeste
						handler.removeCallbacks(lobbyRunnable);

						startActivity(intent);
					} else {
						Toast.makeText(LobbyActivity.this, getResources().getString(R.string.start_error_text), Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
		startRequest.execute(serverName + "/tanki/server.php?action=st");
	}

}
