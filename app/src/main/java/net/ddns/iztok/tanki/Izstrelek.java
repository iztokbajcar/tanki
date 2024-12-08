package net.ddns.iztok.tanki;

import android.graphics.Color;

import java.util.Random;

public class Izstrelek {
    public int x, y; // Položaj izstrelka na ekranu (koordinati sredine izstrelka)
    public int r; // Radij izstrelka (krogle)
    public int c; // barva
    public int dm;
    public int dr;
    public int m = 1; // masa izstrelka (ni uporabljeno)
    public double fX = 0;
    public double fY = 0;
    public int d; // Določi, ali bo izstrelek uničil ali gradil teren
                   // -1 - s terenom ne stori nič
                   // 0 - uniči teren
                   // 1 - gradi teren
    private ParticleInfo particleInfo = null; // Partikel, ki ga izstrelek spušča za sabo
    public Tank t;  // Lastnik

    public static int[] tipi = {R.string.weapon_ball, R.string.weapon_bigball, R.string.weapon_tnt, R.string.weapon_three_balls, R.string.weapon_terrain, R.string.weapon_clusterbomb, R.string.weapon_doubleclusterbomb, R.string.weapon_doubleterrainclusterbomb, R.string.weapon_thermonuclear20, R.string.weapon_thermonuclear50 /*, R.string.weapon_obliterator*/};

    public void setPos(int xPos, int yPos) {
        x = xPos;
        y = yPos;
    }


    // Privzeti konstruktor
    public Izstrelek(Tank t) {
        x = 0;
        y = 0;
        r = 0;
        c = 0;
        d = 0;
        dr = 0;
        this.t = t;
    }

    public Izstrelek(Tank t, int radius, int color, int damage, int damageRadius, int destroy) {
        x = 0;
        y = 0;
        r = radius;
        c = color;
        d = destroy;
        dm = damage;
        dr = damageRadius;
        this.t = t;
    }

    public void addForce(double x, double y) {
        fX += x;
        fY += y;
    }

    public void setParticleInfo(ParticleInfo info) {
        particleInfo = info;
    }

    public ParticleInfo getParticleInfo() {
        return particleInfo;
    }

    public Particle generateParticle() {
        if (particleInfo != null) {
            Random r = new Random();
            int red = Math.min(Color.red(particleInfo.c) + ((particleInfo.dColor[0] > 0) ? r.nextInt(particleInfo.dColor[0]) : 0) - particleInfo.dColor[0] / 2, 255);
            int green = Math.min(Color.green(particleInfo.c) + ((particleInfo.dColor[1] > 0) ? r.nextInt(particleInfo.dColor[1]) : 0) - particleInfo.dColor[1] / 2, 255);
            int blue = Math.min(Color.blue(particleInfo.c) + ((particleInfo.dColor[2] > 0) ? r.nextInt(particleInfo.dColor[2]) : 0) - particleInfo.dColor[2] / 2, 255);
            return new Particle(x, y, Color.rgb(red, green, blue), particleInfo);
        } else {
            return null;
        }
    }

}
