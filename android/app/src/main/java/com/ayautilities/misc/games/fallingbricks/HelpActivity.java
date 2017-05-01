package com.ayautilities.misc.games.fallingbricks;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class HelpActivity extends ActionBarActivity implements TextActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_text);
	}

	@Override
	public Object getTextForFragment() {
		return R.string.frag_text_help;
	}
}
