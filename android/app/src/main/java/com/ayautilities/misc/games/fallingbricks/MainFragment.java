package com.ayautilities.misc.games.fallingbricks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
/*#*/import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainFragment extends ListFragment {
	private static final String TAG = MainFragment.class.getName();
	private static final int btnNew = 0, btnHelp = 1, btnHighScores = 2, btnAbout = 3, btnExit = 4;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String[] myStringArray = getResources().getStringArray(R.array.activity_main_content);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
		        android.R.layout.simple_list_item_1, myStringArray);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
		case btnNew:
			startActivity(new Intent(getActivity(), LevelActivity.class));
			break;
		case btnHelp:
			startActivity(new Intent(getActivity(), HelpActivity.class));
			break;
		case btnHighScores:
			startActivity(new Intent(getActivity(), HighScoresActivity.class));
			break;
		case btnAbout:
			startActivity(new Intent(getActivity(), AboutActivity.class));
			break;
		case btnExit:
			getActivity().finish();
			break;
		default:
			/*#*/Log.w(TAG, "Unexpected position: " + position);
			break;
		}
	}
}
