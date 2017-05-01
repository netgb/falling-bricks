package com.ayautilities.misc.games.fallingbricks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
/*#*/import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LevelFragment extends ListFragment {
	private static final String TAG = LevelFragment.class.getName();
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String[] myStringArray = new String[10];
		for (int i = 0; i < myStringArray.length; i++) {
			myStringArray[i] = getString(R.string.frag_level_text, i + 1);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
		        android.R.layout.simple_list_item_1, myStringArray);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		int level = position + 1;
		/*#*/Log.i(TAG, String.format("Selected Level %d.", level));

		Intent gameIntent = new Intent(getActivity(), GameActivity.class);
		gameIntent.putExtra(GameRestoreFragment.INTENT_EXTRA_DATA_KEY_LEVEL, level);
		startActivity(gameIntent);
	}
}
