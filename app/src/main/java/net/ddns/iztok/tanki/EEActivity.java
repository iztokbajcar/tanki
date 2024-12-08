package net.ddns.iztok.tanki;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class EEActivity extends Activity implements SensorEventListener {

	private Canvas can;
	private Bitmap bmp;
	private Handler handler;
	private ImageView iv;
	private Paint paint;
	private Runnable drawRunnable;
	private Sensor accel;
	private SensorManager sensorManager;
	private float x = 0;
	private float y = 0;
	private float z = 0;
	private int bmpWidth = 1920;
	private int bmpHeight = 1080;
	private int krogR = 50;
	private int krogX = 50;
	private int krogY = 50;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ee);

		iv = findViewById(R.id.ee_iv);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		handler = new Handler();

		bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		iv.setImageBitmap(bmp);
		can = new Canvas(bmp);
		paint = new Paint();

		drawRunnable = new Runnable() {
			@Override
			public void run() {
				can.drawRGB(0, 0, 0);
				paint.setColor(Color.RED);
				can.drawCircle(krogX, krogY, krogR, paint);
				paint.setColor(Color.WHITE);
				can.drawText("X: " + x, 10, 10, paint);
				can.drawText("Y: " + y, 10, 25, paint);
				can.drawText("Z: " + z, 10, 40, paint);
				iv.invalidate();
				handler.post(drawRunnable);
			}
		};

		handler.post(drawRunnable);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			x = 2 * sensorEvent.values[0];
			y = 2 * sensorEvent.values[1];
			//z = sensorEvent.values[2];
			if (y > 0) {
				if (krogX + y <= bmpWidth - krogR) {
					krogX += y;
				} else {
					krogX = bmpWidth - krogR;
				}
			} else {
				if (krogX + y >= krogR) {
					krogX += y;
				} else {
					krogX = krogR;
				}
			}
			if (x > 0) {
				if (krogY + x <= bmpHeight - krogR) {
					krogY += x;
				} else {
					krogY = bmpHeight - krogR;
				}
			} else {
				if (krogY + x >= krogR) {
					krogY += x;
				} else {
					krogY = krogR;
				}
			}
		}
	}
}
