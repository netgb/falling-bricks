package com.ayautilities.misc.games.fallingbricks.game;

import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
/*#*/import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.ayautilities.misc.games.fallingbricks.GameRestoreFragment;
import com.ayautilities.misc.games.fallingbricks.GameView;

public class FallingBricksGame {
	private static final String TAG = FallingBricksGame.class.getName();
	
	private GameView gameView;
	private SurfaceHolder surfaceHolder;
	
	private boolean paused = true;
	
	// record stats every 1 second (roughly)
	private static long MAX_STATS_INTERVAL = 1000000000L;

    // no. of frames that can be skipped in any one animation loop
    // i.e the games state is updated but not rendered
	private static final int MAX_FRAME_SKIPS = 5;

    /* Number of frames with a delay of 0 ms before the
       animation thread yields to other running threads. */
	private static final int NO_DELAYS_PER_YIELD = 16;

	// number of FPS values stored to get an average
	private static int NUM_FPS = 10;

	// used for gathering statistics
	private long statsInterval = 0L; // in ns
	private long prevStatsTime = 0L;
	private long totalElapsedTime = 0L;

	private long frameCount = 0;
	private double fpsStore[];
	private long statsCount = 0;
	private double averageFPS = 0.0;

	private long framesSkipped = 0L;
	private long totalFramesSkipped = 0L;
	private double upsStore[];
	private double averageUPS = 0.0;

	private DecimalFormat df = new DecimalFormat("0.##"); // 2 dp
	private DecimalFormat timedf = new DecimalFormat("0.####"); // 4 dp

	private long afterTime, sleepTime = 0L;
    private long overSleepTime = 0L;
    private int noDelays = 0;
    private long excess = 0L;
    
    // FallingBricks-specific state variables.
    static final int MIN_LEVEL = 1;
    static final int MAX_LEVEL = 10;

    private static final long MAX_LEVEL_PERIOD = 100000000L; // 100 ms
    private static final long MIN_LEVEL_PERIOD = 1000000000L; // 1 sec
    private static final long MIN_LEVEL_SYSTEM_TASK_PERIOD = 75000000L; // 75 ms
    private static final long MAX_LEVEL_SYSTEM_TASK_PERIOD = 25000000L; // 25ms
    private static final long THREAD_PERIOD = 25000000L; // 25 ms
    private static final long USER_INPUT_FAST_PERIOD = 50000000L; // 50 ms
    private static final long USER_INPUT_SLOW_PERIOD = 150000000L; // 150 ms
    private static final long FLASH_PERIOD = 400000000L; // 400 milliseconds.

    public static final int GRID_ROW_COUNT = 25;
    public static final int GRID_COLUMN_COUNT = 16;

    private static final int SCORE_INCREMENT = 100;

    private int level = 1;
	private long levelPeriod, systemTaskPeriod, gameOverPeriod;
	private long gameAdvancerTime, systemTaskTime;

    private Arena arena;
    private Brick brick;

    private GameOverAnimator gameOverAnimator;
    private FilledRowClearanceAnimator filledRowClearer;
    
    private boolean gameOver = false;
    private boolean rowClearanceInProgress = false;

    private long levelStartTime;
    private static final long MAX_LEVEL_TIME = 900000000000L; // 15 mins

    // Index 0 is for running total; the remaining indices are for per level scores.
    private int[] scores;
    
    private static final long MAX_TIME_AFTER_GAME_OVER_ANIMATION = 7000000000L; // 7 seconds
    private long timeAfterGameOver = 0L;
    private boolean gameOverCleanUpInitiated = false;
    private Runnable quitGameRunnable;
    
    private static final int USER_INPUT_MOVE_LEFT = 0, USER_INPUT_MOVE_RIGHT = 3,
    		USER_INPUT_MOVE_DOWN = 2, USER_INPUT_ROTATE = 1;
    private int  userInput = -1, lastUserInput = -1;
    private long userInputTimeLag;
    private long lastUserInputTime;

	private final int arrowImageSizePlusPadding;
	private final int touchAreaSize;

	private int touchAreaX, touchAreaY, touchAreaWidth, touchAreaHeight;
	private boolean orientationLandscape, keyboardHidden;
    
