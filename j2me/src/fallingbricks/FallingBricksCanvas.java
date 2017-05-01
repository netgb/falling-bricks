package fallingbricks;

import fallingbricks.infrastructure.AbstractGameTask;
import fallingbricks.infrastructure.GameTaskManager;
import fallingbricks.infrastructure.MyGameCanvas;
import java.util.Date;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author Aaron
 */
public class FallingBricksCanvas extends MyGameCanvas {
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 10;

    private static final int MAX_LEVEL_PERIOD = 100;
    private static final int MIN_LEVEL_PERIOD = 1000;
    private static final int MIN_LEVEL_SYSTEM_TASK_PERIOD = 75;
    private static final int MAX_LEVEL_SYSTEM_TASK_PERIOD = 25;
    private static final int THREAD_PERIOD = 25;
    private static final int USER_INPUT_PERIOD = 150;

    public static final int GRID_ROW_COUNT = 25;
    public static final int GRID_COLUMN_COUNT = 16;

    private static final int CANVAS_COLOR = 0xffffff; // white

    private static final int SCORE_INCREMENT = 100;

    private FallingBricksMidlet midlet;
    private int level;
    private int gameAdvancerPeriod, systemTaskPeriod;

    private final Arena arena;
    private final Brick brick;

    private final GameOverAnimator gameOverAnimator;
    private final FilledRowClearanceAnimator filledRowClearer;
    private final KeyDownHandler keyDownHandler;
    private final RotateRequestHandler rotateRequestHandler;
    private final UserInputHandler userInputHandler;
    private final GameAdvancer gameAdvancer;

    private final GameTaskManager taskManager;

    private boolean gameOver = false;
    private boolean rowClearanceInProgress = false;

    private long levelStartTime;
    private static final long MAX_LEVEL_TIME = 900000; // 15 mins

    // Index 0 is for running total; the remaining indices are for per level scores.
    private int[] scores = new int[FallingBricksMidlet.LEVEL_COUNT+1];

    private static final int MAX_TIME_AFTER_GAME_OVER_ANIMATION = 7000; // seconds
    private int timeAfterGameOver = 0;
    private boolean gameOverCleanUpInitiated = false;
    private final Command pauseCommand;

    public FallingBricksCanvas(FallingBricksMidlet midlet,
            int level, boolean storeStats) {
        super(THREAD_PERIOD, storeStats);
        this.midlet = midlet;
        this.level = level;

        arena = new Arena(this, GRID_ROW_COUNT, GRID_COLUMN_COUNT, scores,
            midlet.getHighScores());
        brick = new Brick(arena, GRID_ROW_COUNT, GRID_COLUMN_COUNT);

        gameAdvancer = new GameAdvancer();
        userInputHandler = new UserInputHandler();
        keyDownHandler = new KeyDownHandler();
        rotateRequestHandler = new RotateRequestHandler();
        filledRowClearer = new FilledRowClearanceAnimator(arena);
        gameOverAnimator = new GameOverAnimator(arena);

        taskManager = new GameTaskManager(THREAD_PERIOD);

        levelChanged();


        pauseCommand = new Command("Pause", Command.SCREEN, 0);
        addCommand(pauseCommand);
        setCommandListener(midlet);
    }

    private void levelChanged() {
        levelStartTime = new Date().getTime();

        gameAdvancerPeriod = MIN_LEVEL_PERIOD +
                (MAX_LEVEL_PERIOD - MIN_LEVEL_PERIOD) *
                (level - MIN_LEVEL) / (MAX_LEVEL - MIN_LEVEL);
        systemTaskPeriod = MIN_LEVEL_SYSTEM_TASK_PERIOD +
                (MAX_LEVEL_SYSTEM_TASK_PERIOD - MAX_LEVEL_SYSTEM_TASK_PERIOD) *
                (level - MIN_LEVEL) / (MAX_LEVEL - MIN_LEVEL);
        gameAdvancer.timeBetweenRuns = gameAdvancerPeriod;
        filledRowClearer.timeBetweenRuns = gameOverAnimator.timeBetweenRuns =
                systemTaskPeriod;
        gameOverAnimator.accruedTime = filledRowClearer.accruedTime = 0;

        arena.setLevel(level);
    }

    protected void sizeChanged(int w, int h) {
        arena.sizeChanged(w, h);
    }

