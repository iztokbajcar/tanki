package net.ddns.iztok.tanki;

import android.graphics.Color;

import java.util.ArrayList;

public class Tank {

    public byte aiType = 1;
    public byte orozje = 0; // Tip izstrelka
    public byte moc = 100;
    public boolean p = false;
    public boolean compFinished = false;
    public int x, y;  // Koordinati zgornjega levega kota
    public int h;
    public int c;
    public int hp = 100;
    public int rot = 90;
    public String n;

    public Tank(String name, int xPos, int yPos, int h, int color, int hitpoints) {
        n = name;
        x = xPos;
        y = yPos;
        c = color;
        hp = hitpoints;
    }

    public Tank(String name, int xPos, int yPos, int h, int color, int hitpoints, boolean playerControlled) {
        n = name;
        if (n.equals("TestAI")) aiType = 2;
        x = xPos;
        y = yPos;
        c = color;
        hp = hitpoints;
        p = playerControlled;
    }

    public ArrayList<Izstrelek> strel() {
        // FIXME izstrelek se ne pojavi vedno v topu
        //Izstrelek izstrelek;
        ArrayList<Izstrelek> izstrelki = new ArrayList<>();
        int originX = x + h;
        Izstrelek izstrelek;
        switch (orozje) {
            case 1: // Velik izstrelek
                izstrelek = new Izstrelek(this,6, Color.rgb(0, 0, 0), 110, 50, 0);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(0, 0, 0), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 6);
                break;
            case 2: // Eksploziven izstrelek
                izstrelek = new Izstrelek(this, 4, Color.rgb(128, 0, 0), 150, 80, 0);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 500, Color.rgb(255, 0, 0), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
                break;
            case 3: // Trije navadni izstrelki
                izstrelek = new Izstrelek(this, 4, Color.rgb(0, 0, 255), 100, 40, 0);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(0, 128, 128), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX - 20, y - 6);
                izstrelek = new Izstrelek(this, 4, Color.rgb(0, 0, 255), 100, 40, 0);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(0, 128, 128), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 6);
                izstrelek = new Izstrelek(this, 4, Color.rgb(0, 0, 255), 100, 40, 0);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(0, 128, 128), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX + 20, y - 6);
                break;
            case 4: // Teren
                izstrelek = new Izstrelek(this, 4, Color.rgb(128, 128, 0), 100, 40, 1);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(128, 255, 0), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
                break;
            case 5: // Cluster bomb (3 bombe)
                izstrelek = new ClusterBomb(this, 4, Color.BLACK, 100, 40, 0, new Izstrelek(this, 4, Color.BLACK, 100, 40, 0), 3);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(128, 128, 128), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
                break;
            case 6: // Dvojni cluster bomb
                izstrelek = new ClusterBomb(this, 4, Color.BLACK, 100, 40, 0, new ClusterBomb(this, 4, Color.BLACK, 100, 40, 0, new Izstrelek(this, 4, Color.BLACK, 50, 40, 0), 3), 3);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(128, 128, 128), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
                break;
            case 7: // Dvojni cluster bomb (teren)
                izstrelek = new ClusterBomb(this, 4, Color.BLACK, 100, 40, 0, new ClusterBomb(this, 4, Color.BLACK, 100, 40, 1, new Izstrelek(this, 4, Color.rgb(128, 128, 0), 100, 40, 1), 3), 3);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(128, 128, 128), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
                break;
            case 8: // Termonuklearna 20
                izstrelek = new Thermonuclear(this, 4, Color.rgb(0, 255, 0), 20, 20, 0);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(0, 255, 0), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
                break;
            case 9: // Termonuklearna 50
                izstrelek = new Thermonuclear(this, 4, Color.rgb(0, 255, 0), 50, 20, 0);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.rgb(0, 255, 0), new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
                break;
            /*case 10: // Obliterat0r
                izstrelek = new Izstrelek(10, Color.rgb(255, 255, 255), 0, 200, 0);
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
                break;*/
            default: // Navaden izstrelek
                izstrelek = new Izstrelek(this, 4, Color.BLACK, 100, 40, 0);
                izstrelek.setParticleInfo(new ParticleInfo(new int[]{0, 0}, 10, 5, Color.WHITE, new int[]{0, 0, 0}, 25, 5, 10));
                izstrelki.add(izstrelek);
                izstrelek.setPos(originX, y - 4);
        }
        double fX = Math.sin(Math.toRadians(rot)) * moc;
        double fY = -Math.cos(Math.toRadians(rot)) * moc;
        for (Izstrelek i : izstrelki) {
            i.addForce(fX, fY);
        }
        return izstrelki;
    }

}