	public FallingBricksGame(GameView gameView) {
		this.gameView = gameView;
		
		arrowImageSizePlusPadding = GameView.ARROW_IMAGE_SIZE + gameView.getArrowImagePadding() * 2;
		touchAreaSize = 2 * arrowImageSizePlusPadding;
		
		// initialise timing elements
	    fpsStore = new double[NUM_FPS];
	    upsStore = new double[NUM_FPS];
	    for (int i=0; i < NUM_FPS; i++) {
	    	fpsStore[i] = 0.0;
	    	upsStore[i] = 0.0;
	    }
	}

	public void setQuitRunnable(Runnable quitRunnable) {
		this.quitGameRunnable = quitRunnable;
	}

	public void setOrientation(boolean landscape) {
		/*#*/Log.d(TAG, String.format("Orientation changed to %s.",
		/*#*/		landscape ? "LANDSCAPE" : "PORTRAIT"));
		this.orientationLandscape = landscape;		
	}

	public void setKeyboardHidden(boolean keyboardHidden) {
		/*#*/Log.d(TAG, String.format("Keyboard is %s.",
		/*#*/		keyboardHidden ? "hidden" : "available"));
		this.keyboardHidden = keyboardHidden;
	}
	
	public void setSurface(Object[] holderAndSetStatus) {
		this.surfaceHolder = (SurfaceHolder)holderAndSetStatus[0];
		boolean setStatus = surfaceHolder != null;
		/*#*/Log.d(TAG, String.format("Surface Holder has been %s.",
		/*#*/		setStatus ? "set" : "lost"));
		if (holderAndSetStatus.length > 1) {
			synchronized (holderAndSetStatus) {
				holderAndSetStatus[1] = setStatus;
				holderAndSetStatus.notify();
			}
		}
	}
	
	public void saveState(Object[] stateAndStateSaveStatus) {
		/*#*/Log.i(TAG, "Saving game state...");
		
		GameRestoreFragment state = (GameRestoreFragment)stateAndStateSaveStatus[0];
		
		// Save Arena state.
		state.setA_artifactsVisible(arena.isArtifactsVisible());
		state.setA_gameOverMessage(arena.getGameOverMessage());
		state.setA_nextBrickInfoFilledCells(arena.getNextBrickInfoFilledCells());
		state.setA_nonEmptyGridRows(arena.getNonEmptyGridRows());
		
		// Save Brick state.
		state.setB_brickInfo(brick.getBrickInfo());
		state.setB_nextBrickInfo(brick.getNextBrickInfo());
		state.setB_columnPosition(brick.getColumnPosition());
		state.setB_rowPosition(brick.getRowPosition());
		
		// Save FilledRowClearanceAnimator state.
		state.setFrca_minRowToCheck(filledRowClearer.getMinRowToCheck());
		state.setFrca_rowToCheck(filledRowClearer.getRowToCheck());
		state.setFrca_rowOccupiedStatuses(filledRowClearer.getRowOccupiedStatuses());
		
		// Save remaining state variables.
		state.setLevel(level);
		state.setFg_gameAdvancerTime(gameAdvancerTime);
		state.setFg_gameOver(gameOver);
		state.setFg_gameOverCleanUpInitiated(gameOverCleanUpInitiated);
		state.setFg_gameOverPeriod(gameOverPeriod);
		state.setFg_levelPeriod(levelPeriod);
		state.setFg_levelStartTime(levelStartTime);
		state.setFg_rowClearanceInProgress(rowClearanceInProgress);
		state.setFg_systemTaskPeriod(systemTaskPeriod);
		state.setFg_systemTaskTime(systemTaskTime);
		
		state.setEdited(true);
		
		synchronized (stateAndStateSaveStatus) {
			stateAndStateSaveStatus[1] = true;
			stateAndStateSaveStatus.notify();
		}
	}
	
