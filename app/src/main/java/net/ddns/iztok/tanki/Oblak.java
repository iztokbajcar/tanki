package net.ddns.iztok.tanki;

import android.graphics.Bitmap;

public class Oblak {
	public float x;
	public int y, w, h;
	public float v;  // Modifikator hitrosti
	public Bitmap slika;

	public Oblak(float xPos, int yPos, int width, int height, Bitmap bmp, float vel) {
		x = xPos;
		y = yPos;
		w = width;
		h = height;
		slika = bmp;
		v = vel;
	}
}
