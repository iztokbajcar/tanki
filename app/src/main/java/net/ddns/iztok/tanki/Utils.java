package net.ddns.iztok.tanki;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;

import java.io.IOException;

public class Utils {

    // Vrne razdaljo med dvema točkama
    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((Math.max(x1, x2) - Math.min(x1, x2)) * (Math.max(x1, x2) - Math.min(x1, x2)) + (Math.max(y1, y2) - Math.min(y1, y2)) * (Math.max(y1, y2) - Math.min(y1, y2)));
    }

    // Preveri, ali se dana pravokotnika sekata (prekrivata)
    public static boolean prekrivanje(Rect r1, Rect r2) {
        if (r1.left > r2.right || r2.left > r1.right)
            return false;
        if (r1.top > r2.bottom || r2.top > r1.bottom)
            return false;
        return true;
    }

    // Predvaja zvok
    public static void playSound(final Context context, final String sound) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                MediaPlayer player = new MediaPlayer();
                try {
                    AssetFileDescriptor descriptor = context.getAssets().openFd("sounds/" + sound);
                    player.reset();
                    player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    player.prepare();
                    player.start();
                } catch (IOException e) {

                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
}