	public void restoreState(GameRestoreFragment state) {
		// Call state.isEdited for reads and writes by ui thread to be
		// observable here.
		if (state.isEdited()) {
			/*#*/Log.i(TAG, "Restoring game state...");			
		}
		else {
			/*#*/Log.i(TAG, "Starting new game state...");
		}

        level = state.getLevel();
		scores = state.getScores();
		
		// Restore arena state.
        arena = new Arena(GRID_ROW_COUNT, GRID_COLUMN_COUNT, scores,
            state.getHighScores(), gameView.getTextSize());
        if (state.isEdited()) {
        	arena.setArtifactsVisible(state.isA_artifactsVisible());
        	arena.setGameOverMessage(state.getA_gameOverMessage());
        	arena.setNextBrickInfoFilledCells(state.getA_nextBrickInfoFilledCells());
        	arena.getNonEmptyGridRows().addAll(state.getA_nonEmptyGridRows());
            arena.setLevel(level);
        }
        
        // Restore brick state.
        if (state.isEdited()) {
        	brick = new Brick(arena, GRID_ROW_COUNT, GRID_COLUMN_COUNT,
        			state.getB_brickInfo(), state.getB_nextBrickInfo());
        	brick.setColumnPosition(state.getB_columnPosition());
        	brick.setRowPosition(state.getB_rowPosition());
        }
        else {
        	brick = new Brick(arena, GRID_ROW_COUNT, GRID_COLUMN_COUNT);
        }
        
        filledRowClearer = new FilledRowClearanceAnimator(arena);
        gameOverAnimator = new GameOverAnimator(arena);

        // Restore remaining state.
        if (state.isEdited()) {
        	gameAdvancerTime = state.getFg_gameAdvancerTime();
        	gameOver = state.isFg_gameOver();
        	gameOverCleanUpInitiated = state.isFg_gameOverCleanUpInitiated();
        	gameOverPeriod = state.getFg_gameOverPeriod();
        	levelPeriod = state.getFg_levelPeriod();
        	levelStartTime = state.getFg_levelStartTime();
        	rowClearanceInProgress = state.isFg_rowClearanceInProgress();
        	systemTaskPeriod = state.getFg_systemTaskPeriod();
        	systemTaskTime = state.getFg_systemTaskTime();
        	
        	filledRowClearer.setMinRowToCheck(state.getFrca_minRowToCheck());
        	filledRowClearer.setRowOccupiedStatuses(state.getFrca_rowOccupiedStatuses());
        	filledRowClearer.setRowToCheck(state.getFrca_rowToCheck());
        }
        else {
        	levelChanged();
        }
	}

    private void levelChanged() {
        levelStartTime = System.nanoTime();
        levelPeriod = MIN_LEVEL_PERIOD +
                (MAX_LEVEL_PERIOD - MIN_LEVEL_PERIOD) *
                (level - MIN_LEVEL) / (MAX_LEVEL - MIN_LEVEL);
        systemTaskPeriod = MIN_LEVEL_SYSTEM_TASK_PERIOD +
                (MAX_LEVEL_SYSTEM_TASK_PERIOD - MAX_LEVEL_SYSTEM_TASK_PERIOD) *
                (level - MIN_LEVEL) / (MAX_LEVEL - MIN_LEVEL);
        arena.setLevel(level);
    }

	public void setPaused(boolean paused) {
		/*#*/Log.d(TAG, String.format("Received %s request.", paused ? "Pause" : "Resume"));
		this.paused = paused;
		lastUserInput = -1;
	}

	public void surfaceChanged(int w, int h) {
		/*#*/Log.d(TAG, String.format("Surface dimensions changed to %d by %d.", w, h));
		if (keyboardHidden) {
			touchAreaWidth = orientationLandscape ? touchAreaSize : w;
			touchAreaHeight = orientationLandscape ? h : touchAreaSize;
			touchAreaX = w - touchAreaWidth;
			touchAreaY = h - touchAreaHeight;
			/*#*/Log.d(TAG, String.format("Touch Area x,y,w,h: %d %d %d %d", touchAreaX,
			/*#*/		touchAreaY, touchAreaWidth, touchAreaHeight));
			int daw = orientationLandscape ? touchAreaSize : 0;
			int day = orientationLandscape ? 0 : touchAreaSize;
			/*#*/Log.d(TAG, String.format("daw,day = %d,%d", daw, day));
			arena.sizeChanged(w - daw, h - day);
		}
		else {
			arena.sizeChanged(w, h);
		}
	}

