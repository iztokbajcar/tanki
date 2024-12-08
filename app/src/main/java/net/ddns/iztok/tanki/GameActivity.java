package net.ddns.iztok.tanki;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends Activity {

	private boolean konecIgre = false;
	private boolean tankUstrelil = false;
	private Bitmap bmp, ozadje, ozadje1, ozadje2, ozadje3, ozadje4, ozadje5, oblak1, oblak2, oblak3, oblakiBmp, teren, ospredje, coll, topovi, flashBmp, top;
	private Button streljaj, topL, topD, mocG, mocD, orozjeL, orozjeD;
	private Canvas can, ozadjeCan, oblakiCan, terenCan, ospredjeCan, collCan, topoviCan, flashCan, topCan;
	private Handler handler;
	private ImageView iv, ivOrozje;
	private int barvaTerena, barvaTeksture;
	private int nivoTerena;
	private int spremembaSmeriVetra = 0;  // Koliko krogov je minilo od zadnje spremembe smeri vetra
	private int odNovegaOblaka = 0;  // Koliko ciklov je monilo od zadnjega novega oblaka
	//private Izstrelek izstrelek = null;
	private Matrix matrix;
	private Paint paint;
	private LinearLayout ui;
	private Runnable drawRunnable;
	private TextView hp, moc, rot, orozje, igralec, textVeter, igr1, igr2, igr3, igr4, igr5, igr6, igr7, igr8, igr9;

	// Nastavitve
	private int tip1, tip2, tip3, tip4, tip5, tip6, tip7, tip8, tip9;     // Tip umetne inteligence
	private int c1, c2, c3, c4, c5, c6, c7, c8, c9;                 // Barva igralca
	private int mon1, mon2, mon3, mon4, mon5, mon6;     // Denar
	private int velikostZrn;
	private String ime1, ime2, ime3, ime4, ime5, ime6, ime7, ime8, ime9;  // Ime igralca
	private boolean soOblaki;
	private boolean hribcki;
	private boolean terenTekstura;
	private boolean tankPoskodba;
	private boolean tankPrikazHP;
	private boolean tankBomba;
	private boolean tankBlisk;
	private boolean bombaBlisk;
	private boolean tankIzgled;
	private boolean partikliOn;
	private boolean aiMenjaOrozje;
	private boolean zvok;
	private int ozadjeId, terenId, teksturaId, aiOrozjeId, tankiHPId;

	// Stanja gumbov
	private boolean topLDol = false;
	private boolean topDDol = false;
	private boolean mocGDol = false;
	private boolean mocDDol = false;
	//private boolean leti = false;

	private long lastBackPressed;
	private byte numBackPressed = 0;

	private static int bmpWidth = 1200;
	private static int bmpHeight = 720;
	private static int drawDelay = 0;
	private static int tankHeight = 20;

	private ArrayList<Integer> barve = new ArrayList<Integer>();
	private ArrayList<String> imena = new ArrayList<>();
	private ArrayList<Tank> tanki = new ArrayList<Tank>();
	private ArrayList<Boolean> tankiNaTleh = new ArrayList<Boolean>();
	private ArrayList<Izstrelek> izstrelki = new ArrayList<>();
	private ArrayList<Particle> partikli = new ArrayList<>();
	private ArrayList<Oblak> oblaki = new ArrayList<>();

	private ArrayList<Bitmap> orozjaSlike = new ArrayList<>();
	private ArrayList<Bitmap> ozadja = new ArrayList<>();
	private ArrayList<Bitmap> oblakiSlike = new ArrayList<>();
	private ArrayList<Integer> barveTerena = new ArrayList<>();
	private ArrayList<Integer> barveTeksture = new ArrayList<>();
	private ArrayList<Integer> tankiHP = new ArrayList<>();

	private double grav;
	private int naVrsti = 0;
	private int crkeAlpha = 0;
	private int veter = 0;
	private boolean negativenVeter = false;

	private int flash; // Blisk ekrana (in fadeout)

	// Zvok
	private SoundPool sp;
	private int[] zvoki;
	private int soundpack = 0;  // TODO soundpacki

	// Online
	private boolean onlinePlay = false;
	private byte playerNum;

	@Override
	public void onBackPressed() {
		if (numBackPressed == 1 && System.currentTimeMillis() - lastBackPressed < 1000) {
			super.onBackPressed();
			handler.removeCallbacks(drawRunnable);
			finish();
		} else {
			numBackPressed = 1;
			lastBackPressed = System.currentTimeMillis();
			Toast.makeText(this, R.string.tap_again_to_exit, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			iv.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(drawRunnable);
	}

	@Override
	protected void onResume() {
		super.onResume();
		handler.post(drawRunnable);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		lastBackPressed = System.currentTimeMillis();

		// INICIALIZACIJA
		onlinePlay = getIntent().getBooleanExtra("onlinePlay", false);
		playerNum = getIntent().getByteExtra("playerNum", (byte)1);

		// Pridobi nastavitve za igro
		loadSettings("net.ddns.iztok.tanki");

		// Naloži zvoke
		if (zvok) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  // Pred Lollipop ima SoundPool drugačen konstruktor
				sp = new SoundPool.Builder().setMaxStreams(10).build();
			} else {
				sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			}

			zvoki = new int[4];
			switch (soundpack) {
				default:
					zvoki[0] = sp.load(this, R.raw.bomb, 1);
					zvoki[1] = sp.load(this, R.raw.fire, 1);
					zvoki[2] = sp.load(this, R.raw.tankboom, 2);
					zvoki[3] = sp.load(this, R.raw.tankboom2, 2);
					break;
				case 1:
					zvoki[0] = sp.load(this, R.raw.bomb_8bit, 1);
					zvoki[1] = sp.load(this, R.raw.fire_8bit, 1);
					zvoki[2] = sp.load(this, R.raw.tankboom_8bit, 2);
					zvoki[3] = sp.load(this, R.raw.tankboom2_8bit, 2);
					break;
			}

		}

		barve.add(Color.rgb(255, 0, 0));
		barve.add(Color.rgb(255, 255, 0));
		barve.add(Color.rgb(0, 255, 0));
		barve.add(Color.rgb(0, 255, 255));
		barve.add(Color.rgb(0, 0, 255));
		barve.add(Color.rgb(255, 0, 255));

		ui = findViewById(R.id.ui);

		// SEZNAMI
		// Naloži potrebne slike
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_ball));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_bigball));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_explosive));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_ball3));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_terrain));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_cluster));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_dblcluster));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_dblterraincluster));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_thermo20));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_thermo50));
		orozjaSlike.add(BitmapFactory.decodeResource(getResources(), R.drawable.w_obliterator));

		ozadje1 = BitmapFactory.decodeResource(getResources(), R.drawable.ozadje1);
		ozadje2 = BitmapFactory.decodeResource(getResources(), R.drawable.ozadje2);
		ozadje3 = BitmapFactory.decodeResource(getResources(), R.drawable.ozadje3);
		ozadje4 = BitmapFactory.decodeResource(getResources(), R.drawable.ozadje4);
		ozadja.add(ozadje1);
		ozadja.add(ozadje2);
		ozadja.add(ozadje3);
		ozadja.add(ozadje4);
		ozadje = ozadja.get(ozadjeId);

		oblak1 = BitmapFactory.decodeResource(getResources(), R.drawable.oblak1);
		oblak2 = BitmapFactory.decodeResource(getResources(), R.drawable.oblak2);
		oblak3 = BitmapFactory.decodeResource(getResources(), R.drawable.oblak3);
		oblakiSlike.add(oblak1);
		oblakiSlike.add(oblak2);
		oblakiSlike.add(oblak3);

		barveTerena.add(Color.rgb(56, 126, 0));
		barveTerena.add(Color.rgb(76, 126, 4));
		barveTerena.add(Color.rgb(95, 126, 0));
		barveTerena.add(Color.rgb(128, 128, 0));
		barveTerena.add(Color.rgb(128, 128, 128));
		barveTerena.add(Color.rgb(94, 52, 0));
		barveTerena.add(Color.rgb(255, 255, 255));
		barveTerena.add(Color.rgb(198, 166, 100));
		barvaTerena = barveTerena.get(terenId);

		barveTeksture.add(Color.rgb(94, 52, 0));
		barveTeksture.add(Color.rgb(128, 128, 128));
		barveTeksture.add(Color.rgb(128, 64, 0));
		barveTeksture.add(Color.rgb(200, 255, 255));
		barvaTeksture = barveTeksture.get(teksturaId);

		tankiHP.add(50);
		tankiHP.add(100);
		tankiHP.add(150);
		tankiHP.add(200);
		tankiHP.add(250);
		tankiHP.add(500);
		tankiHP.add(750);
		tankiHP.add(1000);
		tankiHP.add(10000);

		// GUMBI
		streljaj = findViewById(R.id.streljaj);
		topL = findViewById(R.id.topLevo);
		topD = findViewById(R.id.topDesno);
		mocG = findViewById(R.id.mocGor);
		mocD = findViewById(R.id.mocDol);
		orozjeL = findViewById(R.id.orozjeNazaj);
		orozjeD = findViewById(R.id.orozjeNaprej);

		//naprej = findViewById(R.id.naprej);

		topL.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						topLDol = true;
						break;
					case MotionEvent.ACTION_UP:
						topLDol = false;
						break;
				}
				return true;
			}
		});
		topD.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						topDDol = true;
						break;
					case MotionEvent.ACTION_UP:
						topDDol = false;
						break;
				}
				return true;
			}
		});
		mocG.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mocGDol = true;
						break;
					case MotionEvent.ACTION_UP:
						mocGDol = false;
						break;
				}
				return true;
			}
		});
		mocD.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mocDDol = true;
						break;
					case MotionEvent.ACTION_UP:
						mocDDol = false;
						break;
				}
				return true;
			}
		});
		orozjeL.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_UP:  // Orožje se zamenja šele, ko igralec spusti gumb
						orozjeNazaj();
						break;
				}
				return true;
			}
		});
		orozjeD.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_UP:  // Orožje se zamenja šele, ko igralec spusti gumb
						orozjeNaprej();
						break;
				}
				return true;
			}
		});

		// NAPISI
		hp = findViewById(R.id.hp);
		moc = findViewById(R.id.moc);
		rot = findViewById(R.id.rotacija);
		orozje = findViewById(R.id.orozje);

		igralec = findViewById(R.id.igralec);
		igr1 = findViewById(R.id.igr1);
		igr2 = findViewById(R.id.igr2);
		igr3 = findViewById(R.id.igr3);
		igr4 = findViewById(R.id.igr4);
		igr5 = findViewById(R.id.igr5);
		igr6 = findViewById(R.id.igr6);
		igr7 = findViewById(R.id.igr7);
		igr8 = findViewById(R.id.igr8);
		igr9 = findViewById(R.id.igr9);

		textVeter = findViewById(R.id.veter);
		iv = (ImageView)findViewById(R.id.iv);
		ivOrozje = findViewById(R.id.iv_orozje);

		//if (!jeVeter) textVeter.setVisibility(View.INVISIBLE);
		if (!tankPoskodba) hp.setVisibility(View.INVISIBLE);

		bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		oblakiBmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		teren = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		ospredje = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		coll = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		topovi = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		flashBmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);

		can = new Canvas(bmp);
		oblakiCan = new Canvas(oblakiBmp);
		terenCan = new Canvas(teren);
		ospredjeCan = new Canvas(ospredje);
		collCan = new Canvas(coll);
		topoviCan = new Canvas(topovi);
		flashCan = new Canvas(flashBmp);

		paint = new Paint();

		// Slika topa tankov
		top = Bitmap.createBitmap(tankHeight / 2, tankHeight, Bitmap.Config.ARGB_8888);
		topCan = new Canvas(top);
		paint.setColor(Color.BLACK);
		topCan.drawRect(new Rect(0, 0, tankHeight / 2, tankHeight / 5), paint);
		topCan.drawRect(new Rect(tankHeight / 10, tankHeight / 5, 4 * tankHeight / 10, tankHeight), paint);

		matrix = new Matrix();
		flash = 0;

		handler = new Handler();
		drawRunnable = new Runnable() {
			@Override
			public void run() {

				// TODO Antialiasing?

				// Počisti vse plasti
				can.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				oblakiCan.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				collCan.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				ospredjeCan.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				topoviCan.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

				if (tanki.size() <= naVrsti) {
					naVrsti = tanki.size() - 1;
				}

				// Izriše tanke
				// TODO dodaj stil tankov
				for (int i = 0; i < tanki.size(); i++) {
					Tank t = tanki.get(i);
					if (tankIzgled) {
						paint.setStyle(Paint.Style.FILL);
						paint.setColor(t.c);
						// GOSENICE
						ospredjeCan.drawRoundRect(new RectF(t.x + tankHeight / 2, t.y - tankHeight / 4, t.x + tankHeight * 2 - tankHeight / 2, t.y + tankHeight / 2), 5.0f, 5.0f, paint);
						paint.setStyle(Paint.Style.STROKE);
						paint.setColor(Color.BLACK);
						ospredjeCan.drawRoundRect(new RectF(t.x + tankHeight / 2, t.y - tankHeight / 4, t.x + tankHeight * 2 - tankHeight / 2, t.y + tankHeight / 2), 5.0f, 5.0f, paint);

						paint.setStyle(Paint.Style.FILL);
						paint.setColor(t.c);
						// KUPOLA
						ospredjeCan.drawRoundRect(new RectF(t.x, t.y + tankHeight / 2, t.x + tankHeight * 2, t.y + tankHeight), 10.0f, 10.0f, paint);
						paint.setStyle(Paint.Style.STROKE);
						paint.setColor(Color.BLACK);
						ospredjeCan.drawRoundRect(new RectF(t.x, t.y + tankHeight / 2, t.x + tankHeight * 2, t.y + tankHeight), 10.0f, 10.0f, paint);

						paint.setStyle(Paint.Style.FILL);
						// KOLESA
						for (int j = 0; j < 7; j++) {
							ospredjeCan.drawCircle(t.x + j * 5 + 4, t.y + 3 * tankHeight / 4, 2, paint);
						}


					} else {
						paint.setStyle(Paint.Style.FILL);
						paint.setColor(t.c);
						ospredjeCan.drawArc(new RectF(t.x, t.y, t.x + tankHeight * 2, t.y + tankHeight * 2), 0, -180, false, paint);
						paint.setStyle(Paint.Style.STROKE);
						paint.setColor(Color.BLACK);
						ospredjeCan.drawArc(new RectF(t.x, t.y, t.x + tankHeight * 2, t.y + tankHeight * 2), 0, -180, true, paint);
						paint.setStyle(Paint.Style.FILL);
					}

					paint.setTextSize(10);
					if (t.p) {
						//ospredjeCan.drawText("P", t.x + tankHeight / 2, t.y + tankHeight, paint);
					}

					// Izriše ime (in HP) tanka
					try {
						paint.setColor(Color.BLACK);
						paint.setStyle(Paint.Style.FILL);
						String s = t.n;
						if (tankPrikazHP) {
							s += " (" + t.hp + ")";
						}
						paint.setTextSize(20.0f);
						ospredjeCan.drawText(s, t.x, t.y - tankHeight, paint);
					} catch (Exception e) {

					}

                /*matrix = new Matrix();
                matrix.setTranslate(t.x + tankHeight / 2 - 2, t.y - tankHeight / 2);
                matrix.setRotate(t.rot);*/
					int topX = t.x + tankHeight - tankHeight / 5;
					int topY = t.y - 4 * tankHeight / 5;
					topoviCan.translate(topX + top.getWidth() / 2, topY + top.getHeight());
					topoviCan.rotate(t.rot);
					topoviCan.drawBitmap(top, new Rect(0, 0, top.getWidth(), top.getHeight()), new Rect(-top.getWidth() / 2, -top.getHeight(), top.getWidth() - top.getWidth() / 2, 0), paint);
					topoviCan.rotate(-t.rot);
					topoviCan.translate(-(topX + top.getWidth() / 2), -(topY + top.getHeight()));

                /*paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                can.drawRect(new RectF(t.x, t.y, t.x + tankHeight * 2, t.y + tankHeight), paint);
                paint.setStyle(Paint.Style.FILL);*/
				}

				// Izpiše ime igralca
                /*paint.setTextSize(100);
                Rect bounds = new Rect();
                paint.getTextBounds(tanki.get(naVrsti).n, 0, tanki.get(naVrsti).n.length(), bounds);
                paint.setColor(tanki.get(naVrsti).c);
                paint.setStyle(Paint.Style.FILL);
                ospredjeCan.drawText(tanki.get(naVrsti).n, bmpWidth / 2 - (bounds.right - bounds.left) / 2, (bounds.bottom - bounds.top) + 50, paint);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                ospredjeCan.drawText(tanki.get(naVrsti).n, bmpWidth / 2 - (bounds.right - bounds.left) / 2, (bounds.bottom - bounds.top) + 50, paint);*/

				collCan.drawBitmap(teren, new Rect(0, 0, bmpWidth, bmpHeight), new Rect(0, 0, bmpWidth, bmpHeight), paint);

				// Premakne tanke
				for (int i = 0; i < tanki.size(); i++) {
					try {
						if (padanje(tanki.get(i), 2)) {
							tankiNaTleh.set(i, true);
						}
						//tanki.get(i).rot = (tanki.get(i).rot + 2) % 360;
					} catch (IndexOutOfBoundsException e) {
						// Napaka: poskuša premakniti neobstoječ tank
						Log.e("Tanki", "NAPAKA: " + e.getMessage());
						// Lahko se zgodi, da nekateri izstrelki do takrat, ko pride do napake, še ne eksplodirajo,
						// zato jih je treba odstraniti ročno
						izstrelki.clear();
						//naslednji();
					}
				}

				if (topLDol) topLevo();
				if (topDDol) topDesno();
				if (mocGDol) mocGor();
				if (mocDDol) mocDol();

				// Izriše partikle
				int ptc = 0;
				while (ptc < partikli.size()) {
					Particle p = partikli.get(ptc);
					boolean odstranjen = false;

					if (p != null && p.info != null) {
						int c = paint.getColor();
						paint.setColor(Color.argb(p.alpha, Color.red(p.c), Color.green(p.c), Color.blue(p.c)));
						paint.setStyle(Paint.Style.FILL);
						// Zarotira canvas in izriše partikel
						ospredjeCan.rotate(p.info.rot);
						ospredjeCan.drawRect(new Rect(p.x - p.info.w / 2, p.y - p.info.w / 2, p.x + p.info.w / 2, p.y + p.info.w / 2), paint);
						ospredjeCan.rotate(-p.info.rot);

						// Posodobi partikel
						p.alpha -= p.info.f;
						if (p.alpha <= 0) {
							partikli.remove(p);
							odstranjen = true;
						} else {
							p.y += p.info.g;
							p.r += p.info.rot;
						}

						paint.setColor(c);
					}

					if (!odstranjen) { // Če je bil kak partikel odstranjen, se seznam zamakne, zato ni treba povečati indeksa
						ptc++;
					}
				}

				int st = 0;
				while (st < izstrelki.size()) {
					Izstrelek izstrelek = izstrelki.get(st);
					if (izstrelek != null) {
						boolean unici = false;

						// Premakne izstrelek
						izstrelek.fX += (double) veter / 100;
						izstrelek.fY += grav;
						double dX = izstrelek.fX / 9;
						double dY = izstrelek.fY / 9;
						if (izstrelek.x + dX > izstrelek.r + 1 && izstrelek.x + dX < bmpWidth - izstrelek.r && izstrelek.y + dY < bmpHeight - izstrelek.r) {
							izstrelek.x += dX;
							izstrelek.y += dY;
						} else {
							unici = true;
						}

						// Preveri, ali je iztrelek ob kaj zadel
						boolean hit = false;
						if (izstrelek.y - 2 * izstrelek.r >= 0) {
							int[] b = {coll.getPixel(izstrelek.x - izstrelek.r - 1, izstrelek.y), coll.getPixel(izstrelek.x, izstrelek.y - izstrelek.r - 1), coll.getPixel(izstrelek.x + izstrelek.r, izstrelek.y), coll.getPixel(izstrelek.x, izstrelek.y + izstrelek.r)};
							for (int i : b) {
								if (i != Color.TRANSPARENT) {
									hit = true;
									break;
								}
							}
						}

						// Preveri, ali je izstrelek zadel ob tank
						boolean direkt = false;
						for (Tank t : tanki) {
							if (t != izstrelek.t) {
								if (Utils.prekrivanje(new Rect(izstrelek.x - izstrelek.r, izstrelek.y - izstrelek.r, izstrelek.x + 2 * izstrelek.r, izstrelek.y + 2 * izstrelek.r), new Rect(t.x, t.y, t.x + tankHeight, t.y + tankHeight)))
									hit = true;
									direkt = true;
							}
						}

						if (hit) {

							if (zvok) sp.play(zvoki[0], 0.5f, 0.5f, 1, 0, 1f);  // bomb.mp3

							// Če je izstrelek cluster bomb, izvrže svoje bombe
							if (izstrelek instanceof ClusterBomb) {
								for (int i = 0; i < ((ClusterBomb) izstrelek).bombN; i++) {
									if (((ClusterBomb) izstrelek).cbomb != null) { // Bomba izvrže nov cluster bomb
										ClusterBomb b = ((ClusterBomb) izstrelek).cbomb;
										ClusterBomb novi = new ClusterBomb(izstrelek.t, b.r, b.c, b.dm, b.dr, b.d, b.bomb, b.bombN);
										// Partikli novega izstrelka
										if (partikliOn) {
											novi.setParticleInfo(b.getParticleInfo());
										}
										izstrelki.add(novi);
										novi.setPos(izstrelek.x, izstrelek.y);
										novi.addForce(i * (((ClusterBomb)izstrelek).bombN - 1) * 10 - (((ClusterBomb)izstrelek).bombN - 1) * 10, -50);
									} else if (((ClusterBomb) izstrelek).bomb != null) { // Bomba izvrže navaden izstrelek
										Izstrelek b = ((ClusterBomb) izstrelek).bomb;
										Izstrelek novi = new Izstrelek(izstrelek.t, b.r, b.c, b.dm, b.dr, b.d);
										izstrelki.add(novi);
										novi.setPos(izstrelek.x, izstrelek.y);
										novi.addForce(i * (((ClusterBomb)izstrelek).bombN - 1) * 10 - (((ClusterBomb)izstrelek).bombN - 1) * 10, -50);
									}
								}
							}

							// Poškoduje (oz. gradi) teren
							switch (izstrelek.d) {
								case 0: // Uniči teren
									paint.setColor(Color.RED);
									Xfermode xfm = paint.getXfermode();
									paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
									paint.setStyle(Paint.Style.FILL);
									terenCan.drawCircle(izstrelek.x, izstrelek.y, izstrelek.dr, paint);
									paint.setXfermode(xfm);
									// Blisk izstrelka
									if (bombaBlisk) {
										paint.setColor(Color.rgb(255, 255, 200));
										ospredjeCan.drawCircle(izstrelek.x, izstrelek.y, izstrelek.dr, paint);
									}
									break;
								case 1: // Gradi teren
									paint.setColor(barvaTerena);
									paint.setStyle(Paint.Style.FILL);
									terenCan.drawCircle(izstrelek.x, izstrelek.y, izstrelek.dr, paint);
									break;
							}

							if (izstrelek instanceof Thermonuclear) {
								if (bombaBlisk) {
                                    /*paint.setColor(Color.rgb(255, 255, 255));
                                    for (Tank t : tanki) {
                                        ospredjeCan.drawCircle(t.x + tankHeight / 2, t.y + tankHeight / 2, 100, paint);
                                    }*/
									flash = 255;
								}
							}

							// Poškoduje tanke, če je to omogočeno v nastavitvah
							// TODO točke (denar)
							if (tankPoskodba) {
								int i = 0;

								if (izstrelek instanceof Thermonuclear) {
									ArrayList<Tank> tanki2 = (ArrayList<Tank>) tanki.clone();
									while (tanki2.size() > 0) {

										poskodujTank(tanki2.get(i), izstrelek.dm);

										if (tanki2.size() > 0)
											tanki2.remove(0); // Odstrani obravnavani tank s pomožnega seznama
									}
								} else {
									while (i < tanki.size()) {
										/*if (izstrelek instanceof Thermonuclear) { // Izstrelek je termonuklearna bomba, ki vse tanke poškoduje enako
											poskodujTank(tanki.get(i), izstrelek.dm);
										} else*/ {
											double d = Utils.distance(tanki.get(i).x + tankHeight / 2, tanki.get(i).y + tankHeight, izstrelek.x, izstrelek.y);
											if (d <= izstrelek.dr) {
												poskodujTank(tanki.get(i), (int) (1.5 * (direkt ? 5 : 1) * izstrelek.dm / (d / 2)));
											}
										}
										i++;
									}
								}
							}
							unici = true;
						}

						// Izriše izstrelek, če ta ni previsoko oz. izven igre
						if (izstrelek.y - 2 * izstrelek.r >= 0) {
							paint.setStyle(Paint.Style.FILL);
							paint.setColor(izstrelek.c);
							ospredjeCan.drawCircle(izstrelek.x, izstrelek.y, izstrelek.r, paint);
							paint.setStyle(Paint.Style.STROKE);
							paint.setColor(Color.BLACK);
							ospredjeCan.drawCircle(izstrelek.x, izstrelek.y, izstrelek.r, paint);
						}

						// Ustvari partikel izstrelka
						if (partikliOn) {
							Particle p = izstrelek.generateParticle();
							if (p != null) {
								partikli.add(p);
							}
						}

						// Treba je uničiti izstrelek
						if (unici) {
							izstrelki.remove(izstrelek);//izstrelek = null;
							requestComp();
							// Konec strela, vse igralce postavi na tla
							// Če so vsi izstrelki že eksplodirali, nadaljuje
							if (/*!tankiNaTleh.contains(false) && */izstrelki.size() == 0) {
								//Log.w("Tanki", "Vsi tanki so na tleh.");
								//Toast.makeText(getApplicationContext(), "Vsi tanki so na tleh", Toast.LENGTH_SHORT).show();

								// Na vrsti je naslednji igralec
								pokaziGUI();
								naslednji();
								// Če tanka, ki je na vrsti, ne upravlja noben igralec (je "računalnik"), izračuna njegovo potezo.
							}
						}

					}
					st++;
				}
				grav += 0.025;

				// Preveri, ali je igre konec
				if (tanki.size() <= 1) {
					konec();
					//Log.w("Tanki", "IGRE JE KONEC");
				}

				// Ustvari, premakne in izrriše oblake
				if (soOblaki) {
					if (odNovegaOblaka >= 100) {
						if (oblaki.size() < 5) novOblak();
						odNovegaOblaka = 0;
					}
					int i = 0;
					while (i < oblaki.size()) {
						Oblak o = oblaki.get(i);
						o.x += (veter / 100.0f) * o.v;
						// Če oblak odpihne daleč iz vidnega območja, ga uniči
						if (o.x < -50 || o.x > bmpWidth + 50) {
							oblaki.remove(o);
						}
						oblakiCan.drawBitmap(o.slika, new Rect(0, 0, o.slika.getWidth(), o.slika.getHeight()), new Rect((int) o.x, o.y, (int) o.x + o.slika.getWidth(), o.y + o.slika.getHeight()), paint);
						i++;
					}
					odNovegaOblaka++;
				}

				collCan.drawBitmap(ospredje, new Rect(0, 0, bmpWidth, bmpHeight), new Rect(0, 0, bmpWidth, bmpHeight), paint);

				// Če je bil zahtevan blisk, izvede naslednji frame bliska
				if (flash > 0) {
					// Počisti sliko bliska
					paint.setStyle(Paint.Style.FILL);
					Xfermode xfm = paint.getXfermode();
					paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
					paint.setColor(Color.RED);
					flashCan.drawRect(0, 0, bmpWidth, bmpHeight, paint);
					paint.setXfermode(xfm);

					paint.setColor(Color.rgb(255, 255, 255));
					paint.setAlpha(flash);
					flashCan.drawRect(new Rect(0, 0, bmpWidth, bmpHeight), paint);
					paint.setAlpha(255);
					flash --;
				}

				// Izriše vse plasti
				can.drawBitmap(ozadje, new Rect(0, 0, ozadje.getWidth(), ozadje.getHeight()), new Rect(0, 0, bmpWidth, bmpHeight), paint);
				if (soOblaki) can.drawBitmap(oblakiBmp, new Rect(0, 0, oblakiBmp.getWidth(), oblakiBmp.getHeight()), new Rect(0, 0, bmpWidth, bmpHeight), paint);
				can.drawBitmap(coll, new Rect(0, 0, bmpWidth, bmpHeight), new Rect(0, 0, bmpWidth, bmpHeight), paint);
				can.drawBitmap(topovi, new Rect(0, 0, bmpWidth, bmpHeight), new Rect(0, 0, bmpWidth, bmpHeight), paint);
				can.drawBitmap(flashBmp, new Rect(0, 0, bmpWidth, bmpHeight), new Rect(0, 0, bmpWidth, bmpHeight), paint);

				iv.setImageBitmap(bmp);
				iv.postInvalidate();

				try {
					if (!tanki.get(naVrsti).compFinished) comp();
				} catch (Exception e) {
				}

				// Spet pokliče sebe - začne neskončno zanko izrisovanja
				handler.postDelayed(drawRunnable, drawDelay);
			}
		};

		// Nastavi veter
		nastaviVeter();

		// Zgenerira teren
		nivoTerena = bmpHeight - 200;
		teren();

		novOblak();

		// Postavi tanke (na teren)
		postaviTanke();
		for (int i = 0; i < tanki.size(); i++) {
			tankiNaTleh.add(false);
		}

		moc.setText(tanki.get(0).moc + "%");
		rot.setText(tanki.get(0).rot + "°");
		hp.setText("HP: " + tanki.get(0).hp);
		orozje.setText(Izstrelek.tipi[tanki.get(0).orozje]);
		ivOrozje.setImageBitmap(orozjaSlike.get(tanki.get(0).orozje));

		comp();
		igralec.setText(tanki.get(naVrsti).n);
		igralec.setTextColor(tanki.get(naVrsti).c);

		// Prikaže imena igralcev na seznamu
		try {
			igr1.setText(tanki.get(0).n);
			igr1.setTextColor(tanki.get(0).c);
		} catch (Exception e) {
			igr1.setVisibility(View.GONE);
		}
		try {
			igr2.setText(tanki.get(1).n);
			igr2.setTextColor(tanki.get(1).c);
		} catch (Exception e) {
			igr2.setVisibility(View.GONE);
		}
		try {
			igr3.setText(tanki.get(2).n);
			igr3.setTextColor(tanki.get(2).c);
		} catch (Exception e) {
			igr3.setVisibility(View.GONE);
		}
		try {
			igr4.setText(tanki.get(3).n);
			igr4.setTextColor(tanki.get(3).c);
		} catch (Exception e) {
			igr4.setVisibility(View.GONE);
		}
		try {
			igr5.setText(tanki.get(4).n);
			igr5.setTextColor(tanki.get(4).c);
		} catch (Exception e) {
			igr5.setVisibility(View.GONE);
		}
		try {
			igr6.setText(tanki.get(5).n);
			igr6.setTextColor(tanki.get(5).c);
		} catch (Exception e) {
			igr6.setVisibility(View.GONE);
		}
		try {
			igr7.setText(tanki.get(6).n);
			igr7.setTextColor(tanki.get(6).c);
		} catch (Exception e) {
			igr7.setVisibility(View.GONE);
		}
		try {
			igr8.setText(tanki.get(7).n);
			igr8.setTextColor(tanki.get(7).c);
		} catch (Exception e) {
			igr8.setVisibility(View.GONE);
		}
		try {
			igr9.setText(tanki.get(8).n);
			igr9.setTextColor(tanki.get(8).c);
		} catch (Exception e) {
			igr9.setVisibility(View.GONE);
		}

		comp();

		// Izrisovanje se začne v onResume

	}

	// Izriše teren
	public void teren() {
		paint.setColor(barvaTerena);
		terenCan.drawRect(new RectF(0, nivoTerena, bmpWidth, bmpHeight), paint);
		// Hribi
		Random r = new Random();
		if (hribcki) {
			paint.setColor(barvaTerena);
			for (int i = 0; i < 5; i++) {
				int x = r.nextInt(bmpWidth);
				int y = nivoTerena;
				int a = r.nextInt(100) + 25;
				int b = (r.nextInt(4) + 1) * 25;
				RectF elipsa = new RectF(x - a, y - b, x + a, y + b);
				terenCan.drawOval(elipsa, paint);
			}
		}
		// Noise
		if (terenTekstura) {
			Xfermode xfm = paint.getXfermode();
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			paint.setColor(barvaTeksture);
			for (int i = 0; i < 10000; i++) {
				int xPos = r.nextInt(bmpWidth - velikostZrn);
				int yPos = r.nextInt(bmpHeight - velikostZrn);
				terenCan.drawRect(new Rect(xPos, yPos, xPos + velikostZrn, yPos + velikostZrn), paint);
			}
			paint.setXfermode(xfm);
		}
	}

    /*public void postaviTanke(int players) {
        int num = imena.size();
        if (num > 6) num = 6;
        if (players > imena.size()) return;
        ArrayList<Integer> xPos = new ArrayList<Integer>();
        Random r = new Random();
        boolean chk = true;
        for (byte i = 0; i < num; i++) {
            if (barve.size() > 0) {
                int x = r.nextInt(bmpWidth - 20);
                while (!postavitev(x)) {
                    x = r.nextInt(bmpWidth - 20);
                }
                xPos.add(x);
                int b = r.nextInt(barve.size());
                boolean p = false;
                if (players > 0) {
                    p = true;
                    players--;
                }
                tanki.add(new Tank(imena.get(i), x, bmpHeight - 150, tankHeight, barve.get(b), p));
                barve.remove(b);
            }
        }
    }*/

	private void poskodujTank(Tank tank, int damage) {
		tank.hp -= damage;
		if (tank.hp <= 0) {
			hp.setText("HP: 0");
			// Blisk tanka
			if (tankBlisk) {
				paint.setColor(Color.rgb(255, 150, 0));
				ospredjeCan.drawCircle(tank.x + tankHeight / 2, tank.y + tankHeight, 100, paint);
			}
			eksplodirajTank(tank);
		} else {
			if (naVrsti < tanki.size())
				hp.setText("HP: " + tanki.get(naVrsti).hp);
		}
	}

	// Tank eksplodira
	private void eksplodirajTank(Tank tank) {
		if (zvok) {
			sp.play(zvoki[(new Random().nextBoolean()) ? 2 : 3], 1f, 1f, 1, 0, 1f);
		}
		//paint.setColor(Color.RED);
		Xfermode xfm = paint.getXfermode();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		paint.setStyle(Paint.Style.FILL);
		terenCan.drawCircle(tank.x + tankHeight / 2, tank.y + tankHeight, 100, paint);
		paint.setXfermode(xfm);
		if (tankBomba) {
			ClusterBomb b = new ClusterBomb(tank, 4, Color.BLACK, 100, 40, 0, new Izstrelek(tank, 4, Color.BLACK, 100, 40, 0), 3);
			b.setPos(tank.x + tankHeight / 2, tank.y);
			b.addForce(0, -75);
			izstrelki.add(b);
			b = new ClusterBomb(tank, 4, Color.BLACK, 100, 40, 0, new Izstrelek(tank, 4, Color.BLACK, 100, 40, 0), 3);
			b.setPos(tank.x, tank.y);
			b.addForce(-25, -50);
			izstrelki.add(b);
			b = new ClusterBomb(tank, 4, Color.BLACK, 100, 40, 0, new Izstrelek(tank, 4, Color.BLACK, 100, 40, 0), 3);
			b.setPos(tank.x + tankHeight, tank.y);
			b.addForce(25, -50);
			izstrelki.add(b);

		}
		// Eksplozija poškoduje ostale tanke v bližini
        /*int i = 0;
        while (i < tanki.size()) {
            double d = Utils.distance(tank.x, tanki.get(i).x, tank.y, tanki.get(i).y);
            if (d <= 100) {
                //poskodujTank(tanki.get(i), (int)(100 / (d / 2)));
            }
            i++;
        }*/
		tanki.remove(tank);
	}

	private void postaviTanke() {
		ArrayList<Integer> xPos = new ArrayList<Integer>();
		Random r = new Random();
		boolean chk = true;
		int[] tipi = new int[] {tip1, tip2, tip3, tip4, tip5, tip6, tip7, tip8, tip9};
		int[] barve = new int[] {c1, c2, c3, c4, c5, c6, c7, c8, c9};
		String[] imena = new String[] {ime1, ime2, ime3, ime4, ime5, ime6, ime7, ime8, ime9};

		for (byte i = 0; i < 9; i++) {
			if (/*tipi[i] != R.id.rb1_brez && tipi[i] != R.id.rb2_brez && */tipi[i] != R.id.rb3_brez && tipi[i] != R.id.rb4_brez && tipi[i] != R.id.rb5_brez && tipi[i] != R.id.rb6_brez && tipi[i] != R.id.rb7_brez && tipi[i] != R.id.rb8_brez && tipi[i] != R.id.rb9_brez) {
				int x = r.nextInt(bmpWidth - 20);
				while (!postavitev(x)) {
					x = r.nextInt(bmpWidth - 20);
				}
				xPos.add(x);
				boolean p = false; // Igralec
				if (tipi[i] == R.id.rb1_igralec || tipi[i] == R.id.rb2_igralec || tipi[i] == R.id.rb3_igralec || tipi[i] == R.id.rb4_igralec || tipi[i] == R.id.rb5_igralec || tipi[i] == R.id.rb6_igralec || tipi[i] == R.id.rb7_igralec || tipi[i] == R.id.rb8_igralec || tipi[i] == R.id.rb9_igralec) {
					p = true;
				}
				// Položaj y - postavi tank na najvišjo točko terena pod njim
				int y = 0;
				int minDist = bmpWidth;
				for (int j = tankHeight; j < bmpWidth; j++) {
					if (Color.alpha(teren.getPixel(x + tankHeight / 2, j)) != 0) {
						y = j - tankHeight;
						break;
					}
				}

				Tank t = new Tank(imena[i], x, y, tankHeight, barve[i], tankiHP.get(tankiHPId), p);
				if (!p && !aiMenjaOrozje) t.orozje = (byte)aiOrozjeId;
				tanki.add(t);
			}
		}

	}

	// Preveri, ali tank lahko postavi na danem mestu (preveri, ali ne bo preblizu ostalih tankov itd.)
	private boolean postavitev(int x) {
		boolean rez = true;
		for (Tank t : tanki) {
			if (Math.abs(t.x - x) < 75 || x > bmpWidth - tankHeight * 2 || x < tankHeight * 2) {
				rez = false;
			}
		}
		return rez;
	}

	private boolean padanje(Tank tank, int d) {
		int minDist = d;
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < d; j++) {
				if (tank.y + tankHeight + 1 + j < bmpHeight) {
					int b = coll.getPixel(tank.x + i, tank.y + tankHeight + 1 + j);
					if (b != 0) { // Našel razdaljo do tal
						if (j < minDist) {
							minDist = j;
							//Log.w("Tanki", "minDist: " + minDist);
							if (minDist == 0) return true;
						}
					}
				} else {
					// Tank je padel iz igre
					tanki.remove(tank);
					// FIXME umetna inteligenca včasih zmrzne
					//comp();
					return true;
				}
			}
		}
		int premik;

		if (minDist < d) {
			premik = minDist;
		} else {
			premik = d;
		}
		if (tank.y + tankHeight + 1 + premik >= bmpHeight) {
			tanki.remove(tank);
			comp();
		} else {
			// Če je tank na tleh, vrne true
			if (premik == 0) return true;
			tank.y += premik;
		}
		return false;
	}

	private void strel() {
		if (zvok) sp.play(zvoki[1], 1f, 1f, 1, 0, 1f);  // fire.mp3
		// Tank lahko izstreli le, če ni nobenega neeksplodiranega izstrelka v zraku
		if (izstrelki.size() > 0) izstrelki.clear();
		if (izstrelki.size() == 0) {
			grav = 0.05; // Nastavi gravitacijo nazaj na začetno vrednost
			skrijGUI();
			// Ustvari izstrelke
			ArrayList<Izstrelek> i = tanki.get(naVrsti).strel();
			for (Izstrelek izstrelek : i) {
				izstrelki.add(izstrelek);//izstrelek = tanki.get(naVrsti).strel();
			}
		} else {
			izstrelki.clear();
		}
	}

	private void nastaviVeter() {
		Random r = new Random();
		veter = r.nextInt(50) * ((negativenVeter) ? -1 : 1);
		spremembaSmeriVetra++;
		if (spremembaSmeriVetra >= 5 && r.nextBoolean()) {  // TODO možnost nastavitve te konstante
			negativenVeter = !negativenVeter;
			spremembaSmeriVetra = 0;
		}
		textVeter.setText("Veter: " + veter);
	}

	private void novOblak() {
		Random r = new Random();
		int slika = r.nextInt(oblakiSlike.size());
		int x, y;
		float v = r.nextFloat() + 1.0f;  // Modifikator hitrosti
		float s = r.nextFloat() + 0.5f;  // Modifikator velikosti
		// Spremeni širino in višino oblaka, pri čemer obdrži prvotno razmerje
		int w = (int)(oblakiSlike.get(slika).getWidth() * s);
		int h = (w * oblakiSlike.get(slika).getHeight()) / oblakiSlike.get(slika).getWidth();
		if (oblaki.size() == 0) {
			x = r.nextInt(bmpWidth);
			y = r.nextInt(nivoTerena);
		} else {
			// Doda oblak na tisto stran ekrana, kjer jih je manj
			int vsota = 0;
			for (Oblak o : oblaki) vsota += o.x;
			float avgX = vsota / oblaki.size();
			x = (avgX >= bmpWidth / 2) ? -oblakiSlike.get(slika).getWidth() : bmpWidth + 1;
			y = r.nextInt(bmpHeight - oblakiSlike.get(slika).getHeight());
		}
		oblaki.add(new Oblak(x, y, w, h, oblakiSlike.get(slika), v));
	}

	public void streljaj(View v) {
		if (!konecIgre) {
			strel();
		}
		//leti = true;
	}

	public void klik(View v) {
		Log.w("Tanki", Integer.toString(v.getId()));
	}

	// Konec igre
	private void konec() {
		skrijGUI();
		konecIgre = true;
		//handler.removeCallbacks(drawRunnable);
	}

	////////////////////////////////////////////////////////////////////////////

	public void naslednji() {
		if (tanki.size() > 1) {
			tankUstrelil = false;
			izstrelki.clear();
			naVrsti = (naVrsti + 1) % tanki.size();
			// Posodobi napise
			igralec.setText(tanki.get(naVrsti).n);
			igralec.setTextColor(tanki.get(naVrsti).c);
			hp.setText("HP: " + tanki.get(naVrsti).hp);
			rot.setText(tanki.get(naVrsti).rot + "°");
			moc.setText(tanki.get(naVrsti).moc + "%");

			orozje.setText(Izstrelek.tipi[tanki.get(naVrsti).orozje]);
			ivOrozje.setImageBitmap(orozjaSlike.get(tanki.get(naVrsti).orozje));
			nastaviVeter();
		}
	}

	public void topLevo() {
		if (naVrsti < tanki.size()) {
			int r = tanki.get(naVrsti).rot;
			if (r > -90) {
				tanki.get(naVrsti).rot -= 1;
				rot.post(new Runnable() {
					@Override
					public void run() {
						if (naVrsti < tanki.size()) rot.setText(Integer.toString(tanki.get(naVrsti).rot) + "°");
					}
				});
			}
		}
	}

	public void topDesno() {
		int r = tanki.get(naVrsti).rot;
		if (r < 90) {
			tanki.get(naVrsti).rot += 1;
			rot.post(new Runnable() {
				@Override
				public void run() {
					if (naVrsti < tanki.size()) rot.setText(Integer.toString(tanki.get(naVrsti).rot) + "°");
				}
			});
		}
	}

	public void mocGor() {
		int m = tanki.get(naVrsti).moc;
		if (m < 100) {
			tanki.get(naVrsti).moc += 1;
			moc.post(new Runnable() {
				@Override
				public void run() {
					moc.setText(Integer.toString(tanki.get(naVrsti).moc) + "%");
				}
			});
		}
	}

	public void mocDol() {
		int m = tanki.get(naVrsti).moc;
		if (m > 1) {
			tanki.get(naVrsti).moc -= 1;
			moc.post(new Runnable() {
				@Override
				public void run() {
					if (naVrsti < tanki.size()) moc.setText(tanki.get(naVrsti).moc + "%");
				}
			});
		}
	}

	public void orozjeNazaj() {
		int o = tanki.get(naVrsti).orozje;
		if (o == 0) {
			tanki.get(naVrsti).orozje = (byte) (Izstrelek.tipi.length - 1);
		} else {
			tanki.get(naVrsti).orozje = (byte) ((o - 1) % Izstrelek.tipi.length);
		}
		orozje.post(new Runnable() {
			@Override
			public void run() {
				orozje.setText(Izstrelek.tipi[tanki.get(naVrsti).orozje]);
			}
		});
		ivOrozje.post(new Runnable() {
			@Override
			public void run() {
				if (naVrsti >= tanki.size()) {
					naVrsti = 0;
				} else {
					ivOrozje.setImageBitmap(orozjaSlike.get(tanki.get(naVrsti).orozje));
				}
			}
		});
	}

	public void orozjeNaprej() {
		int o = tanki.get(naVrsti).orozje;
		if (o == Izstrelek.tipi.length - 1) {
			tanki.get(naVrsti).orozje = (byte) 0;
		} else {
			tanki.get(naVrsti).orozje = (byte) ((o + 1) % Izstrelek.tipi.length);
		}
		orozje.post(new Runnable() {
			@Override
			public void run() {
				orozje.setText(Izstrelek.tipi[tanki.get(naVrsti).orozje]);
			}
		});
		ivOrozje.post(new Runnable() {
			@Override
			public void run() {
				ivOrozje.setImageBitmap(orozjaSlike.get(tanki.get(naVrsti).orozje));
			}
		});
	}

	public void skrijGUI() {
        /*streljaj.setVisibility(View.INVISIBLE);
        topL.setVisibility(View.INVISIBLE);
        topD.setVisibility(View.INVISIBLE);*/
		//ui.setVisibility(View.INVISIBLE);
		streljaj.setEnabled(false);
		mocG.setEnabled(false);
		mocD.setEnabled(false);
		topL.setEnabled(false);
		topD.setEnabled(false);
		orozjeL.setEnabled(false);
		orozjeD.setEnabled(false);
		// Spusti vse gumbe
		topLDol = false;
		topDDol = false;
		mocGDol = false;
		mocDDol = false;
	}

	public void pokaziGUI() {
        /*streljaj.setVisibility(View.VISIBLE);
        topL.setVisibility(View.VISIBLE);
        topD.setVisibility(View.VISIBLE);*/
		streljaj.setEnabled(true);
		mocG.setEnabled(true);
		mocD.setEnabled(true);
		topL.setEnabled(true);
		topD.setEnabled(true);
		orozjeL.setEnabled(true);
		orozjeD.setEnabled(true);
	}

	public void loadSettings(String profil) {
		if (!onlinePlay) {
			SharedPreferences prefs = this.getSharedPreferences(profil, Context.MODE_PRIVATE);
			tip1 = prefs.getInt("tip1", R.id.rb1_igralec);
			tip2 = prefs.getInt("tip2", R.id.rb2_igralec);
			tip3 = prefs.getInt("tip3", R.id.rb3_igralec);
			tip4 = prefs.getInt("tip4", R.id.rb4_igralec);
			tip5 = prefs.getInt("tip5", R.id.rb5_igralec);
			tip6 = prefs.getInt("tip6", R.id.rb6_igralec);
			tip7 = prefs.getInt("tip7", R.id.rb7_igralec);
			tip8 = prefs.getInt("tip8", R.id.rb8_igralec);
			tip9 = prefs.getInt("tip9", R.id.rb9_igralec);
			c1 = prefs.getInt("c1", Color.rgb(255, 0, 0));
			c2 = prefs.getInt("c2", Color.rgb(255, 255, 0));
			c3 = prefs.getInt("c3", Color.rgb(0, 255, 0));
			c4 = prefs.getInt("c4", Color.rgb(0, 255, 255));
			c5 = prefs.getInt("c5", Color.rgb(0, 0, 255));
			c6 = prefs.getInt("c6", Color.rgb(255, 0, 255));
			c7 = prefs.getInt("c7", Color.rgb(255, 255, 255));
			c8 = prefs.getInt("c8", Color.rgb(128, 128, 128));
			c9 = prefs.getInt("c9", Color.rgb(80, 0, 0));
			ime1 = prefs.getString("ime1", "Tank 1");
			ime2 = prefs.getString("ime2", "Tank 2");
			ime3 = prefs.getString("ime3", "Tank 3");
			ime4 = prefs.getString("ime4", "Tank 4");
			ime5 = prefs.getString("ime5", "Tank 5");
			ime6 = prefs.getString("ime6", "Tank 6");
			ime7 = prefs.getString("ime7", "Tank 7");
			ime8 = prefs.getString("ime8", "Tank 8");
			ime9 = prefs.getString("ime9", "Tank 9");
			soOblaki = prefs.getBoolean("clouds", true);
			hribcki = prefs.getBoolean("hills", false);
			terenTekstura = prefs.getBoolean("terrainTexture", false);
			tankPoskodba = prefs.getBoolean("tankDamage", true);
			tankPrikazHP = prefs.getBoolean("tankShowHP", false);
			tankBomba = prefs.getBoolean("tankClusterBomb", true);
			tankBlisk = prefs.getBoolean("tankFlash", false);
			bombaBlisk = prefs.getBoolean("bombFlash", false);
			tankIzgled = prefs.getBoolean("tankAppearance", false);
			partikliOn = prefs.getBoolean("particles", false);
			aiMenjaOrozje = prefs.getBoolean("aiMenjaOrozje", false);
			zvok = prefs.getBoolean("zvok", true);
			velikostZrn = prefs.getInt("grainSize", 3);
			ozadjeId = prefs.getInt("ozadjeId", 0);
			terenId = prefs.getInt("terenId", 0);
			teksturaId = prefs.getInt("teksturaId", 0);
			aiOrozjeId = prefs.getInt("aiOrozjeId", 0);
			tankiHPId = prefs.getInt("tankiHPId", 1);
		} else {
			final AlertDialog.Builder builder  = new AlertDialog.Builder(GameActivity.this);
			builder.setTitle("Še ni implementirano");
			builder.setMessage("Omogočena je igra prek interneta, ki zaenkrat še ne deluje.");
			builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.cancel();
					finish();
				}
			});
			builder.show();
			Toast.makeText(this, "ONLINE PLAY", Toast.LENGTH_SHORT).show();
			tip1 = R.id.rb1_igralec;
			tip2 = R.id.rb2_igralec;
			tip3 = R.id.rb3_igralec;
			tip4 = R.id.rb4_igralec;
			tip5 = R.id.rb5_igralec;
			tip6 = R.id.rb6_igralec;
			tip7 = R.id.rb7_igralec;
			tip8 = R.id.rb8_igralec;
			tip9 = R.id.rb9_igralec;
			c1 = Color.rgb(255, 0, 0);
			c2 = Color.rgb(255, 255, 0);
			c3 = Color.rgb(0, 255, 0);
			c4 = Color.rgb(0, 255, 255);
			c5 = Color.rgb(0, 0, 255);
			c6 = Color.rgb(255, 0, 255);
			c7 = Color.rgb(255, 255, 255);
			c8 = Color.rgb(128, 128, 128);
			c9 = Color.rgb(80, 0, 0);
			ime1 = "Tank 1";
			ime2 = "Tank 2";
			ime3 = "Tank 3";
			ime4 = "Tank 4";
			ime5 = "Tank 5";
			ime6 = "Tank 6";
			ime7 = "Tank 7";
			ime8 = "Tank 8";
			ime9 = "Tank 9";
			soOblaki = true;
			hribcki = false;
			terenTekstura = false;
			tankPoskodba = true;
			tankPrikazHP = false;
			tankBomba = true;
			tankBlisk = false;
			bombaBlisk = false;
			tankIzgled = true;
			partikliOn = false;
			aiMenjaOrozje = false;
			zvok = true;
			velikostZrn = 3;
			ozadjeId = 0;
			terenId = 0;
			teksturaId = 0;
			aiOrozjeId = 0;
			tankiHPId = 1;
		}
	}

	// Pripravi tanke na potezo
	public void requestComp() {
		for (int i = 0; i < tanki.size(); i++) {
			tanki.get(i).compFinished = false;
		}
	}
	//////////////////////////////////////////////////////
	// Izračuna potezo umetne inteligence
	// TODO ekipe
	public void comp() {

		Thread topLevoThread = new Thread(new Runnable() {
			@Override
			public void run() {
				topLevo();
			}
		});
		Thread topDesnoThread = new Thread(new Runnable() {
			@Override
			public void run() {
				topDesno();
			}
		});
		Thread mocGorThread = new Thread(new Runnable() {
			@Override
			public void run() {
				mocGor();
			}
		});
		Thread mocDolThread = new Thread(new Runnable() {
			@Override
			public void run() {
				mocDol();
			}
		});
		Thread orozjeNaprejThread = new Thread(new Runnable() {
			@Override
			public void run() {
				orozjeNaprej();
			}
		});


		if (!tanki.get(naVrsti).p && !tanki.get(naVrsti).compFinished) {

			//Log.w("Tanki", "COMP");
			skrijGUI();

			if (flash == 0) {
				switch (tanki.get(naVrsti).aiType) {
					case 0: // Najlažje
						int manj = 0;
						int vec = 0;
						for (int i = 0; i < tanki.size(); i++) {
							if (tanki.get(i).x < tanki.get(naVrsti).x) {
								manj++;
							} else if (tanki.get(i).x > tanki.get(naVrsti).x) {
								vec++;
							}
						}

						if (vec > manj && tanki.get(naVrsti).rot < 90) {
							//Log.w("Tanki", "[AI tip 0]: " + "Obračam desno... [" + Integer.toString(tanki.get(naVrsti).rot) + "]");
							topDesnoThread.start();
						} else if (vec < manj && tanki.get(naVrsti).rot > -90) {
							//Log.w("Tanki", "[AI tip 0]: " + "Obračam levo... [" + Integer.toString(tanki.get(naVrsti).rot) + "]");
							topLevoThread.start();
						} else {
							//Log.w("Tanki", "[AI tip 0]: " + "OK");
							strel();
							tanki.get(naVrsti).compFinished = true;
						}
						break;
					case 1: // Nekoliko težje
						// Preveri, kateri tank je najbližji
						int minDistance = bmpWidth;
						int tankNum = 0;
						for (int i = 0; i < tanki.size(); i++) {
							if (i != naVrsti) {
								int tankX = tanki.get(i).x;
								if (Math.abs(tanki.get(naVrsti).x - tankX) < minDistance) {
									minDistance = Math.abs(tanki.get(naVrsti).x - tankX);
									tankNum = i;
								}
							}
						}
						minDistance = (int) (minDistance / 2) * 2;
						if (tanki.get(tankNum).x > tanki.get(naVrsti).x && tanki.get(naVrsti).rot < 90) {
							//Log.w("Tanki", "[AI tip 1]: " + "Obračam desno... [" + Integer.toString(tanki.get(naVrsti).rot) + "]");
							topDesnoThread.start();
						} else if (tanki.get(tankNum).x < tanki.get(naVrsti).x && tanki.get(naVrsti).rot > -90) {
							//Log.w("Tanki", "[AI tip 1]: " + "Obračam levo... [" + Integer.toString(tanki.get(naVrsti).rot) + "]");
							topLevoThread.start();
						} else {
							if (minDistance / 3 > tanki.get(naVrsti).moc && tanki.get(naVrsti).moc < 100) {
								//Log.w("Tanki", "[AI tip 1]: " + "Povečujem moč... [" + Integer.toString(tanki.get(naVrsti).moc) + "]");
								mocGorThread.start();
							} else if (minDistance / 3 < tanki.get(naVrsti).moc) {
								//Log.w("Tanki", "[AI tip 1]: " + "Zmanjšujem moč... [" + Integer.toString(tanki.get(naVrsti).moc) + "]");
								mocDolThread.start();
							} else {
								//Log.w("Tanki", "[AI tip 1]: " + "OK");
								// Odloči o menjanju orožja
								if (aiMenjaOrozje && !tankUstrelil) {
									Random r = new Random();
									int i = r.nextInt(5);
									if (i == 0) {
										// Zamenja orožje
										i = r.nextInt(Izstrelek.tipi.length);
										tanki.get(naVrsti).orozje = (byte) i;

										orozje.setText(Izstrelek.tipi[i]);
										ivOrozje.setImageBitmap(orozjaSlike.get(i));
									}
								}
								if (izstrelki.size() == 0) {
									tankUstrelil = true;
									strel();
									tanki.get(naVrsti).compFinished = true;
								} else {
									//Log.w("Tanki", "[AI tip 1]: Čakam izstrelke.");
								}
							}
						}

						break;
					case 2:
						// TODO boljša umetna inteligenca
						// Preveri, kateri tank je najbližji (po vodoravni osi)
						minDistance = bmpWidth;
						tankNum = 0;
						for (int i = 0; i < tanki.size(); i++) {
							if (i != naVrsti) {
								int tankX = tanki.get(i).x;
								if (Math.abs(tanki.get(naVrsti).x - tankX) < minDistance) {
									minDistance = Math.abs(tanki.get(naVrsti).x - tankX);
									tankNum = i;
								}
							}
						}

						// Razdalja najbližjega tanka od trenutnega tanka
						double distX = (tanki.get(tankNum).x) - (tanki.get(naVrsti).x);
						double distY = (tanki.get(tankNum).y) - (tanki.get(naVrsti).y);
						double dist = Math.sqrt((Math.abs(distX) * Math.abs(distX)) + (Math.abs(distY) * Math.abs(distY)));

						// Razdalja, ki jo bo krogla prepotovala
						//double d = tanki.get(naVrsti).moc

						// Izračuna kot, pod katerim mora biti top obrnjen
						double kot = 90.0;
						Log.w("Tanki", distX + ", " + distY);

						if (distX >= 0) {
							if (distY < 0) {
								kot = Math.atan(Math.abs(distY) / Math.abs(distX));
								Log.w("Tanki", Double.toString(kot));
							}
						}

						//Log.w("Tanki", Double.toString(kot));

						// Obrača top do pravega kota
						if (tanki.get(naVrsti).rot > Math.round(kot)) {
							//Log.w("Tanki", "[AI tip 2]: " + "Obračam levo... [" + Integer.toString(tanki.get(naVrsti).rot) + "]");
							topLevoThread.start();
						} else if (tanki.get(naVrsti).rot < Math.round(kot)) {
							//Log.w("Tanki", "[AI tip 2]: " + "Obračam desno... [" + Integer.toString(tanki.get(naVrsti).rot) + "]");
							topDesnoThread.start();
						} else {
							//Log.w("Tanki", "[AI tip 2]: " + "OK");
							if (izstrelki.size() == 0) {
								tankUstrelil = true;
								strel();
								tanki.get(naVrsti).compFinished = true;
							}
						}
						break;
				}

			}
		}
	}

}
