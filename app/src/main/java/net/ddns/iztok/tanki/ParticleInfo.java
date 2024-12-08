package net.ddns.iztok.tanki;

public class ParticleInfo {
	public int w;
	public int c;
	public int f, g, rot;
	public int[] dPos = {0, 0};
	public int dWidth = 0;
	public int[] dColor = {0, 0, 0};

	public ParticleInfo(int[] deltaPos, int width, int deltaWidth, int color, int[] deltaColor, int fadeAmount, int gravity, int rotation) {
		dPos = deltaPos;
		w = width;
		dWidth = deltaWidth;
		c = color;
		dColor = deltaColor;
		f = fadeAmount;
		g = gravity;
		rot = 0; // TODO implementiraj obračanje
	}
}