	private void gameUpdate() {
		if (paused) return;
        if (gameOver) {
            timeAfterGameOver += THREAD_PERIOD;
            if (timeAfterGameOver >= MAX_TIME_AFTER_GAME_OVER_ANIMATION &&
                    !gameOverCleanUpInitiated) {            	
            	gameView.post(quitGameRunnable);
                gameOverCleanUpInitiated = true;
            }
            systemTaskTime += THREAD_PERIOD;
            if (systemTaskTime >= gameOverPeriod) {
            	systemTaskTime = 0L;
	            boolean gameOverAnimationComplete = gameOverAnimator.execute();
	            if (gameOverAnimationComplete) {
	                arena.setGameOverMessage("GAME OVER!\nYour score is " +
	                        scores[0] + ".");
	            }
	            else if (gameOverAnimator.isGridFilled()) {
	            	gameOverPeriod = FLASH_PERIOD;
	            }
            }
        }
        else if (rowClearanceInProgress) {
        	systemTaskTime += THREAD_PERIOD;
        	if (systemTaskTime >= systemTaskPeriod) {
	            systemTaskTime = 0L;
	            boolean rowClearanceCompleted = filledRowClearer.execute();
	            if (rowClearanceCompleted) {
	                rowClearanceInProgress = false;
	                int scoreInc = filledRowClearer.getDeletedRowCount() *
	                        SCORE_INCREMENT;
	                scores[0] += scoreInc;
	                scores[level] += scoreInc;
	                changeBrick();
	            }
        	}
        }
        else {
        	gameAdvancerTime += THREAD_PERIOD;
        	if (gameAdvancerTime >= levelPeriod) {
        		gameAdvancerTime = 0L;
        		moveBrickDown();
        	}
        	handleUserInput();
        }
	}

    private void moveBrickDown() {
        if (brick.moveDown()) return;
        if (gameOver = gameOverAnimator.isGameOver()) {
        	gameOverPeriod = systemTaskPeriod;
        	systemTaskTime = 0L;
            return;
        }
        rowClearanceInProgress = filledRowClearer.filledRowsExist(brick);
        if (!rowClearanceInProgress) {
            changeBrick();
        }
        else {
            // Let's make row clearance get to work faster
            systemTaskTime = systemTaskPeriod;
        }
    }

    private void changeBrick() {
        brick.changeBrickInfo();
        // Let's make brick come down faster.
        gameAdvancerTime = levelPeriod;
        if (System.nanoTime() - levelStartTime < MAX_LEVEL_TIME) {
            return;
        }
        if (level >= MAX_LEVEL) level = MIN_LEVEL;
        else level++;
        levelChanged();
    }

    public boolean isGameOver() {
        return gameOver;
    }

