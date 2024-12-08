package net.ddns.iztok.tanki;

import java.util.Random;

public class Particle {
	public int x, y, r;
	public int c;
	public int alpha = 255;
	public ParticleInfo info;

	public Particle(int xPos, int yPos, int color, ParticleInfo particleInfo) {
		x = xPos;
		y = yPos;
		c = color;
		info = particleInfo;
	}

	public void update() {
		alpha -= info.f;
		r += info.rot;
		y += info.g;
	}

}
