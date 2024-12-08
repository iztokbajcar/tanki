package net.ddns.iztok.tanki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    LinearLayout root;
    TextView naslov;
    int sirina = 0;
    int visina = 0;
    String koda = "";
    Typeface goodtimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = findViewById(R.id.root);
        naslov = findViewById(R.id.naslov);

        goodtimes = Typeface.createFromAsset(getAssets(), "goodtimes.ttf");
        naslov.setTypeface(goodtimes);
    }

    public void newGame(View v) {
        Intent intent = new Intent();
        intent.setClass(this, GameActivity.class);
        startActivity(intent);
    }

    public void settings(View v) {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void credits(View v) {
        Intent intent = new Intent();
        intent.setClass(this, CreditsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (sirina == 0 || visina == 0) {
                    sirina = root.getMeasuredWidth();
                    visina = root.getMeasuredHeight();
                }
                if (x >= 0 && x <= 200 && y >= 0 && y <= 200) {  // Zgoraj levo
                    koda += "1";
                }
                if (x >= sirina - 200 && x <= sirina && y >= 0 && y <= 200) {  // Zgoraj desno
                    koda += "2";
                }
                if (x >= 0 && x <= 200 && y >= visina - 200 && y <= visina) {  // Spodaj levo
                    koda += "4";
                }
                if (x >= sirina - 200 && x <= sirina && y >= visina - 200 && y <= visina) {  // Spodaj desno
                    koda += "3";
                }
                if (koda.length() == 7) koda = koda.substring(1);
                if (koda.indexOf("123424") != -1) {
                    Intent i = new Intent();
                    i.setClass(this, ServerActivity.class);
                    startActivity(i);
                }
                break;
        }
        return super.onTouchEvent(event);
    }


}