	public void processTouchEvent(MotionEvent event) {
		int direction = -1;
		if (orientationLandscape) {
			int x, y;
			int cy = touchAreaY + (touchAreaHeight - arrowImageSizePlusPadding) / 2;
			x = touchAreaX;
			y = cy;
			if (event.getX() > x && event.getX() < (x + arrowImageSizePlusPadding) &&
					event.getY() > y && event.getY() < (y + arrowImageSizePlusPadding)) {
				/*#*/Log.v(TAG, "Detected touch of left arrow.");
				direction = USER_INPUT_MOVE_LEFT;
			}
			
			x = touchAreaX + touchAreaWidth - arrowImageSizePlusPadding;
			if (event.getX() > x && event.getX() < (x + arrowImageSizePlusPadding) &&
					event.getY() > y && event.getY() < (y + arrowImageSizePlusPadding)) {
				/*#*/Log.v(TAG, "Detected touch of right arrow.");
				direction = USER_INPUT_MOVE_RIGHT;
			}
			
			y = cy - arrowImageSizePlusPadding;
			x = touchAreaX + (touchAreaWidth - arrowImageSizePlusPadding) / 2;
			if (event.getX() > x && event.getX() < (x + arrowImageSizePlusPadding) &&
					event.getY() > y && event.getY() < (y + arrowImageSizePlusPadding)) {
				/*#*/Log.v(TAG, "Detected touch of up arrow.");
				direction = USER_INPUT_ROTATE;
			}
			
			y = cy + arrowImageSizePlusPadding;
			if (event.getX() > x && event.getX() < (x + arrowImageSizePlusPadding) &&
					event.getY() > y && event.getY() < (y + arrowImageSizePlusPadding)) {
				/*#*/Log.v(TAG, "Detected touch of down arrow.");
				direction = USER_INPUT_MOVE_DOWN;
			}
		}
		else {
			int x, y;
			int cx = touchAreaX + (touchAreaWidth - arrowImageSizePlusPadding) / 2;
			
			x = cx;
			y = touchAreaY;
			if (event.getX() > x && event.getX() < (x + arrowImageSizePlusPadding) &&
					event.getY() > y && event.getY() < (y + arrowImageSizePlusPadding)) {
				/*#*/Log.v(TAG, "Detected touch of up arrow.");
				direction = USER_INPUT_ROTATE;
			}
			
			y = touchAreaY + touchAreaHeight - arrowImageSizePlusPadding;
			if (event.getX() > x && event.getX() < (x + arrowImageSizePlusPadding) &&
					event.getY() > y && event.getY() < (y + arrowImageSizePlusPadding)) {
				/*#*/Log.v(TAG, "Detected touch of down arrow.");
				direction = USER_INPUT_MOVE_DOWN;
			}
			
			x = cx - arrowImageSizePlusPadding;
			y = touchAreaY + (touchAreaHeight - arrowImageSizePlusPadding) / 2;
			if (event.getX() > x && event.getX() < (x + arrowImageSizePlusPadding) &&
					event.getY() > y && event.getY() < (y + arrowImageSizePlusPadding)) {
				/*#*/Log.v(TAG, "Detected touch of left arrow.");
				direction = USER_INPUT_MOVE_LEFT;
			}
			
			x = cx + arrowImageSizePlusPadding;
			if (event.getX() > x && event.getX() < (x + arrowImageSizePlusPadding) &&
					event.getY() > y && event.getY() < (y + arrowImageSizePlusPadding)) {
				/*#*/Log.v(TAG, "Detected touch of right arrow.");
				direction = USER_INPUT_MOVE_RIGHT;
			}
		}
		
		switch (direction) {
    	case USER_INPUT_MOVE_LEFT:
    		moveLeft();
    		break;
    	case USER_INPUT_ROTATE:
    		rotate();
    		break;
    	case USER_INPUT_MOVE_DOWN:
    		moveDown();
    		break;
    	case USER_INPUT_MOVE_RIGHT:
    		moveRight();
    		break;
    	default:
    		break;
    	}
		
		if (userInput != -1 &&
				(direction == -1 || event.getAction() == MotionEvent.ACTION_UP)) {
			cancelUserInput();
		}
	}

	public void rotate() {
		/*#*/Log.v(TAG, "Received rotate request.");
		if (userInput != USER_INPUT_ROTATE) {
			userInput = USER_INPUT_ROTATE;
			handleUserInput();
		}
	}

	public void moveDown() {
		/*#*/Log.v(TAG, "Received move down request.");
		if (userInput != USER_INPUT_MOVE_DOWN) {
			userInput = USER_INPUT_MOVE_DOWN;
			handleUserInput();
		}
	}

	public void moveRight() {
		/*#*/Log.v(TAG, "Received move right request.");
		if (userInput != USER_INPUT_MOVE_RIGHT) {
			userInput = USER_INPUT_MOVE_RIGHT;
			handleUserInput();
		}
	}

	public void moveLeft() {
		/*#*/Log.v(TAG, "Received move left request.");
		if (userInput != USER_INPUT_MOVE_LEFT) {
			userInput = USER_INPUT_MOVE_LEFT;
			handleUserInput();
		}
	}

	public void cancelUserInput() {
		/*#*/Log.d(TAG, "Received cancel user input request.");
		userInput = -1;
	}
	
