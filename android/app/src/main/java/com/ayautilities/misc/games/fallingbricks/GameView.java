package com.ayautilities.misc.games.fallingbricks;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
/*#*/import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ayautilities.misc.games.fallingbricks.game.FallingBricksGame;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = GameView.class.getName();
	
	public static final int ARROW_IMAGE_SIZE = 32;
	
	private float textSize;
	private int arrowImagePadding;
	private Bitmap upArrow, downArrow, leftArrow, rightArrow;
	private GameHandler gameHandler;
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GameView);
        textSize = a.getDimensionPixelSize(R.styleable.GameView_textSize, 12);
        a.recycle();
        
        Resources res = getResources();
        arrowImagePadding = res.getDimensionPixelSize(R.dimen.arrow_image_margin);
        upArrow = loadTile(res.getDrawable(R.drawable.dpad_up));
        downArrow = loadTile(res.getDrawable(R.drawable.dpad_down));
        leftArrow = loadTile(res.getDrawable(R.drawable.dpad_left));
        rightArrow = loadTile(res.getDrawable(R.drawable.dpad_right));
        
		// register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        
        setFocusable(true);
        
        Thread secondaryThread = new Thread() {
			
			@Override
			public void run() {
				Looper.prepare();
				
				synchronized (this) {
					gameHandler = new GameHandler(GameView.this);
					notify();
				}
				
				Looper.loop();
			}
		};
		secondaryThread.start();
		
		// Pause until game handler is created.
		synchronized (secondaryThread) {
			while (gameHandler == null) {
				try {
					secondaryThread.wait();
				} catch (InterruptedException e) { }
			}
		}
	}
	
	private static Bitmap loadTile(Drawable tile) {
        Bitmap bitmap = Bitmap.createBitmap(ARROW_IMAGE_SIZE,
        		ARROW_IMAGE_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tile.setBounds(0, 0, ARROW_IMAGE_SIZE, ARROW_IMAGE_SIZE);
        tile.draw(canvas);
        return bitmap;
    }

	public int getArrowImagePadding() {
		return arrowImagePadding;
	}

	public GameHandler getGameHandler() {
		return gameHandler;
	}
	
	public float getTextSize() {
		return textSize;
	}

	public Bitmap getLeftArrow() {
		return leftArrow;
	}

	public Bitmap getRightArrow() {
		return rightArrow;
	}

	public Bitmap getUpArrow() {
		return upArrow;
	}

	public Bitmap getDownArrow() {
		return downArrow;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/*#*/Log.d(TAG, String.format("widthMeasureSpec: %d; heightMeasureSpec: %d",
		/*#*/		widthMeasureSpec, heightMeasureSpec));
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		/*#*/Log.d(TAG, String.format("widthSpec: %d; heightSpec: %d",
		/*#*/		widthSize, heightSize));
		
		int width = Integer.MAX_VALUE;
        int height = Integer.MAX_VALUE;

        if (widthMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
        	/*#*/Log.d(TAG, "width mode = EXACTLY");
            width = widthSize;
        }
        else if (widthMode == MeasureSpec.AT_MOST) {
        	/*#*/Log.d(TAG, "width mode = AT_MOST");
            width = Math.min(widthSize, getSuggestedMinimumWidth());
        }
        else if (widthMode == MeasureSpec.UNSPECIFIED) {
        	/*#*/Log.d(TAG, "width mode = UNSPECIFIED");
        }
        
        if (heightMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
        	/*#*/Log.d(TAG, "height mode = EXACTLY");
            height = heightSize;
        }
        else if (heightMode == MeasureSpec.AT_MOST) {
        	/*#*/Log.d(TAG, "height mode = AT_MOST");
            height = Math.min(heightSize, getSuggestedMinimumHeight());
        }
        else if (heightMode == MeasureSpec.UNSPECIFIED) {
        	/*#*/Log.d(TAG, "height mode = UNSPECIFIED");
        }
        
        /*#*/Log.d(TAG, String.format("width: %d; height: %d", width, height));
		setMeasuredDimension(width, height);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gameHandler.postTouchEvent(event);
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		/*#*/Log.d(TAG, "surfaceCreated called.");
		gameHandler.postSurfaceCreated(holder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		/*#*/Log.d(TAG, "surfaceChanged called.");
		gameHandler.postSurfaceChanged(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		gameHandler.clearSurface();
	}

	public static class GameHandler extends Handler {		
		private static final int GAME_ADVANCE = 0, PAUSE = 2,
				RESUME = 3, MOVE_LEFT = 5, MOVE_RIGHT = 6,
				MOVE_DOWN = 7, ROTATE = 8, SURFACE_CHANGED = 9,
				SURFACE_CREATED = 10, USE_RESTORED_STATE = 12,
				SAVE_STATE = 13, CANCEL_USER_INPUT = 19, ORIENTATION_LANDSCAPE = 20,
				ORIENTATION_PORTRAIT = 21, TOUCH_EVENT = 23,
				KEYBOARD_HIDDEN = 24, KEYBOARD_SHOWN = 25;

		private final FallingBricksGame game;
		private boolean shutdown = false;
		
		public GameHandler(GameView gameView) {
			game = new FallingBricksGame(gameView);
		}
		
		public void postTouchEvent(MotionEvent event) {
			sendMessage(Message.obtain(this, TOUCH_EVENT, event));
		}

		public void setQuitRunnable(Runnable quitRunnable) {
			game.setQuitRunnable(quitRunnable);
		}
		
		public void shutdown() {
			getLooper().quit();
			shutdown = true;
		}
		
		public void saveState(GameRestoreFragment state) {
			/*#*/Log.d(TAG, "saveState started.");
			Object[] stateAndSaveStatus = new Object[]{ state, false };
			sendMessage(Message.obtain(this, SAVE_STATE, stateAndSaveStatus));
			synchronized (stateAndSaveStatus) {
				while (!((boolean)stateAndSaveStatus[1])) {
					try {
						stateAndSaveStatus.wait();
					}
					catch (InterruptedException e) {}
				}
			}
			/*#*/Log.d(TAG, "saveState finished.");
		}
		
		public void postRestoreState(GameRestoreFragment state) {
			sendMessage(Message.obtain(this, USE_RESTORED_STATE, state));
		}
		
		public void postGamePaused(boolean paused) {
			sendEmptyMessage(paused ? PAUSE : RESUME);
		}
		
		public void postMoveLeft() {
			sendEmptyMessage(MOVE_LEFT);
		}

		public void postCancelUserInput() {
			sendEmptyMessage(CANCEL_USER_INPUT);
		}
		
		public void postMoveRight() {
			sendEmptyMessage(MOVE_RIGHT);
		}
		
		public void postMoveDown() {
			sendEmptyMessage(MOVE_DOWN);
		}
		
		public void postRotate() {
			sendEmptyMessage(ROTATE);
		}
		
		public void postGameAdvance(int delayMillis) {
			sendEmptyMessageDelayed(GAME_ADVANCE, delayMillis);
		}
		
		public void postSurfaceCreated(SurfaceHolder holder) {
			sendMessage(Message.obtain(this, SURFACE_CREATED, new Object[]{ holder }));
		}

		public void clearSurface() {
			if (shutdown) {
				/*#*/Log.w(TAG, "Game handler is already shutdown. Skipping clearSurface...");
				return;
			}
			/*#*/Log.d(TAG, "clearSurface started.");
			Object[] holderAndSetStatus = new Object[]{ null, true };
			sendMessage(Message.obtain(this, SURFACE_CREATED, holderAndSetStatus));
			// Wait for surface clearance to be reflected.
			synchronized (holderAndSetStatus) {
				while ((boolean)holderAndSetStatus[1]) {
					try {
						holderAndSetStatus.wait();
					}
					catch (InterruptedException e) {}
				}
			}
			/*#*/Log.d(TAG, "clearSurface finished.");
		}
		
		public void postSurfaceChanged(int w, int h) {
			sendMessage(Message.obtain(this, SURFACE_CHANGED, w, h));
			if (!hasMessages(GAME_ADVANCE)) {
				postGameAdvance(0);
			}
		}

		public void postOrientationChanged(boolean landscape) {
			sendEmptyMessage(landscape ? ORIENTATION_LANDSCAPE : ORIENTATION_PORTRAIT);
		}

		public void postKeyboardHidden(boolean keyboardHidden) {
			sendEmptyMessage(keyboardHidden ? KEYBOARD_HIDDEN : KEYBOARD_SHOWN);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PAUSE:
				game.setPaused(true);
				break;
			case RESUME:
				game.setPaused(false);
				break;
			case TOUCH_EVENT:
				game.processTouchEvent((MotionEvent)msg.obj);
				break;
			case MOVE_LEFT:
				game.moveLeft();
				break;
			case CANCEL_USER_INPUT:
				game.cancelUserInput();
				break;
			case MOVE_RIGHT:
				game.moveRight();
				break;
			case MOVE_DOWN:
				game.moveDown();
				break;
			case ROTATE:
				game.rotate();
				break;
			case SURFACE_CHANGED:
				game.surfaceChanged(msg.arg1, msg.arg2);
				break;
			case SURFACE_CREATED:
				game.setSurface((Object[])msg.obj);
				break;
			case ORIENTATION_LANDSCAPE:
			case ORIENTATION_PORTRAIT:
				game.setOrientation(msg.what == ORIENTATION_LANDSCAPE);
				break;
			case KEYBOARD_HIDDEN:
			case KEYBOARD_SHOWN:
				game.setKeyboardHidden(msg.what == KEYBOARD_HIDDEN);
				break;
			case USE_RESTORED_STATE:
				game.restoreState((GameRestoreFragment)msg.obj);
				break;
			case SAVE_STATE:
				game.saveState((Object[])msg.obj);
				break;
			case GAME_ADVANCE:
				postGameAdvance(game.advance());
				break;
			default:
				/*#*/Log.w(TAG, "Unknown message.what: " + msg.what);
				break;
			}
		}
	}
}
