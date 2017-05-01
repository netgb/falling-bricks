package com.ayautilities.misc.games.fallingbricks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class HighScoresActivity extends ActionBarActivity implements TextActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_text);
	}

	@Override
	public Object getTextForFragment() {
		StringBuilder text = new StringBuilder();
		SharedPreferences highScoresStore = getSharedPreferences(
				GameRestoreFragment.PREF_FILE_NAME, MODE_PRIVATE);
		text.append("Total: ").append(highScoresStore.getInt(
				GameRestoreFragment.HIGH_SCORE_PREF_KEY_TOTAL, 0)).append('\n');
		for (int i = 1; i <= GameRestoreFragment.LEVEL_COUNT; i++) {
			text.append("Level ").append(i).append(": ")
				.append(highScoresStore.getInt(String.format(
					GameRestoreFragment.HIGH_SCORE_PREF_KEY_SINGLE_FORMAT, i), 0))
				.append('\n');
		}
		return text.toString();
	}
}