	private void handleUserInput() {
		if (!paused && !rowClearanceInProgress && !gameOver && userInput != -1) {
			long now = System.nanoTime();
			long timeDiff = now - lastUserInputTime;
			if (userInput == lastUserInput && timeDiff < userInputTimeLag) {
				return;
			}
			
			switch (userInput) {
			case USER_INPUT_MOVE_DOWN:
				/*#*/Log.d(TAG, "Processing move down request.");
				moveBrickDown();
				break;
			case USER_INPUT_MOVE_LEFT:
				/*#*/Log.d(TAG, "Processing move left request.");
				brick.moveLeft();
				break;
			case USER_INPUT_MOVE_RIGHT:
				/*#*/Log.d(TAG, "Processing move right request.");
				brick.moveRight();
				break;
			case USER_INPUT_ROTATE:
				/*#*/Log.d(TAG, "Processing rotate request.");
				brick.rotateClockwise();
				break;
			default:
				/*#*/Log.w(TAG, "Unknown user input: " + lastUserInput);
				break;
			}
			
			switch (userInput) {
			case USER_INPUT_MOVE_DOWN:
				userInputTimeLag = USER_INPUT_FAST_PERIOD;
				break;
			case USER_INPUT_ROTATE:
				userInputTimeLag = USER_INPUT_SLOW_PERIOD;
				break;
			default:
				userInputTimeLag = (userInput != lastUserInput && timeDiff > USER_INPUT_SLOW_PERIOD) ?
						USER_INPUT_SLOW_PERIOD : USER_INPUT_FAST_PERIOD;
				break;
			}
			
			lastUserInput = userInput;
			lastUserInputTime = now;
		}
	}
	
