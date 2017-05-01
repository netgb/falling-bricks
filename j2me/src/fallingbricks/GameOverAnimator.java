package fallingbricks;

import fallingbricks.infrastructure.AbstractGameTask;
import java.util.Vector;

/**
 *
 * @author Aaron
 */
public class GameOverAnimator extends AbstractGameTask {
    private static final int MAX_CELLS_TO_FILL = 10;
    private static final int FLASH_PERIOD = 400; // milliseconds.
    private static final int MAX_FLASH_RUN_COUNT = 2;

    private final Arena arena;
    private int currentRow, currentCol;
    private boolean gridFilled;
    private int flashRunCount;

    public GameOverAnimator(Arena arena) {
        this.arena = arena;
    }

    public boolean isGameOver() {
        boolean gameOver = arena.getNonEmptyGridRows().size() >
                FallingBricksCanvas.GRID_ROW_COUNT;
        if (gameOver) {
            currentRow = 0;
            currentCol = 0;
            gridFilled = false;
        }
        return gameOver;
    }

    public boolean execute() {
        if (!gridFilled) {
            gridFilled = fillGrid();
            if (!gridFilled)
                return false;
            else {
                flashRunCount = 0;
                timeBetweenRuns = FLASH_PERIOD;
            }
        }
        return flashGrid();
    }

    private boolean fillGrid() {
        Vector nonEmptyRows = arena.getNonEmptyGridRows();
        int rows = FallingBricksCanvas.GRID_ROW_COUNT;
        int cols = FallingBricksCanvas.GRID_COLUMN_COUNT;
        int cellsFilled = 0;
        while (currentRow < rows) {
            boolean[] rowCellsOccupied;
            if (currentRow < nonEmptyRows.size()) {
                rowCellsOccupied = (boolean[])nonEmptyRows.elementAt(
                    currentRow);
            }
            else {
                rowCellsOccupied = new boolean[cols];
                nonEmptyRows.addElement(rowCellsOccupied);
            }
            if (currentRow % 2 == 0) {
                for (; currentCol < cols; currentCol++) {
                    rowCellsOccupied[currentCol] = true;
                    cellsFilled++;
                    if (cellsFilled >= MAX_CELLS_TO_FILL) {
                        return false;
                    }
                }
            }
            else {
                for (; currentCol > 0; currentCol--) {
                    rowCellsOccupied[currentCol-1] = true;
                    cellsFilled++;
                    if (cellsFilled >= MAX_CELLS_TO_FILL) {
                        return false;
                    }
                }
            }
            currentRow++;
        }
        if (cellsFilled > 0) {
            return false;
        }
        return true;
    }

    private boolean flashGrid() {
        if (flashRunCount > MAX_FLASH_RUN_COUNT) {
            return true;
        }
        arena.setArtifactsVisible(flashRunCount % 2 == 0);
        flashRunCount++;
        return false;
    }
}
