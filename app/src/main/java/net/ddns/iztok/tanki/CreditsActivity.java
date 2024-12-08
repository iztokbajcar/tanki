package net.ddns.iztok.tanki;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CreditsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credits);

		TextView author = findViewById(R.id.creditsAuthor);
		TextView sounds = findViewById(R.id.creditsSounds);
		author.setText(R.string.creditsAuthor);
		sounds.setText(R.string.creditsSounds);
	}
}