	private void gameRender() {
		if (surfaceHolder == null) {
			/*#*/Log.v(TAG, "Surface Holder hasn't been set.");
			return;
		}
		if (paused) return;
		Canvas c = null;
        try {
            c = surfaceHolder.lockCanvas();
            if (c != null) {
            	c.drawColor(Color.BLACK);
            	if (keyboardHidden)
            		paintTouchArea(c);
            	arena.paint(c);
            	brick.paint(c);
            }
            else {
            	/*#*/Log.w(TAG, "Canvas is null.");
            }
        } finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (c != null) {
                surfaceHolder.unlockCanvasAndPost(c);
            }
        }
	}
	
	private void paintTouchArea(Canvas c) {
		Paint p = new Paint();
		
		if (orientationLandscape) {
			int x = touchAreaX + gameView.getArrowImagePadding();
			int cy = touchAreaY + (touchAreaHeight - arrowImageSizePlusPadding) / 2 +
					gameView.getArrowImagePadding();
			int y = cy;
			c.drawBitmap(gameView.getLeftArrow(), x, y, p);
			
			x = touchAreaX + touchAreaWidth - GameView.ARROW_IMAGE_SIZE - gameView.getArrowImagePadding();
			c.drawBitmap(gameView.getRightArrow(), x, y, p);
			
			y = cy - arrowImageSizePlusPadding;
			x = touchAreaX + (touchAreaWidth - arrowImageSizePlusPadding) / 2 +
					gameView.getArrowImagePadding();
			c.drawBitmap(gameView.getUpArrow(), x, y, p);
			
			y = cy + arrowImageSizePlusPadding;
			c.drawBitmap(gameView.getDownArrow(), x, y, p);
		}
		else {
			int cx = touchAreaX + (touchAreaWidth - arrowImageSizePlusPadding) / 2 +
					gameView.getArrowImagePadding();
			int x = cx;
			int y = touchAreaY + gameView.getArrowImagePadding();		
			c.drawBitmap(gameView.getUpArrow(), x, y, p);
			
			y = touchAreaY + touchAreaHeight - GameView.ARROW_IMAGE_SIZE - gameView.getArrowImagePadding();
			c.drawBitmap(gameView.getDownArrow(), x, y, p);
			
			x = cx - arrowImageSizePlusPadding;
			y = touchAreaY + (touchAreaHeight - arrowImageSizePlusPadding) / 2 +
					gameView.getArrowImagePadding();
			c.drawBitmap(gameView.getLeftArrow(), x, y, p);
			
			x = cx + arrowImageSizePlusPadding;
			c.drawBitmap(gameView.getRightArrow(), x, y, p);
		}
	}

	public int advance() {
		long timeNow = System.nanoTime();
		if (prevStatsTime == 0L) { // initialize prevStatsTime
			prevStatsTime = timeNow;
		}
	    if (sleepTime > 0L) {
	    	overSleepTime = (timeNow - afterTime) - sleepTime;
	    }
	    long beforeTime = timeNow;
	 
	    /* If frame animation is taking too long, update the game state
        without rendering it, to get the updates/sec nearer to
        the required FPS. */
	    int skips = 0;
	    while((excess > THREAD_PERIOD) && (skips < MAX_FRAME_SKIPS)) {
	    	excess -= THREAD_PERIOD;
	    	gameUpdate();      // update state but don't render
	    	skips++;
	    }
	    framesSkipped += skips;
	      
		gameUpdate();
		gameRender();
		
		afterTime = System.nanoTime();
        long timeDiff = afterTime - beforeTime;
        sleepTime = (THREAD_PERIOD - timeDiff) - overSleepTime;
        if (sleepTime > 0) {   // some time left in this cycle
        }
        else {    // sleepTime <= 0; frame took longer than the period
        	excess -= sleepTime;  // store excess time value
    		overSleepTime = 0L;
    		if (++noDelays >= NO_DELAYS_PER_YIELD) {
    			Thread.yield();   // give another thread a chance to run
    			noDelays = 0;
    		}
        }
        
        //storeStats();
        
        return Math.max(0, (int)(sleepTime / 1000000L));  // nano -> ms
	}

	/*
	 * The statistics: - the summed periods for all the iterations in this
	 * interval (period is the amount of time a single frame iteration should
	 * take), the actual elapsed time in this interval, the error between these
	 * two numbers;
	 * 
	 * - the total frame count, which is the total number of calls to run();
	 * 
	 * - the frames skipped in this interval, the total number of frames
	 * skipped. A frame skip is a game update without a corresponding render;
	 * 
	 * - the FPS (frames/sec) and UPS (updates/sec) for this interval, the
	 * average FPS & UPS over the last NUM_FPSs intervals.
	 * 
	 * The data is collected every MAX_STATS_INTERVAL (1 sec).
	 */
	private void storeStats()
	{
		frameCount++;
		statsInterval += THREAD_PERIOD;

		if (statsInterval >= MAX_STATS_INTERVAL) { // record stats every
													// MAX_STATS_INTERVAL
			long timeNow = System.nanoTime();
			
			long realElapsedTime = timeNow - prevStatsTime; // time since last
															// stats collection
			totalElapsedTime += realElapsedTime;

			double timingError = ((double) (realElapsedTime - statsInterval) / statsInterval) * 100.0;

			totalFramesSkipped += framesSkipped;

			double actualFPS = 0; // calculate the latest FPS and UPS
			double actualUPS = 0;
			if (totalElapsedTime > 0) {
				actualFPS = (((double) frameCount / totalElapsedTime) * 1000000000L);
				actualUPS = (((double) (frameCount + totalFramesSkipped) / totalElapsedTime) * 1000000000L);
			}

			// store the latest FPS and UPS
			fpsStore[(int) statsCount % NUM_FPS] = actualFPS;
			upsStore[(int) statsCount % NUM_FPS] = actualUPS;
			statsCount = statsCount + 1;

			double totalFPS = 0.0; // total the stored FPSs and UPSs
			double totalUPS = 0.0;
			for (int i = 0; i < NUM_FPS; i++) {
				totalFPS += fpsStore[i];
				totalUPS += upsStore[i];
			}

			if (statsCount < NUM_FPS) { // obtain the average FPS and UPS
				averageFPS = totalFPS / statsCount;
				averageUPS = totalUPS / statsCount;
			} else {
				averageFPS = totalFPS / NUM_FPS;
				averageUPS = totalUPS / NUM_FPS;
			}
			
			/*#*/Log.v(TAG,
			/*#*/         timedf.format( (double) statsInterval/1000000000L) + " " +
			/*#*/         timedf.format((double) realElapsedTime/1000000000L)+"s "+
			/*#*/         df.format(timingError) + "% " +
			/*#*/         frameCount + "c " +
			/*#*/         framesSkipped + "/" + totalFramesSkipped + " skip; " +
			/*#*/         df.format(actualFPS) + " " + df.format(averageFPS)+" afps; " +
			/*#*/         df.format(actualUPS) + " " + df.format(averageUPS)+" aups" );
			
			framesSkipped = 0;
			prevStatsTime = timeNow;
			statsInterval = 0L; // reset
		}
	}
}
