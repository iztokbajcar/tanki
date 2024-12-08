package net.ddns.iztok.tanki;

import java.io.Serializable;

public class SettingsProfile implements Serializable {

	private String n;
	private String[] t;
	private int maxHP, bg, tr, tt;
	private int[] b;
	private int[] tipi;
	private boolean veter, hribi, tekstura, tankiPoskodba, prikaziHP, cb, tankBlisk, bombaBlisk, izgled, AImenjaOrozje;

	public SettingsProfile(String name, String[] tankNames, int maxHealth, int background, int terrainColor, int textureColor, int[] tankColors, int[] playerTypes, boolean wind, boolean hills, boolean texture, boolean damage, boolean showHP, boolean clusterB, boolean tankFlash, boolean bombFlash, boolean look, boolean AIChangeWeapons) {
		n = name;
		t = tankNames;
		maxHP = maxHealth;
		bg = background;
		tr = terrainColor;
		tt = textureColor;
		b = tankColors;
		tipi = playerTypes;
		veter = wind;
		hribi = hills;
		tekstura = texture;
		tankiPoskodba = damage;
		prikaziHP = showHP;
		cb = clusterB;
		tankBlisk = tankFlash;
		bombaBlisk = bombFlash;
		izgled = look;
		AImenjaOrozje = AIChangeWeapons;
	}

}
