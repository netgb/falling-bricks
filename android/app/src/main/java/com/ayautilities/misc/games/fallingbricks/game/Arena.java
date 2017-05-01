package com.ayautilities.misc.games.fallingbricks.game;

import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
/*#*/import android.util.Log;

/**
 *
 * @author Aaron
 */
public class Arena {
	private static final String TAG = Arena.class.getName();

    private static final int BG_COLOR = Color.WHITE;
    private static final int GRID_COLOR = Color.BLACK;
    private static final int CELL_COLOR = Color.YELLOW;
    private static final float CELL_OUTER_RECT_WIDTH_PERCENTAGE = 90.0f;
    private static final float CELL_INNER_RECT_WIDTH_PERCENTAGE = 50.0f;
    private static final int NEXT_GRID_COLUMN_COUNT = 4;
    private static final int NEXT_GRID_ROW_COUNT = 4;

    private int gridRowCount;
    private int gridColumnCount;
    private final Vector nonEmptyGridRows;

    private float gridX, gridY, gridWidth, gridHeight;
    private float cellWidth, cellHeight;

    private int[] nextBrickInfoFilledCells;
    private float statsHeight, scoreX, statsYInc;
    private int[] scores, highScores;
    private int level;

    private boolean artifactsVisible = true; // for game over animation.

    private String gameOverMessage;
    
    private int canvasWidth, canvasHeight;
    
    private float textSize;

	private boolean orientationLandscape;
	private Bitmap b;

    public Arena(int gridRowCount, int gridColumnCount, int[] scores, int[] highScores,
    		float textSize) {
        this.gridRowCount = gridRowCount;
        this.gridColumnCount = gridColumnCount;
        this.scores = scores;
        this.highScores = highScores;
        this.textSize = textSize;
        nonEmptyGridRows = new Vector();

        // Use textSizes to set height of rows to contain scores.
        Paint p = new Paint();
        p.setTextSize(textSize);
        Paint.FontMetrics fm = p.getFontMetrics();
        /*#*/Log.d(TAG, "fm.top: " + fm.top);
        /*#*/Log.d(TAG, "fm.bottom: " + fm.bottom);
        statsYInc = -fm.top + fm.bottom;
        statsHeight = statsYInc*3;
    }
    

    public int getCanvasWidth() {
		return canvasWidth;
	}

	public int getCanvasHeight() {
		return canvasHeight;
	}

	public boolean isArtifactsVisible() {
		return artifactsVisible;
	}

	public String getGameOverMessage() {
		return gameOverMessage;
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

    public float getGridX() {
        return gridX;
    }

    public float getGridY() {
        return gridY;
    }

    public float getCellWidth() {
        return cellWidth;
    }

    public float getCellHeight() {
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

    public void sizeChanged(int f, int g) {
    	canvasWidth = f;
    	canvasHeight = g;
        // Assume small grid for next brick is part of main grid.
        // extra 1 is for empty column between them.
        gridColumnCount += NEXT_GRID_COLUMN_COUNT + 1;
        g -= statsHeight;
        gridHeight = (g / gridRowCount) * gridRowCount;
        gridWidth = (f / gridColumnCount) * gridColumnCount;
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

        gridX = (f - gridWidth) / 2;
        gridY = (g - gridHeight) / 2;

        // Now make gridWidth equal width of main grid.
        gridColumnCount -= NEXT_GRID_COLUMN_COUNT + 1;
        gridWidth = cellWidth * gridColumnCount;

        gridY += statsHeight;

        scoreX = f;
        
        /*#*/Log.d(TAG, String.format("gridX: %f; gridY: %f; gridWidth: %f; gridHeight: %f"
        /*#*/		+ "; cellWidth: %f",
        /*#*/		gridX, gridY, gridWidth, gridHeight, cellWidth));
    }

    public void paint(Canvas g) {
    	Paint bgPaint = new Paint();
    	bgPaint.setColor(BG_COLOR);
    	g.drawRect(0f, 0f, canvasWidth, canvasHeight, bgPaint);
    	
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
            float xpos = canvasWidth / 2;
            float ypos = canvasHeight / 2;
            
            Paint gameOverMessagePaint = new Paint();
            gameOverMessagePaint.setTextSize(textSize);
            gameOverMessagePaint.setTypeface(Typeface.DEFAULT_BOLD);
            gameOverMessagePaint.setColor(Color.RED);
            gameOverMessagePaint.setTextAlign(Paint.Align.CENTER);
            g.drawText(msg1, xpos, ypos, gameOverMessagePaint);
            if (msg2 != null) {
                ypos += statsYInc;
                g.drawText(msg2, xpos, ypos, gameOverMessagePaint);
            }
            return;
        }

        Paint gridPaint = new Paint();
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStyle(Paint.Style.FILL);
        g.drawRect(gridX, gridY, gridX + gridWidth, gridY + gridHeight, gridPaint);

        float x = gridX + gridWidth + cellWidth;
        g.drawRect(x, gridY, x +
                NEXT_GRID_COLUMN_COUNT * cellWidth, gridY + NEXT_GRID_ROW_COUNT *
                        cellHeight, gridPaint);

        if (!artifactsVisible) return;

        Paint statsPaint = new Paint();
        statsPaint.setTextSize(textSize);
        statsPaint.setColor(Color.BLACK);
        statsPaint.setTextAlign(Paint.Align.CENTER);
        float y = statsYInc;
        g.drawText("Level " + level, scoreX/2, y, statsPaint);
        y += statsYInc;
        statsPaint.setTextAlign(Paint.Align.LEFT);
        g.drawText("Total Score: " + scores[0], 0, y, statsPaint);
        statsPaint.setTextAlign(Paint.Align.RIGHT);
        g.drawText("Highest: " + highScores[0], scoreX, y, statsPaint);
        y += statsYInc;
        statsPaint.setTextAlign(Paint.Align.LEFT);
        g.drawText("Level Score: " + scores[level], 0, y, statsPaint);
        statsPaint.setTextAlign(Paint.Align.RIGHT);
        g.drawText("Highest: " + highScores[level], scoreX, y, statsPaint);

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

    public void drawCell(Canvas g, int row, int column) {
        if (row >= gridRowCount) return;
        Paint cellPaint = new Paint();
        cellPaint.setColor(CELL_COLOR);
        cellPaint.setStyle(Paint.Style.STROKE);
        float x = gridX + column * cellWidth +
                cellWidth * (100.0f - CELL_OUTER_RECT_WIDTH_PERCENTAGE) / 200.0f;
        float y = gridY + (gridRowCount-1-row) * cellHeight +
                cellHeight * (100.0f - CELL_OUTER_RECT_WIDTH_PERCENTAGE) / 200.0f;
        g.drawRect(
            x, y, x + cellWidth * CELL_OUTER_RECT_WIDTH_PERCENTAGE / 100.0f,
            y + cellHeight * CELL_OUTER_RECT_WIDTH_PERCENTAGE / 100.0f, cellPaint
        );
        
        cellPaint.setStyle(Paint.Style.FILL);
        x = gridX + column * cellWidth +
                cellWidth * (100.0f - CELL_INNER_RECT_WIDTH_PERCENTAGE) / 200.0f;
        y = gridY + (gridRowCount-1-row) * cellHeight +
                cellHeight * (100.0f - CELL_INNER_RECT_WIDTH_PERCENTAGE) / 200.0f;
        g.drawRect(
            x, y, x + cellWidth * CELL_INNER_RECT_WIDTH_PERCENTAGE / 100,
            y + cellHeight * CELL_INNER_RECT_WIDTH_PERCENTAGE / 100, cellPaint
        );
    }
}
