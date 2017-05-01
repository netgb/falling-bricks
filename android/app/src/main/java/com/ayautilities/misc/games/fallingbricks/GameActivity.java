package com.ayautilities.misc.games.fallingbricks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
/*#*/import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class GameActivity extends FragmentActivity {
	private static final String TAG = GameActivity.class.getName();
	
	private GameView.GameHandler gameHandler;
	
	private GameView gameView;
	private TextView pauseScreen;
	
	private GameRestoreFragment gameRestoreFragment;
	private GameQuitConfirmFragment gameQuitConfirmFrag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game);
		
		gameView = (GameView)findViewById(R.id.gameview);
		pauseScreen = (TextView)findViewById(R.id.pause_screen);

		gameHandler = gameView.getGameHandler();
		gameHandler.setQuitRunnable(new Runnable() {
			
			@Override
			public void run() {
				quitGame();
			}
		});
		
		postConfigurationChange(getResources().getConfiguration());
		
		gameRestoreFragment = (GameRestoreFragment)getSupportFragmentManager(
				).findFragmentByTag(GameRestoreFragment.class.getName());		
		boolean resuming = gameRestoreFragment != null;
		
		if (!resuming) {
			gameRestoreFragment = new GameRestoreFragment();
			getSupportFragmentManager().beginTransaction().add(gameRestoreFragment, 
					GameRestoreFragment.class.getName()).commit();
			int initialLevel = getIntent().getIntExtra(
					GameRestoreFragment.INTENT_EXTRA_DATA_KEY_LEVEL, 1);
			/*#*/Log.d(TAG, "Initial level: " + initialLevel);
			gameRestoreFragment.setLevel(initialLevel);
		}
		loadHighScores();
		gameHandler.postRestoreState(gameRestoreFragment);
		
		gameRestoreFragment.setGamePaused(true);
		pauseScreen.setText(resuming ? R.string.mode_resume : R.string.mode_start);
		
		pauseScreen.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// No need to process ACTION_UP and have
				// resumeGame() called twice.
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					resumeGame();
				}
				return true;
			}
		});
		
		View.OnKeyListener keyListener = new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				int action = event.getAction();
				if (v == pauseScreen) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
							keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
							keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
							keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						// No need to process ACTION_UP and have
						// resumeGame() called twice.
						if (action == KeyEvent.ACTION_DOWN) {
							resumeGame();
						}
						return true;
					}
					else {
						return false;
					}
				}
				else {
					// Some Android devices (e.g. Sony Xperia) do not continuously send
					// key events while key is held down,
					// but all do send an initial ACTION_DOWN and a final ACTION_UP
					// so we use these two only and ignore ACTION_MOVEs.
					if (action == KeyEvent.ACTION_UP) {
						gameHandler.postCancelUserInput();
					}
					else if (action == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							gameHandler.postRotate();
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							gameHandler.postMoveDown();
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							gameHandler.postMoveLeft();
							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							gameHandler.postMoveRight();
							break;
						default:
							return false;
						}
					}
					return true;
				}
			}
		};
		pauseScreen.setOnKeyListener(keyListener);
		gameView.setOnKeyListener(keyListener);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		postConfigurationChange(newConfig);
	}
		
	private void postConfigurationChange(Configuration newConfig) {			
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			gameHandler.postOrientationChanged(true);
		}
		else {
			gameHandler.postOrientationChanged(false);
		}
		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
			gameHandler.postKeyboardHidden(false);
		}
		else {
			gameHandler.postKeyboardHidden(true);
		}
	}

	@Override
	public void onBackPressed() {
		/*#*/Log.d(TAG, "Back key pressed.");
		
		if (!gameRestoreFragment.isGamePaused()) {
			pauseGame();
		}
		else {
			if (gameQuitConfirmFrag == null) {
				gameQuitConfirmFrag = new GameQuitConfirmFragment();
			}
			gameQuitConfirmFrag.show(getSupportFragmentManager(),
					GameQuitConfirmFragment.class.getName());
		}
	}
	
	public void quitGame() {
		updateAndSaveHighScores();
		Intent homeIntent = new Intent(this, MainActivity.class);
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeIntent);
	}
	
	public void loadHighScores() {
		SharedPreferences highScoresStore = getSharedPreferences(
				GameRestoreFragment.PREF_FILE_NAME, MODE_PRIVATE);
		int[] highScores = gameRestoreFragment.getHighScores();
		highScores[0] = highScoresStore.getInt(
				GameRestoreFragment.HIGH_SCORE_PREF_KEY_TOTAL, 0);
		for (int i = 1; i < highScores.length; i++) {
			highScores[i] = highScoresStore.getInt(String.format(
					GameRestoreFragment.HIGH_SCORE_PREF_KEY_SINGLE_FORMAT, i), 0);
		}
	}
	
	public void updateAndSaveHighScores() {
		// Call setEdited() so reads and writes by surface thread are
		// observable in ui thread.
		
		gameRestoreFragment.setEdited(true);
		int[] highScores = gameRestoreFragment.getHighScores();
		int[] scores = gameRestoreFragment.getScores();
		for (int i = 0; i < highScores.length; i++) {
			highScores[i] = Math.max(highScores[i], scores[i]);
		}
		
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences highScoresStore = getSharedPreferences(
				GameRestoreFragment.PREF_FILE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = highScoresStore.edit();

		editor.putInt(
				GameRestoreFragment.HIGH_SCORE_PREF_KEY_TOTAL, highScores[0]);
		for (int i = 1; i < highScores.length; i++) {
			editor.putInt(String.format(
					GameRestoreFragment.HIGH_SCORE_PREF_KEY_SINGLE_FORMAT, i), highScores[i]);
		}
		
		// Commit the edits!
		editor.commit();
	}

	private void resumeGame() {
		gameHandler.postGamePaused(false);
		gameRestoreFragment.setGamePaused(false);
		pauseScreen.setVisibility(View.GONE);
		gameView.setVisibility(View.VISIBLE);
	}
	
	private void pauseGame() {
		gameHandler.postGamePaused(true);
		gameRestoreFragment.setGamePaused(true);
		pauseScreen.setText(R.string.mode_resume);
		gameView.setVisibility(View.GONE);
		pauseScreen.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		pauseGame();
		gameHandler.saveState(gameRestoreFragment);
	}
	
	@Override 
	protected void onDestroy() {
		super.onDestroy();
		
		/*#*/Log.i(TAG, "Shutting down game handler...");		
		gameHandler.shutdown();
	}
}
