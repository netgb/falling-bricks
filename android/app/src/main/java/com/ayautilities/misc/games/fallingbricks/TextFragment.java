package com.ayautilities.misc.games.fallingbricks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextFragment extends Fragment {
	private TextView childView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_text, container,
				false);
		childView = (TextView)rootView.findViewById(R.id.frag_text_textview);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Object text = ((TextActivity)getActivity()).getTextForFragment();
		if (text instanceof String) {
			childView.setText((String)text);
		}
		else {
			childView.setText((int)text);
		}
	}
}
