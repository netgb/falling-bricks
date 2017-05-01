package fallingbricks;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 *
 * @author Aaron
 */
public class PauseScreen implements CommandListener {
    private static final int MENU_ITEM_RESUME_GAME = 0;
    private static final int MENU_ITEM_QUIT_GAME = 1;
    private FallingBricksMidlet midlet;
    private List screen;
    private Command resumeCommand;
    private boolean awaitingQuitConfirm;
    private Alert quitConfirmAlert;
    private Command yesCommand, noCommand;

    public PauseScreen(FallingBricksMidlet midlet) {
        this.midlet = midlet;
        String[] menuItems = new String[]{ "Resume Game", "Quit Game"};
        screen = new List("Pause Menu", List.IMPLICIT, menuItems, null);
        screen.setCommandListener(this);
        resumeCommand = new Command("Resume", Command.BACK, 0);
        screen.addCommand(resumeCommand);
        quitConfirmAlert = new Alert("Quit Game",
                "Do you really want to quit the game?", null, AlertType.CONFIRMATION);
        quitConfirmAlert.setTimeout(Alert.FOREVER);
        yesCommand = new Command("Yes", Command.OK, 0);
        noCommand = new Command("No", Command.CANCEL, 1);
        quitConfirmAlert.addCommand(yesCommand);
        quitConfirmAlert.addCommand(noCommand);
        quitConfirmAlert.setCommandListener(this);
    }

    public Displayable getScreen() {
        if (awaitingQuitConfirm) {
            return quitConfirmAlert;
        }
        return screen;
    }

    public void commandAction(Command c, Displayable d) {
        if (d == screen) {
            if (c == List.SELECT_COMMAND) {
                int menuItemIndex = screen.getSelectedIndex();
                switch (menuItemIndex) {
                    case MENU_ITEM_RESUME_GAME:
                        midlet.resumeGame();
                        break;
                    case MENU_ITEM_QUIT_GAME:
                        awaitingQuitConfirm = true;
                        midlet.screenUpdateRequired();
                        break;
                }
            }
            else if (c == resumeCommand) {
                midlet.resumeGame();
            }
        }
        else if (d == quitConfirmAlert) {
            awaitingQuitConfirm = false;
            if (c == yesCommand) {
                midlet.quitGame(false);
            }
            else {
                midlet.screenUpdateRequired();
            }
        }
    }
}
