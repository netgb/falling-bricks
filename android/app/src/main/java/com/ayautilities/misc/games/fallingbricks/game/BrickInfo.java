package com.ayautilities.misc.games.fallingbricks.game;

import java.util.Random;

/**
 *
 * @author Aaron
 */
public class BrickInfo {
    public int type;
    public int orientation;
    public int lengthY;
    public int lengthX;
    public int[] filledCells;

    private static final BrickInfo[][] brickInfoTable;

    private static final int BRICK_TYPE_COUNT = 7;
    private static final int TYPE_BACKSLASH = 0;
    private static final int TYPE_FORWARD_SLASH = 1;
    public static final int TYPE_RULE = 2;
    private static final int TYPE_BOX = 3;
    private static final int TYPE_T = 4;
    private static final int TYPE_L = 5;
    private static final int TYPE_L_MIRRORED = 6;

    private static final Random rand = new Random();

    static {
        brickInfoTable = new BrickInfo[BRICK_TYPE_COUNT][];

        brickInfoTable[TYPE_BACKSLASH] = new BrickInfo[] {
            new BrickInfo(2, 3, new int[]{0, 1, 1, 0, 1, 1, 2, 0}),
            new BrickInfo(3, 2, new int[]{0, 0, 0, 1, 1, 1, 1, 2})
        };

        brickInfoTable[TYPE_FORWARD_SLASH] = new BrickInfo[] {
            new BrickInfo(2, 3, new int[]{0, 0, 1, 0, 1, 1, 2, 1}),
            new BrickInfo(3, 2, new int[]{0, 1, 1, 0, 1, 1, 0, 2})
        };

        brickInfoTable[TYPE_RULE] = new BrickInfo[] {
            new BrickInfo(4, 1, new int[]{0, 0, 0, 1, 0, 2, 0, 3}),
            new BrickInfo(1, 4, new int[]{0, 0, 1, 0, 2, 0, 3, 0})
        };

        brickInfoTable[TYPE_BOX] = new BrickInfo[] {
            new BrickInfo(2, 2, new int[]{0, 0, 0, 1, 1, 0, 1, 1})
        };

        brickInfoTable[TYPE_T] = new BrickInfo[] {
            new BrickInfo(3, 2, new int[]{0, 0, 0, 1, 0, 2, 1, 1}),
            new BrickInfo(2, 3, new int[]{0, 0, 1, 0, 2, 0, 1, 1}),
            new BrickInfo(3, 2, new int[]{1, 0, 1, 1, 1, 2, 0, 1}),
            new BrickInfo(2, 3, new int[]{1, 0, 0, 1, 1, 1, 2, 1})
        };

        brickInfoTable[TYPE_L] = new BrickInfo[] {
            new BrickInfo(2, 3, new int[]{0, 0, 0, 1, 1, 0, 2, 0}),
            new BrickInfo(3, 2, new int[]{0, 0, 1, 0, 1, 1, 1, 2}),
            new BrickInfo(2, 3, new int[]{0, 1, 1, 1, 2, 1, 2, 0}),
            new BrickInfo(3, 2, new int[]{0, 0, 0, 1, 0, 2, 1, 2})
        };

        brickInfoTable[TYPE_L_MIRRORED] = new BrickInfo[] {
            new BrickInfo(2, 3, new int[]{0, 0, 0, 1, 1, 1, 2, 1}),
            new BrickInfo(3, 2, new int[]{0, 0, 0, 1, 1, 0, 0, 2}),
            new BrickInfo(2, 3, new int[]{0, 0, 1, 0, 2, 0, 2, 1}),
            new BrickInfo(3, 2, new int[]{0, 2, 1, 0, 1, 1, 1, 2})
        };

        // Set type and orientation together.
        for (int i = 0; i < brickInfoTable.length; i++) {
            for (int j = 0; j < brickInfoTable[i].length; j++) {
                brickInfoTable[i][j].type = i;
                brickInfoTable[i][j].orientation = j;
            }
        }
    }

    public static void supplyRandomBrickInfo(BrickInfo dest) {
        int randType = rand.nextInt(brickInfoTable.length);
        int randOrientation = rand.nextInt(brickInfoTable[randType].length);
        supplyBrickInfo(randType, randOrientation, dest);
    }

    public static void supplyBrickInfo(int type, int orientation,
            BrickInfo dest) {
        BrickInfo src = brickInfoTable[type][orientation];
        dest.type = type;
        dest.orientation = orientation;
        dest.lengthX = src.lengthX;
        dest.lengthY = src.lengthY;
        dest.filledCells = src.filledCells;
    }

    public static BrickInfo rotateBrickInfoClockwise(BrickInfo src) {
        int newOrientation = (src.orientation + 1) %
                brickInfoTable[src.type].length;
        return brickInfoTable[src.type][newOrientation];
    }

    public static BrickInfo rotateBrickInfoAntiClockwise(BrickInfo src) {
        int newOrientation = (src.orientation - 1 +
                brickInfoTable[src.type].length) %
                brickInfoTable[src.type].length;
        return brickInfoTable[src.type][newOrientation];
    }

    public BrickInfo() {
    }

    public BrickInfo(int lengthX, int lengthY, int[] filledCells) {
        this.lengthX = lengthX;
        this.lengthY = lengthY;
        this.filledCells = filledCells;
    }
}
