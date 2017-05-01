package fallingbricks;

import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Aaron
 */
public class Arena {
    private static final int GRID_COLOR = 0x000000; // black
    private static final int CELL_COLOR = 0xffff00; // yellow
    private static final int CELL_OUTER_RECT_WIDTH_PERCENTAGE = 90;
    private static final int CELL_INNER_RECT_WIDTH_PERCENTAGE = 50;
    private static final int NEXT_GRID_COLUMN_COUNT = 4;
    private static final int NEXT_GRID_ROW_COUNT = 4;

    private FallingBricksCanvas gameCanvas;
    private int gridRowCount;
    private int gridColumnCount;
    private final Vector nonEmptyGridRows;

    private int gridX, gridY, gridWidth, gridHeight;
    private int cellWidth, cellHeight;

    private int[] nextBrickInfoFilledCells;
    private int statsHeight, scoreX, statsYInc;
    private int[] scores, highScores;
    private int level;

    private boolean artifactsVisible = true; // for game over animation.

    private String gameOverMessage;

    public Arena(FallingBricksCanvas gameCanvas,
            int gridRowCount, int gridColumnCount, int[] scores, int[] highScores) {
        this.gameCanvas = gameCanvas;
        this.gridRowCount = gridRowCount;
        this.gridColumnCount = gridColumnCount;
        this.scores = scores;
        this.highScores = highScores;
        nonEmptyGridRows = new Vector();

        statsYInc = Font.getDefaultFont().getHeight();
        statsHeight = statsYInc*3;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int[] getNextBrickInfoFilledCells() {
        return nextBrickInfoFilledCells;
    }

    public void setNextBrickInfoFilledCells(int[] nextBrickInfoFilledCells) {
        this.nextBrickInfoFilledCells = nextBrickInfoFilledCells;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public Vector getNonEmptyGridRows() {
        return nonEmptyGridRows;
    }

    public void setArtifactsVisible(boolean artifactsVisible) {
        this.artifactsVisible = artifactsVisible;
    }

    public void setGameOverMessage(String gameOverMessage) {
        this.gameOverMessage = gameOverMessage;
    }

    public void sizeChanged(int w, int h) {
        // Assume small grid for next brick is part of main grid.
        // extra 1 is for empty column between them.
        gridColumnCount += NEXT_GRID_COLUMN_COUNT + 1;
        h -= statsHeight;
        gridHeight = (h / gridRowCount) * gridRowCount;
        gridWidth = (w / gridColumnCount) * gridColumnCount;
        cellWidth = gridWidth / gridColumnCount;
        cellHeight = gridHeight / gridRowCount;
        if (cellWidth > cellHeight) {
            cellWidth = cellHeight;
            gridWidth = cellWidth * gridColumnCount;
        }
        else if (cellHeight > cellWidth) {
            cellHeight = cellWidth;
            gridHeight = cellHeight * gridRowCount;
        }

        gridX = (w - gridWidth) / 2;
        gridY = (h - gridHeight) / 2;

        // Now make gridWidth equal width of main grid.
        gridColumnCount -= NEXT_GRID_COLUMN_COUNT + 1;
        gridWidth = cellWidth * gridColumnCount;

        gridY += statsHeight;

        scoreX = w;
    }

    public void paint(Graphics g) {
        if (gameOverMessage != null) {
            String msg1, msg2;
            int newlineIndex = gameOverMessage.indexOf('\n');
            if (newlineIndex == -1) {
                msg1 = gameOverMessage;
                msg2 = null;
            }
            else {
                msg1 = gameOverMessage.substring(0, newlineIndex);
                msg2 = gameOverMessage.substring(newlineIndex+1);
            }
            int xpos = gameCanvas.getWidth() / 2;
            int ypos = gameCanvas.getHeight() / 2;
            g.setColor(0xff0000); // red
            g.drawString(msg1, xpos, ypos, Graphics.BASELINE |
                    Graphics.HCENTER);
            if (msg2 != null) {
                ypos += g.getFont().getHeight();
                g.drawString(msg2, xpos, ypos, Graphics.BASELINE |
                        Graphics.HCENTER);
            }
            return;
        }

        g.setColor(GRID_COLOR);
        g.fillRect(gridX, gridY, gridWidth, gridHeight);

        g.fillRect(gridX + gridWidth + cellWidth, gridY,
                NEXT_GRID_COLUMN_COUNT * cellWidth, NEXT_GRID_ROW_COUNT *
                        cellHeight);



        if (!artifactsVisible) return;

        g.setColor(0x000000); // black
        int y = 0;
        g.drawString("Level " + level, scoreX/2, y,
                Graphics.TOP | Graphics.HCENTER);
        y += statsYInc;
        g.drawString("Total Score: " + scores[0], 0, y,
                Graphics.TOP | Graphics.LEFT);
        g.drawString("Highest: " + highScores[0], scoreX, y,
                Graphics.TOP | Graphics.RIGHT);
        y += statsYInc;
        g.drawString("Level Score: " + scores[level], 0, y,
                Graphics.TOP | Graphics.LEFT);
        g.drawString("Highest: " + highScores[level], scoreX, y,
                Graphics.TOP | Graphics.RIGHT);

        for (int i = 0; i < nonEmptyGridRows.size(); i++) {
            boolean[] rowCellsOccupied = (boolean[])nonEmptyGridRows.elementAt(i);
            for (int j = 0; j < rowCellsOccupied.length; j++) {
                boolean cellOccupied = rowCellsOccupied[j];
                if (cellOccupied) {
                    drawCell(g, i, j);
                }
            }
        }

        if (nextBrickInfoFilledCells != null) {
            int baseRow = gridRowCount - NEXT_GRID_ROW_COUNT;
            int baseColumn = gridColumnCount + 1;
            for (int i = 0; i < nextBrickInfoFilledCells.length; i+=2) {
                int effRow = nextBrickInfoFilledCells[i] + baseRow;
                int effCol = nextBrickInfoFilledCells[i+1] + baseColumn;
                drawCell(g, effRow, effCol);
            }
        }
    }

    public void drawCell(Graphics g, int row, int column) {
        if (row >= gridRowCount) return;
        g.setColor(CELL_COLOR);
        g.drawRect(
            gridX + column * cellWidth +
                    cellWidth * (100 - CELL_OUTER_RECT_WIDTH_PERCENTAGE) / 200,
            gridY + (gridRowCount-1-row) * cellHeight +
                    cellHeight * (100 - CELL_OUTER_RECT_WIDTH_PERCENTAGE) / 200,
            cellWidth * CELL_OUTER_RECT_WIDTH_PERCENTAGE / 100,
            cellHeight * CELL_OUTER_RECT_WIDTH_PERCENTAGE / 100
        );
        g.fillRect(
            gridX + column * cellWidth +
                    cellWidth * (100 - CELL_INNER_RECT_WIDTH_PERCENTAGE) / 200,
            gridY + (gridRowCount-1-row) * cellHeight +
                    cellHeight * (100 - CELL_INNER_RECT_WIDTH_PERCENTAGE) / 200,
            cellWidth * CELL_INNER_RECT_WIDTH_PERCENTAGE / 100,
            cellHeight * CELL_INNER_RECT_WIDTH_PERCENTAGE / 100
        );
    }
}
