package fallingbricks;

import fallingbricks.infrastructure.AbstractGameTask;
import java.util.Vector;

/**
 *
 * @author Aaron
 */
public class FilledRowClearanceAnimator extends AbstractGameTask {
    private static final int MAX_CELLS_TO_DELETE = 3;

    private final Arena arena;
    private int minRowToCheck;
    private int rowToCheck;
    private Boolean[] rowOccupedStatuses;

    public FilledRowClearanceAnimator(Arena arena) {
        this.arena = arena;
    }

    public boolean filledRowsExist(Brick brick) {
        minRowToCheck = brick.getRowPosition();
        // Set rowToCheck to upper most row.
        rowToCheck = minRowToCheck + brick.getBrickInfo().lengthY - 1;
        Vector nonEmptyRows = arena.getNonEmptyGridRows();
        for (int i = rowToCheck; i >= minRowToCheck; i--) {
            boolean[] rowCellsOccupied = (boolean[])nonEmptyRows.elementAt(i);
            boolean lineFull = true;
            for (int j = 0; j < rowCellsOccupied.length; j++) {
                if (!rowCellsOccupied[j]) {
                    lineFull = false;
                    break;
                }
            }
            if (lineFull) {
                rowOccupedStatuses = new Boolean[rowToCheck-minRowToCheck+1];
                for (int j = rowToCheck; j > i; j--) {
                    rowOccupedStatuses[j-minRowToCheck] = Boolean.FALSE;
                }
                rowOccupedStatuses[i-minRowToCheck] = Boolean.TRUE;
                return true;
            }
        }
        return false;
    }

    public int getDeletedRowCount() {
        int deletedRowCount = 0;
        for (int i = 0; i < rowOccupedStatuses.length; i++) {
            if (rowOccupedStatuses[i].booleanValue()) {
                deletedRowCount++;
            }
        }
        return deletedRowCount;
    }

    public boolean execute() {
        Vector nonEmptyRows = arena.getNonEmptyGridRows();
        int cols = FallingBricksCanvas.GRID_COLUMN_COUNT;
        while (rowToCheck >= minRowToCheck) {
            boolean[] rowCellsOccupied = (boolean[])nonEmptyRows.elementAt(rowToCheck);
            Boolean rowOccupiedStatus = rowOccupedStatuses[rowToCheck-minRowToCheck];
            if (rowOccupiedStatus == null) {
                boolean lineFull = true;
                for (int j = 0; j < rowCellsOccupied.length; j++) {
                    if (!rowCellsOccupied[j]) {
                        lineFull = false;
                        break;
                    }
                }
                rowOccupiedStatus = (lineFull ? Boolean.TRUE : Boolean.FALSE);
                rowOccupedStatuses[rowToCheck-minRowToCheck] = rowOccupiedStatus;
            }
            if (rowOccupiedStatus.booleanValue()) {
                int cellsDeleted = 0;
                if (rowToCheck % 2 == 0) {
                    for (int i = 0; i < cols && cellsDeleted < MAX_CELLS_TO_DELETE; i++) {
                        if (rowCellsOccupied[i]) {
                            rowCellsOccupied[i] = false;
                            cellsDeleted++;
                        }
                    }
                }
                else {
                    for (int i = cols-1; i >= 0 && cellsDeleted < MAX_CELLS_TO_DELETE; i--) {
                        if (rowCellsOccupied[i]) {
                            rowCellsOccupied[i] = false;
                            cellsDeleted++;
                        }
                    }
                }
                if (cellsDeleted > 0) {
                    return false;
                }
                nonEmptyRows.removeElementAt(rowToCheck);
            }
            rowToCheck--;
        }
        return true;
    }
}
