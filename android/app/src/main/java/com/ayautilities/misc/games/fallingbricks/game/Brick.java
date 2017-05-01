package com.ayautilities.misc.games.fallingbricks.game;

import java.util.Vector;

import android.graphics.Canvas;

/**
 *
 * @author Aaron
 */
public class Brick {
    private final Arena arena;
    private final int gridRowCount, gridColumnCount;
    private final Vector nonEmptyGridRows;

    private final BrickInfo brickInfo, nextBrickInfo;

    private int rowPosition, columnPosition;

    public Brick(Arena arena, int gridRowCount, int gridColumnCount) {
    	this(arena, gridRowCount, gridColumnCount, null, null);
    }
    
    public Brick(Arena arena, int gridRowCount, int gridColumnCount,
    		BrickInfo oldBrickInfo, BrickInfo oldNextBrickInfo) {
        this.arena = arena;
        this.gridRowCount = gridRowCount;
        this.gridColumnCount = gridColumnCount;
        nonEmptyGridRows = arena.getNonEmptyGridRows();

        if (oldBrickInfo == null) {
        	brickInfo = new BrickInfo();
        	nextBrickInfo = new BrickInfo();
        	BrickInfo.supplyRandomBrickInfo(nextBrickInfo);
        	arena.setNextBrickInfoFilledCells(nextBrickInfo.filledCells);
        	_changeBrickInfo();
        }
        else {
        	this.brickInfo = oldBrickInfo;
        	this.nextBrickInfo = oldNextBrickInfo;
        }
    }

    public BrickInfo getNextBrickInfo() {
		return nextBrickInfo;
	}

	public void setRowPosition(int rowPosition) {
		this.rowPosition = rowPosition;
	}

	public void setColumnPosition(int columnPosition) {
		this.columnPosition = columnPosition;
	}

	public void changeBrickInfo() {
        _changeBrickInfo();
    }

    private void _changeBrickInfo() {
        brickInfo.type = nextBrickInfo.type;
        brickInfo.orientation = nextBrickInfo.orientation;
        brickInfo.lengthX = nextBrickInfo.lengthX;
        brickInfo.lengthY = nextBrickInfo.lengthY;
        brickInfo.filledCells = nextBrickInfo.filledCells;
        rowPosition = gridRowCount;
        columnPosition = (gridColumnCount - brickInfo.lengthX) / 2;
        BrickInfo.supplyRandomBrickInfo(nextBrickInfo);
        arena.setNextBrickInfoFilledCells(nextBrickInfo.filledCells);
    }

    public BrickInfo getBrickInfo() {
        return brickInfo;
    }

    public int getRowPosition() {
        return rowPosition;
    }

    public int getColumnPosition() {
        return columnPosition;
    }

    public boolean moveLeft() {
        return move(brickInfo.filledCells, 0, -1);
    }

    public boolean moveRight() {
        return move(brickInfo.filledCells, 0, 1);
    }

    public boolean moveDown() {
        if (move(brickInfo.filledCells, -1, 0)) return true;

        // Integrate brick pieces into grid.
        for (int i = 0; i < brickInfo.filledCells.length; i+=2) {
            int effRow = brickInfo.filledCells[i] + rowPosition;
            int effCol = brickInfo.filledCells[i+1] + columnPosition;

            while (effRow >= nonEmptyGridRows.size()) {
                nonEmptyGridRows.addElement(new boolean[gridColumnCount]);
            }
            boolean[] rowCellsOccupied =
                    (boolean[])nonEmptyGridRows.elementAt(effRow);
            rowCellsOccupied[effCol] = true;
        }
        brickInfo.filledCells = null;
        return false;
    }

    private boolean move(int[] filledCells, int rowChange, int colChange) {
        for (int i = 0; i < filledCells.length; i+=2) {
            int effRow = filledCells[i] + rowPosition + rowChange;
            int effCol = filledCells[i+1] + columnPosition + colChange;
            // Don't check if effRow >= gridRowCount so
            // brick can be positioned outside grid in the beginning.
            if (effRow < 0 || effCol < 0 || effCol >= gridColumnCount)
                return false;
            if (effRow < nonEmptyGridRows.size()) {
                boolean[] rowCellsOccupied =
                        (boolean[])nonEmptyGridRows.elementAt(effRow);
                if (rowCellsOccupied[effCol])
                    return false;
            }
        }
        rowPosition += rowChange;
        columnPosition += colChange;
        return true;
    }

    public void rotateClockwise() {
        rotate(true);
    }

    public void rotateAnticlockwise() {
        rotate(false);
    }

    private void rotate(boolean isClockwise) {
        BrickInfo rotatedBrickInfo = isClockwise ?
                BrickInfo.rotateBrickInfoClockwise(brickInfo) :
                BrickInfo.rotateBrickInfoAntiClockwise(brickInfo);
        int colChangeLimit = brickInfo.lengthX - rotatedBrickInfo.lengthX;
        int colInc = 1;
        if (colChangeLimit < 0) {
            colInc = -1;
        }
        int colChange = colInc;
        boolean rotateSuccessful = false;
        while (colChangeLimit > 0 && colChange <= colChangeLimit ||
                colChangeLimit < 0 && colChange >= colChangeLimit) {
            if (move(rotatedBrickInfo.filledCells, 0, colChange)) {
                BrickInfo.supplyBrickInfo(rotatedBrickInfo.type,
                        rotatedBrickInfo.orientation, brickInfo);
                rotateSuccessful = true;
                break;
            }
            colChange += colInc;
        }
        if (!rotateSuccessful) {
            if (move(rotatedBrickInfo.filledCells, 0, 0)) {
                BrickInfo.supplyBrickInfo(rotatedBrickInfo.type,
                        rotatedBrickInfo.orientation, brickInfo);
            }
        }
    }

    public void paint(Canvas g) {
        // Paint brick in grid.
        if (brickInfo.filledCells != null) {
            for (int i = 0; i < brickInfo.filledCells.length; i+=2) {
                int effRow = brickInfo.filledCells[i] + rowPosition;
                int effCol = brickInfo.filledCells[i+1] + columnPosition;
                arena.drawCell(g, effRow, effCol);
            }
        }
    }
}
