package net.ddns.iztok.tanki;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends Activity {

    // TODO izbira možnih orožij

    private boolean soOblaki, hribcki, terenTekstura, tankPoskodba, tankPrikazHP, tankBomba, tankBlisk, bombaBlisk, tankIzgled, partikli, aiMenjaOrozje, zvok;
    private byte igralec = 0;
    private EditText i1, i2, i3, i4, i5, i6, i7, i8, i9, profileName;
    private Button b1, b2, b3, b4, b5, b6, b7, b8, b9, ozadjeNazajGumb, ozadjeNaprejGumb, terenNazajGumb, terenNaprejGumb;
    private NumberPicker grainSize;
    private RadioGroup r1, r2, r3, r4, r5, r6, r7, r8, r9;
    private int tip1, tip2, tip3, tip4, tip5, tip6, tip7, tip8, tip9, c1, c2, c3, c4, c5, c6, c7, c8, c9, velikostZrn, ozadjeId, terenId, teksturaId, aiOrozjeId, tankiHPId;
    private Spinner tankWeapon, tankHP, profiles;
    private String ime1, ime2, ime3, ime4, ime5, ime6, ime7, ime8, ime9;
    private Switch obl, hills, terrainTexture, tankDamage, tankShowHP, tankClusterBomb, tankFlash, bombFlash, tankAppearance, particles, tankAiChangeWeapon, sound;
    private ImageView ozadje, teren, tekstura;
    private Bitmap o1, o2, o3, o4, o5, o, t, x;
    private Canvas oCan, tCan, xCan;
    private Paint paint = new Paint();
    private ArrayList<Bitmap> ozadja = new ArrayList<>();
    private ArrayList<Integer> barveTerena = new ArrayList<>();
    private ArrayList<Integer> barveTeksture = new ArrayList();
    private ArrayAdapter<CharSequence> weaponAdapter, hpAdapter, profileAdapter;
    private String[] profileList;

    private LinearLayout layout;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            layout.setSystemUiVisibility(
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        layout = findViewById(R.id.layout);

        r1 = findViewById(R.id.r1);
        r2 = findViewById(R.id.r2);
        r3 = findViewById(R.id.r3);
        r4 = findViewById(R.id.r4);
        r5 = findViewById(R.id.r5);
        r6 = findViewById(R.id.r6);
        r7 = findViewById(R.id.r7);
        r8 = findViewById(R.id.r8);
        r9 = findViewById(R.id.r9);
        i1 = findViewById(R.id.ime1);
        i2 = findViewById(R.id.ime2);
        i3 = findViewById(R.id.ime3);
        i4 = findViewById(R.id.ime4);
        i5 = findViewById(R.id.ime5);
        i6 = findViewById(R.id.ime6);
        i7 = findViewById(R.id.ime7);
        i8 = findViewById(R.id.ime8);
        i9 = findViewById(R.id.ime9);
        profileName = findViewById(R.id.profileName);

        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        b4 = findViewById(R.id.b4);
        b5 = findViewById(R.id.b5);
        b6 = findViewById(R.id.b6);
        b7 = findViewById(R.id.b7);
        b8 = findViewById(R.id.b8);
        b9 = findViewById(R.id.b9);

        obl = findViewById(R.id.obl);
        hills = findViewById(R.id.hills);
        terrainTexture = findViewById(R.id.terr_texture);
        tankDamage = findViewById(R.id.tank_damage); // Ali se tanki lahko poškodujejo
        tankShowHP = findViewById(R.id.tank_show_hp);
        tankClusterBomb = findViewById(R.id.tank_cbomb);
        tankFlash = findViewById(R.id.tank_flash);
        bombFlash = findViewById(R.id.bomb_flash);
        tankAppearance = findViewById(R.id.tank_appr);
        particles = findViewById(R.id.particles);
        tankAiChangeWeapon = findViewById(R.id.tank_ai_change_weapon);
        sound = findViewById(R.id.sound);

        // SPINNERJI

        tankWeapon = findViewById(R.id.tank_weapon);
        weaponAdapter = ArrayAdapter.createFromResource(this, R.array.weapons_array, android.R.layout.simple_spinner_item);
        weaponAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tankWeapon.setAdapter(weaponAdapter);
        tankWeapon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                aiOrozjeId = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                aiOrozjeId = 0;
            }

        });

        tankHP = findViewById(R.id.tank_hp);
        hpAdapter = ArrayAdapter.createFromResource(this, R.array.hp_array, android.R.layout.simple_spinner_item);
        hpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tankHP.setAdapter(hpAdapter);
        tankHP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tankiHPId = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                tankiHPId = 1; // Privzeta vrednost je 100
            }
        });

        // NUMBERPICKERJI
        grainSize = findViewById(R.id.grain_size);
        grainSize.setMinValue(1);
        grainSize.setMaxValue(10);

        // PROFILI
        profiles = findViewById(R.id.profili);

        /*profiles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Izbrano: " + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "Nič ni bilo izbrano.", Toast.LENGTH_SHORT).show();
            }
        });*/

        ozadje = findViewById(R.id.iv_ozadje);
        teren = findViewById(R.id.iv_teren);
        tekstura = findViewById(R.id.iv_teren_tekstura);

        // Slike
        o1 = BitmapFactory.decodeResource(getResources(), R.drawable.ozadje1);
        o2 = BitmapFactory.decodeResource(getResources(), R.drawable.ozadje2);
        o3 = BitmapFactory.decodeResource(getResources(), R.drawable.ozadje3);
        o4 = BitmapFactory.decodeResource(getResources(), R.drawable.ozadje4);
        ozadja.add(o1);
        ozadja.add(o2);
        ozadja.add(o3);
        ozadja.add(o4);

        // Barve terena
        barveTerena.add(Color.rgb(56, 126, 0));
        barveTerena.add(Color.rgb(76, 126, 4));
        barveTerena.add(Color.rgb(95, 126, 0));
        barveTerena.add(Color.rgb(128, 128, 0));
        barveTerena.add(Color.rgb(128, 128, 128));
        barveTerena.add(Color.rgb(94, 52, 0));
        barveTerena.add(Color.rgb(255, 255, 255));
        barveTerena.add(Color.rgb(198, 166, 100));

        // Barve teksture
        barveTeksture.add(Color.rgb(94, 52, 0));
        barveTeksture.add(Color.rgb(128, 128, 128));
        barveTeksture.add(Color.rgb(128, 64, 0));
        barveTeksture.add(Color.rgb(200, 255, 255));

        terrainTexture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    grainSize.setEnabled(true);
                    grainSize.setValue(velikostZrn);
                } else {
                    grainSize.setEnabled(false);
                }
            }
        });

        tankDamage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    tankClusterBomb.setEnabled(true);
                    tankClusterBomb.setChecked(tankBomba);
                    tankShowHP.setEnabled(true);
                    tankShowHP.setChecked(tankPrikazHP);
                    tankFlash.setEnabled(true);
                    tankFlash.setChecked(tankBlisk);
                    tankHP.setEnabled(true);
                    tankHP.setSelection(tankiHPId);
                } else {
                    tankClusterBomb.setChecked(false);
                    tankClusterBomb.setEnabled(false);
                    tankShowHP.setChecked(false);
                    tankShowHP.setEnabled(false);
                    tankFlash.setChecked(false);
                    tankFlash.setEnabled(false);
                    tankHP.setEnabled(false);
                }
            }
        });

        tankAiChangeWeapon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    tankWeapon.setEnabled(false);
                } else {
                    tankWeapon.setEnabled(true);
                }
            }
        });

        getProfiles();
        loadSettings("net.ddns.iztok.tanki.xml");
    }

    public void getProfiles() {
        File prefsDir = new File(getApplicationInfo().dataDir, "shared_prefs");
        if (prefsDir.exists() && prefsDir.isDirectory()) {
            profileList = prefsDir.list();
            // Odstrani privzet profil s seznama
			/*ArrayList<String> list = new ArrayList<>(Arrays.asList(profileList));
			list.remove("net.ddns.iztok.tanki.xml");
			list.toArray(profileList);*/
			// Če ni nobenega profila, onemogoči spinner in gumb za nalaganje profilov,
            // da ne bi prišlo do IndexOutOfBoundsException.
			/*if (profileList.length == 0) {
			    profiles.setEnabled(false);
                Button loadButton = findViewById(R.id.button_loadprofile);
                loadButton.setEnabled(false);
            }*/
			ArrayAdapter<String> profilesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, android.R.id.text1, profileList);
			profiles.setAdapter(profilesAdapter);

        }
    }

    public void loadSettings(String profil) {
        // Odstrani .xml na koncu
        profil = profil.substring(0, profil.length() - 4);
        Log.w("Tanki", "Novo ime: " + profil);
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
        hribcki = prefs.getBoolean("hills", true);
        terenTekstura = prefs.getBoolean("terrainTexture", false);
        tankPoskodba = prefs.getBoolean("tankDamage", true);
        tankPrikazHP = prefs.getBoolean("tankShowHP", false);
        tankBomba = prefs.getBoolean("tankClusterBomb", true);
        tankBlisk = prefs.getBoolean("tankFlash", false);
        bombaBlisk = prefs.getBoolean("bombFlash", false);
        tankIzgled = prefs.getBoolean("tankAppearance", false);
        partikli = prefs.getBoolean("particles", false);
        aiMenjaOrozje = prefs.getBoolean("aiMenjaOrozje", false);
        zvok = prefs.getBoolean("zvok", true);
        velikostZrn = prefs.getInt("grainSize", 3);
        ozadjeId = prefs.getInt("ozadjeId", 0);
        terenId = prefs.getInt("terenId", 0);
        teksturaId = prefs.getInt("teksturaId", 0);
        aiOrozjeId = prefs.getInt("aiOrozjeId", 0);
        tankiHPId = prefs.getInt("tankiHPId", 1);

        r1.check(tip1);
        r2.check(tip2);
        r3.check(tip3);
        r4.check(tip4);
        r5.check(tip5);
        r6.check(tip6);
        r7.check(tip7);
        r8.check(tip8);
        r9.check(tip9);

        i1.setText(ime1);
        i2.setText(ime2);
        i3.setText(ime3);
        i4.setText(ime4);
        i5.setText(ime5);
        i6.setText(ime6);
        i7.setText(ime7);
        i8.setText(ime8);
        i9.setText(ime9);

        b1.setBackgroundColor(c1);
        b2.setBackgroundColor(c2);
        b3.setBackgroundColor(c3);
        b4.setBackgroundColor(c4);
        b5.setBackgroundColor(c5);
        b6.setBackgroundColor(c6);
        b7.setBackgroundColor(c7);
        b8.setBackgroundColor(c8);
        b9.setBackgroundColor(c9);

        obl.setChecked(soOblaki);
        hills.setChecked(hribcki);
        terrainTexture.setChecked(terenTekstura);
        tankDamage.setChecked(tankPoskodba);
        tankShowHP.setChecked(tankPrikazHP);
        tankClusterBomb.setChecked(tankBomba);
        tankFlash.setChecked(tankBlisk);
        bombFlash.setChecked(bombaBlisk);
        tankAppearance.setChecked(tankIzgled);
        particles.setChecked(partikli);
        tankAiChangeWeapon.setChecked(aiMenjaOrozje);
        sound.setChecked(zvok);

        grainSize.setValue(velikostZrn);

        tankWeapon.setSelection(aiOrozjeId);
        tankHP.setSelection(tankiHPId);

        o = Bitmap.createBitmap(160, 90, Bitmap.Config.ARGB_8888);
        t = Bitmap.createBitmap(160, 90, Bitmap.Config.ARGB_8888);
        x = Bitmap.createBitmap(160, 90, Bitmap.Config.ARGB_8888);
        ozadje.setImageBitmap(o);
        teren.setImageBitmap(t);
        tekstura.setImageBitmap(x);

        updateBackground();
        updateTerrain();
        updateTexture();

        if (tankDamage.isChecked()) {
            tankClusterBomb.setEnabled(true);
            tankClusterBomb.setChecked(tankBomba);
            tankShowHP.setEnabled(true);
            tankShowHP.setChecked(tankPrikazHP);
            tankFlash.setEnabled(true);
            tankFlash.setChecked(tankBlisk);
            tankHP.setEnabled(true);
            tankHP.setSelection(tankiHPId);
        } else {
            tankClusterBomb.setChecked(false);
            tankClusterBomb.setEnabled(false);
            tankShowHP.setChecked(false);
            tankShowHP.setEnabled(false);
            tankFlash.setChecked(false);
            tankFlash.setEnabled(false);
            tankHP.setEnabled(false);
        }

        if (tankAiChangeWeapon.isChecked()) {
            tankWeapon.setEnabled(false);
        } else {
            tankWeapon.setEnabled(true);
        }
        Log.w("Tanki", "Nastavitve uspešno naložene");
    }

    public void saveSettings(String profil) {
        SharedPreferences prefs = this.getSharedPreferences(profil, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        tip1 = r1.getCheckedRadioButtonId();
        tip2 = r2.getCheckedRadioButtonId();
        tip3 = r3.getCheckedRadioButtonId();
        tip4 = r4.getCheckedRadioButtonId();
        tip5 = r5.getCheckedRadioButtonId();
        tip6 = r6.getCheckedRadioButtonId();
        tip7 = r7.getCheckedRadioButtonId();
        tip8 = r8.getCheckedRadioButtonId();
        tip9 = r9.getCheckedRadioButtonId();

        ime1 = i1.getText().toString();
        ime2 = i2.getText().toString();
        ime3 = i3.getText().toString();
        ime4 = i4.getText().toString();
        ime5 = i5.getText().toString();
        ime6 = i6.getText().toString();
        ime7 = i7.getText().toString();
        ime8 = i8.getText().toString();
        ime9 = i9.getText().toString();

        soOblaki = obl.isChecked();
        hribcki = hills.isChecked();
        terenTekstura = terrainTexture.isChecked();
        tankPoskodba = tankDamage.isChecked();
        tankPrikazHP = tankShowHP.isChecked();
        tankBomba = tankClusterBomb.isChecked();
        tankBlisk = tankFlash.isChecked();
        bombaBlisk = bombFlash.isChecked();
        tankIzgled = tankAppearance.isChecked();
        partikli = particles.isChecked();
        aiMenjaOrozje = tankAiChangeWeapon.isChecked();
        zvok = sound.isChecked();

        velikostZrn = grainSize.getValue();

        editor.putInt("tip1", tip1);
        editor.putInt("tip2", tip2);
        editor.putInt("tip3", tip3);
        editor.putInt("tip4", tip4);
        editor.putInt("tip5", tip5);
        editor.putInt("tip6", tip6);
        editor.putInt("tip7", tip7);
        editor.putInt("tip8", tip8);
        editor.putInt("tip9", tip9);

        editor.putInt("c1", c1);
        editor.putInt("c2", c2);
        editor.putInt("c3", c3);
        editor.putInt("c4", c4);
        editor.putInt("c5", c5);
        editor.putInt("c6", c6);
        editor.putInt("c7", c7);
        editor.putInt("c8", c8);
        editor.putInt("c9", c9);

        editor.putString("ime1", ime1);
        editor.putString("ime2", ime2);
        editor.putString("ime3", ime3);
        editor.putString("ime4", ime4);
        editor.putString("ime5", ime5);
        editor.putString("ime6", ime6);
        editor.putString("ime7", ime7);
        editor.putString("ime8", ime8);
        editor.putString("ime9", ime9);

        editor.putBoolean("clouds", soOblaki);
        editor.putBoolean("hills", hribcki);
        editor.putBoolean("terrainTexture", terenTekstura);
        editor.putBoolean("tankDamage", tankPoskodba);
        editor.putBoolean("tankShowHP", tankPrikazHP);
        editor.putBoolean("tankClusterBomb", tankBomba);
        editor.putBoolean("tankFlash", tankBlisk);
        editor.putBoolean("bombFlash", bombaBlisk);
        editor.putBoolean("tankAppearance", tankIzgled);
        editor.putBoolean("particles", partikli);
        editor.putBoolean("aiMenjaOrozje", aiMenjaOrozje);
        editor.putBoolean("zvok", zvok);

        editor.putInt("grainSize", velikostZrn);

        editor.putInt("ozadjeId", ozadjeId);
        editor.putInt("terenId", terenId);
        editor.putInt("teksturaId", teksturaId);

        editor.putInt("aiOrozjeId", aiOrozjeId);
        editor.putInt("tankiHPId", tankiHPId);

        editor.apply();
    }

    public void saveAsProfile(View v) {
        String ime = profileName.getText().toString();
        ime = "net.ddns.iztok.tanki_" + ime;
        saveSettings(ime);
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
    }

    public void loadProfile(View v) {
    	String ime = profileList[(int)profiles.getSelectedItemId()];
    	Log.w("Tanki", "Izbrano: " + ime);
    	loadSettings(ime);
	}

    @Override
    public void onPause() {
        super.onPause();
        saveSettings("net.ddns.iztok.tanki");
    }

    public void ozadjeNazaj(View v) {
        ozadjeId = (ozadjeId - 1) % ozadja.size();
        if (ozadjeId == -1) ozadjeId = ozadja.size() - 1;
        updateBackground();
    }

    public void ozadjeNaprej(View v) {
        ozadjeId = (ozadjeId + 1) % ozadja.size();
        updateBackground();
    }

    public void terenNazaj(View v) {
        terenId = (terenId - 1) % barveTerena.size();
        if (terenId == -1) terenId = barveTerena.size() - 1;
        updateTerrain();
    }

    public void terenNaprej(View v) {
        terenId = (terenId + 1) % barveTerena.size();
        updateTerrain();
    }

    public void teksturaNazaj(View v) {
        teksturaId = (teksturaId - 1) % barveTeksture.size();
        if (teksturaId == -1) teksturaId = barveTeksture.size() - 1;
        updateTexture();
    }

    public void teksturaNaprej(View v) {
        teksturaId = (teksturaId + 1) % barveTeksture.size();
        updateTexture();
    }

    public void updateBackground() {
        oCan = new Canvas(o);
        Bitmap oz = ozadja.get(ozadjeId);
        oCan.drawBitmap(oz, new Rect(0, 0, oz.getWidth(), oz.getHeight()), new Rect(0, 0, o.getWidth(), o.getHeight()), paint);
        ozadje.invalidate();
    }

    public void updateTerrain() {
        tCan = new Canvas(t);
        int tn = barveTerena.get(terenId);
        tCan.drawRGB(Color.red(tn), Color.green(tn), Color.blue(tn));
        teren.invalidate();
    }

    public void updateTexture() {
        xCan = new Canvas(x);
        int tx = barveTeksture.get(teksturaId);
        xCan.drawRGB(Color.red(tx), Color.green(tx), Color.blue(tx));
        tekstura.invalidate();
    }

    public void barva(View v) {
        ColorDialog dialog = new ColorDialog();
        switch (v.getId()) {
            case R.id.b1:
                igralec = 1;
                break;
            case R.id.b2:
                igralec = 2;
                break;
            case R.id.b3:
                igralec = 3;
                break;
            case R.id.b4:
                igralec = 4;
                break;
            case R.id.b5:
                igralec = 5;
                break;
            case R.id.b6:
                igralec = 6;
                break;
            case R.id.b7:
                igralec = 7;
                break;
            case R.id.b8:
                igralec = 8;
                break;
            case R.id.b9:
                igralec = 9;
                break;
        }
        dialog.showDialog(this, igralec);

    }

    public class ColorDialog {
        public TextView tekst;
        private Button g1, g2, g3, g4, g5, g6, g7, g8, g9, g10, g11, g12, g13, g14, g15, g16, g17, g18, g19, g20, g21, g22, g23, g24, g25, g26, g27, g28, g29, g30, g31, g32, g33, g34, g35, g36, g37, g38, g39, g40;

        public void showDialog(Activity a, byte b) {
            final Dialog dialog = new Dialog(a);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.color_dialog);

            tekst = (TextView)dialog.findViewById(R.id.dialog_tekst);

            tekst.setText("Izberi barvo igralca " + Byte.toString(b) + ":");

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorDrawable cd = (ColorDrawable)v.getBackground();
                    int c = cd.getColor();
                    Log.w("Tanki", "Igralec: " + Integer.toString(igralec));
                    switch (igralec) {
                        case 1:
                            c1 = c;
                            b1.setBackgroundColor(c);
                            Log.w("Tanki", "Barva igralca 1 nastavljena na rgb(" + Integer.toString(Color.red(c)) + ", " + Integer.toString(Color.green(c)) + ", " + Integer.toString(Color.blue(c)) + ")");
                            break;
                        case 2:
                            c2 = c;
                            b2.setBackgroundColor(c);
                            break;
                        case 3:
                            c3 = c;
                            b3.setBackgroundColor(c);
                            break;
                        case 4:
                            c4 = c;
                            b4.setBackgroundColor(c);
                            break;
                        case 5:
                            c5 = c;
                            b5.setBackgroundColor(c);
                            break;
                        case 6:
                            c6 = c;
                            b6.setBackgroundColor(c);
                            break;
                        case 7:
                            c7 = c;
                            b7.setBackgroundColor(c);
                            break;
                        case 8:
                            c8 = c;
                            b8.setBackgroundColor(c);
                            break;
                        case 9:
                            c9 = c;
                            b9.setBackgroundColor(c);
                            break;
                        default:
                            Log.e("Tanki", "Le zakaj je igralec " + igralec + "?");
                    }
                    dialog.dismiss();
                }
            };

            g1 = dialog.findViewById(R.id.g1);
            g2 = dialog.findViewById(R.id.g2);
            g3 = dialog.findViewById(R.id.g3);
            g4 = dialog.findViewById(R.id.g4);
            g5 = dialog.findViewById(R.id.g5);
            g6 = dialog.findViewById(R.id.g6);
            g7 = dialog.findViewById(R.id.g7);
            g8 = dialog.findViewById(R.id.g8);
            g9 = dialog.findViewById(R.id.g9);
            g10 = dialog.findViewById(R.id.g10);
            g11 = dialog.findViewById(R.id.g11);
            g12 = dialog.findViewById(R.id.g12);
            g13 = dialog.findViewById(R.id.g13);
            g14 = dialog.findViewById(R.id.g14);
            g15 = dialog.findViewById(R.id.g15);
            g16 = dialog.findViewById(R.id.g16);
            g17 = dialog.findViewById(R.id.g17);
            g18 = dialog.findViewById(R.id.g18);
            g19 = dialog.findViewById(R.id.g19);
            g20 = dialog.findViewById(R.id.g20);
            g21 = dialog.findViewById(R.id.g21);
            g22 = dialog.findViewById(R.id.g22);
            g23 = dialog.findViewById(R.id.g23);
            g24 = dialog.findViewById(R.id.g24);
            g25 = dialog.findViewById(R.id.g25);
            g26 = dialog.findViewById(R.id.g26);
            g27 = dialog.findViewById(R.id.g27);
            g28 = dialog.findViewById(R.id.g28);
            g29 = dialog.findViewById(R.id.g29);
            g30 = dialog.findViewById(R.id.g30);
            g31 = dialog.findViewById(R.id.g31);
            g32 = dialog.findViewById(R.id.g32);
            g33 = dialog.findViewById(R.id.g33);
            g34 = dialog.findViewById(R.id.g34);
            g35 = dialog.findViewById(R.id.g35);
            g36 = dialog.findViewById(R.id.g36);
            g37 = dialog.findViewById(R.id.g37);
            g38 = dialog.findViewById(R.id.g38);
            g39 = dialog.findViewById(R.id.g39);
            g40 = dialog.findViewById(R.id.g40);
            g1.setOnClickListener(listener);
            g2.setOnClickListener(listener);
            g3.setOnClickListener(listener);
            g4.setOnClickListener(listener);
            g5.setOnClickListener(listener);
            g6.setOnClickListener(listener);
            g7.setOnClickListener(listener);
            g8.setOnClickListener(listener);
            g9.setOnClickListener(listener);
            g10.setOnClickListener(listener);
            g11.setOnClickListener(listener);
            g12.setOnClickListener(listener);
            g13.setOnClickListener(listener);
            g14.setOnClickListener(listener);
            g15.setOnClickListener(listener);
            g16.setOnClickListener(listener);
            g17.setOnClickListener(listener);
            g18.setOnClickListener(listener);
            g19.setOnClickListener(listener);
            g20.setOnClickListener(listener);
            g21.setOnClickListener(listener);
            g22.setOnClickListener(listener);
            g23.setOnClickListener(listener);
            g24.setOnClickListener(listener);
            g25.setOnClickListener(listener);
            g26.setOnClickListener(listener);
            g27.setOnClickListener(listener);
            g28.setOnClickListener(listener);
            g29.setOnClickListener(listener);
            g30.setOnClickListener(listener);
            g31.setOnClickListener(listener);
            g32.setOnClickListener(listener);
            g33.setOnClickListener(listener);
            g34.setOnClickListener(listener);
            g35.setOnClickListener(listener);
            g36.setOnClickListener(listener);
            g37.setOnClickListener(listener);
            g38.setOnClickListener(listener);
            g39.setOnClickListener(listener);
            g40.setOnClickListener(listener);
            dialog.show();
        }

    }

}