    protected void updateGame() {
        if (gameOver) {
            timeAfterGameOver += THREAD_PERIOD;
            if (timeAfterGameOver >= MAX_TIME_AFTER_GAME_OVER_ANIMATION &&
                    !gameOverCleanUpInitiated) {
                Display.getDisplay(midlet).callSerially(new Runnable() {

                    public void run() {
                        midlet.quitGame(true);
                    }
                });
                gameOverCleanUpInitiated = true;
            }
            boolean gameOverAnimationComplete =
                    taskManager.runTask(gameOverAnimator);
            if (gameOverAnimationComplete) {
                arena.setGameOverMessage("GAME OVER!\nYour score is " +
                        scores[0] + ".");
            }
        }
        else if (!isPaused()) {
            if (rowClearanceInProgress) {
                boolean rowClearanceCompleted =
                        taskManager.runTask(filledRowClearer);
                if (rowClearanceCompleted) {
                    rowClearanceInProgress = false;
                    int scoreInc = filledRowClearer.getDeletedRowCount() *
                            SCORE_INCREMENT;
                    scores[0] += scoreInc;
                    scores[level] += scoreInc;
                    changeBrick();
                }
            }
            else {
                int keyStates = getKeyStates();
                keyDownHandler.ks = keyStates;
                rotateRequestHandler.ks = keyStates;
                userInputHandler.ks = keyStates;
                if (!taskManager.runTask(keyDownHandler)) {
                    taskManager.runTask(rotateRequestHandler);
                    taskManager.runTask(userInputHandler);
                    taskManager.runTask(gameAdvancer);
                }
            }
        }
    }

    protected void renderGame() {
        Graphics g = getGraphics();
        int w = getWidth();
        int h = getHeight();
        g.setColor(CANVAS_COLOR);
        g.fillRect(0, 0, w, h);
        arena.paint(g);
        brick.paint(g);
    }

    private void moveBrickDown() {
        if (brick.moveDown()) return;
        if (gameOver = gameOverAnimator.isGameOver()) {
            removeCommand(pauseCommand);
            return;
        }
        rowClearanceInProgress = filledRowClearer.filledRowsExist(brick);
        if (!rowClearanceInProgress) {
            changeBrick();
        }
        else {
            // Let's make row clearance get to work faster
            filledRowClearer.accruedTime += filledRowClearer.timeBetweenRuns;
        }
    }

    private void changeBrick() {
        brick.changeBrickInfo();
        // Let's make brick come down faster.
        gameAdvancer.accruedTime += gameAdvancer.timeBetweenRuns;
        if (new Date().getTime() - levelStartTime < MAX_LEVEL_TIME) {
            return;
        }
        if (level >= MAX_LEVEL) level = MIN_LEVEL;
        else level++;
        levelChanged();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getScore() {
        return scores[0];
    }

    public int[] getScores() {
        return scores;
    }

    public int getLevel() {
        return level;
    }

    private class KeyDownHandler extends AbstractGameTask {
        public int ks;
        {
            this.timeBetweenRuns = THREAD_PERIOD;
        }
        public boolean execute() {
            if ((ks & GameCanvas.DOWN_PRESSED) != 0) {
                moveBrickDown();
                return true;
            }
            return false;
        }
    }

    private class RotateRequestHandler extends AbstractGameTask {
        public int ks;
        private boolean pressed = false;
        {
            this.timeBetweenRuns = THREAD_PERIOD;
        }
        public boolean execute() {
            if ((ks & GameCanvas.UP_PRESSED) != 0) {
                if (!pressed) {
                    brick.rotateClockwise();
                    pressed = true;
                }
            }
            else {
                pressed = false;
            }
            return true;
        }
    }

    private class UserInputHandler extends AbstractGameTask {
        public int ks, keyPressCount;
        {
            this.timeBetweenRuns = USER_INPUT_PERIOD;
            this.accruedTime += this.timeBetweenRuns;
        }
        public boolean execute() {
            boolean increaseSpeed = false;
            if ((ks & GameCanvas.LEFT_PRESSED) != 0) {
                increaseSpeed = brick.moveLeft();
            }
            else if ((ks & GameCanvas.RIGHT_PRESSED) != 0) {
                increaseSpeed = brick.moveRight();
            }
            if (increaseSpeed) {
                keyPressCount++;
            }
            else {
                keyPressCount = 0;
            }
            if (keyPressCount != 1) {
                this.accruedTime += this.timeBetweenRuns;
            }
            return true;
        }
    }

    private class GameAdvancer extends AbstractGameTask  {
        {
            this.timeBetweenRuns = gameAdvancerPeriod;
        }
        public boolean execute() {
            moveBrickDown();
            return true;
        }
    }
}
