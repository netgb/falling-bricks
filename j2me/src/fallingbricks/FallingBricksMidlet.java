package fallingbricks;

import java.io.IOException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.*;

/**
 * @author Aaron
 */
public class FallingBricksMidlet extends MIDlet
        implements CommandListener, Runnable {
    private static final int STATE_BACKGROUND_WORK = -1;
    private static final int STATE_MAIN_MENU = 0;
    private static final int STATE_PAUSE_MENU = 1;
    private static final int STATE_GAME = 2;
    private static final int STATE_INFO_MENU = 3;
    public static final int LEVEL_COUNT = 10;

    private FallingBricksCanvas gameCanvas;
    private MainMenuScreen mainMenu;
    private Form infoScreen;
    private Command infoScreenCommand;
    private LevelSelectionScreen levelSelectionScreen;
    private PauseScreen pauseScreen;
    private Form backgroundWorkScreen;
    private Gauge backgroundWorkGauge;

    private HighScores highscores;

    private boolean started = false;
    private int state;
    private boolean waitingForLevels;

    public void startApp() {
        if (!started) {
            backgroundWorkScreen = new Form(null);
            backgroundWorkGauge = new Gauge("Loading...", false, 100, 0);
            backgroundWorkGauge.setLayout(Item.LAYOUT_2 | Item.LAYOUT_CENTER |
                    Item.LAYOUT_VCENTER | Item.LAYOUT_EXPAND);
            backgroundWorkScreen.append(backgroundWorkGauge);
            Thread initializer = new Thread(this);
            initializer.start();

            state = STATE_BACKGROUND_WORK;

            started = true;
        }

        screenUpdateRequired();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        exit(unconditional);
    }

    public void screenUpdateRequired() {
        Display display = Display.getDisplay(this);
        switch (state) {
            case STATE_BACKGROUND_WORK:
                display.setCurrent(backgroundWorkScreen);
                break;
            case STATE_MAIN_MENU:
                display.setCurrent(mainMenu.getScreen());
                break;
            case STATE_INFO_MENU:
                display.setCurrent(infoScreen);
                break;
            case STATE_PAUSE_MENU:
                if (pauseScreen == null) createPauseScreen();
                display.setCurrent(pauseScreen.getScreen());
                break;
            case STATE_GAME:
                if (waitingForLevels) {
                    if (levelSelectionScreen == null) createLevelsScreen();
                    display.setCurrent(levelSelectionScreen.getScreen());
                }
                else {
                    display.setCurrent(gameCanvas);
                }
                break;
        }
    }

    public void run() {
        Display.getDisplay(this).callSerially(new Runnable() {

            public void run() {
                mainMenu = new MainMenuScreen(FallingBricksMidlet.this);

                infoScreen = new Form(null);
                infoScreen.append("");
                infoScreen.setCommandListener(FallingBricksMidlet.this);
                infoScreenCommand = new Command("Back", Command.BACK, 0);
                infoScreen.addCommand(infoScreenCommand);
            }
        });

        highscores = new HighScores(this, "HighScores", LEVEL_COUNT+1);
        try {
            highscores.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Display.getDisplay(this).callSerially(new Runnable() {

            public void run() {
                state = STATE_MAIN_MENU;
                screenUpdateRequired();
            }
        });
    }

    public void reportProgress(final int progressValue) {
        Display.getDisplay(this).callSerially(new Runnable() {

            public void run() {
                backgroundWorkGauge.setValue(progressValue);
            }
        });
    }

    private void createHelpInfo() {
        infoScreen.setTitle("Help");
        ((StringItem)infoScreen.get(0)).setText(
                "Falling Bricks is a variant of the famous "
                + "Tetris Game on Nintendo Consoles. The aim of this game is "
                + "to prevent the screen from filling up with falling bricks by arranging the "
                + "bricks so that complete rows of brick pieces are formed (the "
                + "system automatically removes those full rows and increases your "
                + "score accordingly for a good job done).\n"
                + "Use the left and right keys to shift a falling brick left or "
                + "right. Use the up key to rotate bricks clockwise and the down "
                + "key to make a brick fall faster.");
    }

    private void createAboutInfo() {
        infoScreen.setTitle("About Me");
        ((StringItem)infoScreen.get(0)).setText(
                "Falling Bricks was developed by "
                + "Aaron Baffour-Awuah. "
                + "To contact me, email me at this address: aaronbaffourawuah@gmail.com .");
    }

    private void createHighScoresInfo() {
        StringBuffer highScoresText = new StringBuffer();
        int[] highScores = highscores.getHighScores();
        highScoresText.append("Total: ").append(highScores[0]).append('\n');
        for (int i = 1; i < highScores.length; i++) {
            highScoresText.append("Level ").append(i).append(": ");
            highScoresText.append(highScores[i]).append('\n');
        }
        infoScreen.setTitle("High Scores");
        ((StringItem)infoScreen.get(0)).setText(highScoresText.toString());
    }

    private void createLevelsScreen() {
        levelSelectionScreen = new LevelSelectionScreen(this);
    }

    private void createPauseScreen() {
        pauseScreen = new PauseScreen(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (d == gameCanvas) {
            state = STATE_PAUSE_MENU;
        }
        else if (d == infoScreen) {
            state = STATE_MAIN_MENU;
        }
        screenUpdateRequired();
    }

    public void showHelpInfo() {
        createHelpInfo();
        state = STATE_INFO_MENU;
        screenUpdateRequired();
    }

    public void showAboutInfo() {
        createAboutInfo();
        state = STATE_INFO_MENU;
        screenUpdateRequired();
    }

    public void showHighScores() {
        createHighScoresInfo();
        state = STATE_INFO_MENU;
        screenUpdateRequired();
    }

    public void exit() {
        exit(false);
    }

    public void exit(boolean unconditional) {
        notifyDestroyed();
    }

    public void playGame() {
        state = STATE_GAME;
        waitingForLevels = true;
        screenUpdateRequired();
    }

    public void levelSelected(int level) {
        waitingForLevels = false;
        if (level == -1) {
            state = STATE_MAIN_MENU;
        }
        else {
            createGameScreen(level);
        }
        screenUpdateRequired();
    }

    public void createGameScreen(int levelPeriod) {
        gameCanvas = new FallingBricksCanvas(this, levelPeriod, false);
    }

    public void resumeGame() {
        state = STATE_GAME;
        screenUpdateRequired();
    }

    public void quitGame(boolean gameOver) {
        if (!gameOver) {
            gameCanvas.stopGame();
        }
        saveHighScoresAndReturnToMainMenu();
        screenUpdateRequired();
    }

    private void saveHighScoresAndReturnToMainMenu() {
        if (highscores.setHighScores(gameCanvas.getScores())) {
            backgroundWorkGauge.setLabel("Saving New High Scores...");
            backgroundWorkGauge.setValue(0);
            new Thread(new Runnable() {

                public void run() {
                    try {
                        highscores.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    Display.getDisplay(FallingBricksMidlet.this).callSerially(
                        new Runnable() {

                            public void run() {
                                gameCanvas = null;
                                state = STATE_MAIN_MENU;
                                screenUpdateRequired();
                            }
                        }
                    );
                }
            }).start();
            state = STATE_BACKGROUND_WORK;
        }
        else {
            gameCanvas = null;
            state = STATE_MAIN_MENU;
        }
        screenUpdateRequired();
    }

    public int[] getHighScores() {
        return highscores.getHighScores();
    }
}
