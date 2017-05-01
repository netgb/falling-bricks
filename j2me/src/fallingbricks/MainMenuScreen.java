package fallingbricks;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 *
 * @author Aaron
 */
public class MainMenuScreen implements CommandListener {
    private static final int MENU_ITEM_NEW_GAME = 0;
    private static final int MENU_ITEM_HELP = 1;
    private static final int MENU_ITEM_HIGH_SCORES = 2;
    private static final int MENU_ITEM_ABOUT = 3;
    private static final int MENU_ITEM_EXIT = 4;
    private FallingBricksMidlet midlet;
    private List screen;
    private Command exitCommand;

    public MainMenuScreen(FallingBricksMidlet midlet) {
        this.midlet = midlet;
        String[] menuItems = new String[]{ "New Game", "Help", "High Scores",
            "About Me", "Exit"};
        screen = new List("Main Menu", List.IMPLICIT, menuItems, null);
        screen.setCommandListener(this);
        exitCommand = new Command("Exit", Command.EXIT, 0);
        screen.addCommand(exitCommand);
    }

    public Displayable getScreen() {
        return screen;
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            int menuItemIndex = screen.getSelectedIndex();
            switch (menuItemIndex) {
                case MENU_ITEM_NEW_GAME:
                    midlet.playGame();
                    break;
                case MENU_ITEM_HELP:
                    midlet.showHelpInfo();
                    break;
                case MENU_ITEM_HIGH_SCORES:
                    midlet.showHighScores();
                    break;
                case MENU_ITEM_ABOUT:
                    midlet.showAboutInfo();
                    break;
                case MENU_ITEM_EXIT:
                    midlet.exit();
                    break;
            }
        }
        else if (c == exitCommand) {
            midlet.exit();
        }
    }
}
