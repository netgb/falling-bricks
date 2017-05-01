package fallingbricks;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 *
 * @author Aaron
 */
public class LevelSelectionScreen implements CommandListener {
    private FallingBricksMidlet midlet;
    private List screen;
    private Command backCommand;

    public LevelSelectionScreen(FallingBricksMidlet midlet) {
        this.midlet = midlet;
        String[] menuItems = new String[FallingBricksMidlet.LEVEL_COUNT];
        for (int i = 1; i <= menuItems.length; i++) {
            menuItems[i-1] = "Level " + i;
        }
        screen = new List("Levels", List.IMPLICIT, menuItems, null);
        screen.setCommandListener(this);
        backCommand = new Command("Back", Command.BACK, 0);
        screen.addCommand(backCommand);
    }

    public Displayable getScreen() {
        return screen;
    }

    public void commandAction(Command c, Displayable d) {
        int level;
        if (c == backCommand) {
            level = -1;
        }
        else {
            level = screen.getSelectedIndex() + 1;
        }
        midlet.levelSelected(level);
    }
}
