package net.ddns.iztok.tanki;

public class ClusterBomb extends Izstrelek {

    public int bombN; // Koliko bomb se izvrže ob zadetku
    public Izstrelek bomb = null; // Izstrelek, ki ga bomba izvrže
    public ClusterBomb cbomb = null;

    public ClusterBomb(Tank tank, int radius, int color, int damage, int damageRadius, int destroy, Izstrelek b, int bn) {
        super(tank, radius, color, damage, damageRadius, destroy);
        bomb = b;
        bombN = bn;
    }

    public ClusterBomb(Tank tank, int radius, int color, int damage, int damageRadius, int destroy, ClusterBomb b, int bn) {
        super(tank, radius, color, damage, damageRadius, destroy);
        cbomb = b;
        bombN = bn;
    }

}
