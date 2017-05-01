package com.ayautilities.misc.games.fallingbricks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class GameQuitConfirmFragment extends DialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final GameActivity activity = (GameActivity)getActivity();
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.game_quit_title)
        		.setMessage(R.string.game_quit_message)
               .setPositiveButton(R.string.game_quit_yes_option, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       activity.quitGame();
                   }
               })
               .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       getDialog().cancel();
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
	}
}
