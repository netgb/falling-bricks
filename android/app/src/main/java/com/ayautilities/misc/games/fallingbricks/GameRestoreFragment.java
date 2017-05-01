package com.ayautilities.misc.games.fallingbricks;

import java.util.Vector;

import com.ayautilities.misc.games.fallingbricks.game.BrickInfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class GameRestoreFragment extends Fragment {
    public static final int LEVEL_COUNT = 10;
    public static final String INTENT_EXTRA_DATA_KEY_LEVEL = "com.ayautilities.misc.games.fallingbricks.level";
    public static final String HIGH_SCORE_PREF_KEY_TOTAL = "total";
    public static final String HIGH_SCORE_PREF_KEY_SINGLE_FORMAT = "level%d";
	public static final String PREF_FILE_NAME = "highscores";
    
	private int level;
	private final int[] scores = new int[LEVEL_COUNT + 1];
	private final int[] highScores = new int[LEVEL_COUNT + 1];
	
	private boolean gamePaused;
	private volatile boolean edited; // volatile so surface thread and ui thread 
									// read and writes are observable to each other.
	
	private long fg_levelPeriod, fg_systemTaskPeriod, fg_gameOverPeriod;
	private long fg_gameAdvancerTime, fg_systemTaskTime;
    
    private boolean fg_gameOver;
    private boolean fg_rowClearanceInProgress;

    private long fg_levelStartTime;
    
    private boolean fg_gameOverCleanUpInitiated;

    private Vector a_nonEmptyGridRows;
    private boolean a_artifactsVisible;
    private int[] a_nextBrickInfoFilledCells;
    private String a_gameOverMessage;
    
    private int frca_minRowToCheck;
    private int frca_rowToCheck;
    private Boolean[] frca_rowOccupiedStatuses;

    private BrickInfo b_brickInfo, b_nextBrickInfo;
    private int b_rowPosition, b_columnPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isGamePaused() {
		return gamePaused;
	}

	public void setGamePaused(boolean gamePaused) {
		this.gamePaused = gamePaused;
	}

	public int[] getHighScores() {
		return highScores;
	}
	
	public int[] getScores() {
		return scores;
	}

	public long getFg_levelPeriod() {
		return fg_levelPeriod;
	}

	public void setFg_levelPeriod(long fg_levelPeriod) {
		this.fg_levelPeriod = fg_levelPeriod;
	}

	public long getFg_systemTaskPeriod() {
		return fg_systemTaskPeriod;
	}

	public void setFg_systemTaskPeriod(long fg_systemTaskPeriod) {
		this.fg_systemTaskPeriod = fg_systemTaskPeriod;
	}

	public long getFg_gameOverPeriod() {
		return fg_gameOverPeriod;
	}

	public void setFg_gameOverPeriod(long fg_gameOverPeriod) {
		this.fg_gameOverPeriod = fg_gameOverPeriod;
	}

	public long getFg_gameAdvancerTime() {
		return fg_gameAdvancerTime;
	}

	public void setFg_gameAdvancerTime(long fg_gameAdvancerTime) {
		this.fg_gameAdvancerTime = fg_gameAdvancerTime;
	}

	public long getFg_systemTaskTime() {
		return fg_systemTaskTime;
	}

	public void setFg_systemTaskTime(long fg_systemTaskTime) {
		this.fg_systemTaskTime = fg_systemTaskTime;
	}

	public boolean isFg_gameOver() {
		return fg_gameOver;
	}

	public void setFg_gameOver(boolean fg_gameOver) {
		this.fg_gameOver = fg_gameOver;
	}

	public boolean isFg_rowClearanceInProgress() {
		return fg_rowClearanceInProgress;
	}

	public void setFg_rowClearanceInProgress(boolean fg_rowClearanceInProgress) {
		this.fg_rowClearanceInProgress = fg_rowClearanceInProgress;
	}

	public long getFg_levelStartTime() {
		return fg_levelStartTime;
	}

	public void setFg_levelStartTime(long fg_levelStartTime) {
		this.fg_levelStartTime = fg_levelStartTime;
	}

	public boolean isFg_gameOverCleanUpInitiated() {
		return fg_gameOverCleanUpInitiated;
	}

	public void setFg_gameOverCleanUpInitiated(boolean fg_gameOverCleanUpInitiated) {
		this.fg_gameOverCleanUpInitiated = fg_gameOverCleanUpInitiated;
	}

	public Vector getA_nonEmptyGridRows() {
		return a_nonEmptyGridRows;
	}

	public void setA_nonEmptyGridRows(Vector a_nonEmptyGridRows) {
		this.a_nonEmptyGridRows = a_nonEmptyGridRows;
	}

	public boolean isA_artifactsVisible() {
		return a_artifactsVisible;
	}

	public void setA_artifactsVisible(boolean a_artifactsVisible) {
		this.a_artifactsVisible = a_artifactsVisible;
	}

	public int[] getA_nextBrickInfoFilledCells() {
		return a_nextBrickInfoFilledCells;
	}

	public void setA_nextBrickInfoFilledCells(int[] a_nextBrickInfoFilledCells) {
		this.a_nextBrickInfoFilledCells = a_nextBrickInfoFilledCells;
	}

	public String getA_gameOverMessage() {
		return a_gameOverMessage;
	}

	public void setA_gameOverMessage(String a_gameOverMessage) {
		this.a_gameOverMessage = a_gameOverMessage;
	}

	public int getFrca_minRowToCheck() {
		return frca_minRowToCheck;
	}

	public void setFrca_minRowToCheck(int frca_minRowToCheck) {
		this.frca_minRowToCheck = frca_minRowToCheck;
	}

	public int getFrca_rowToCheck() {
		return frca_rowToCheck;
	}

	public void setFrca_rowToCheck(int frca_rowToCheck) {
		this.frca_rowToCheck = frca_rowToCheck;
	}

	public Boolean[] getFrca_rowOccupiedStatuses() {
		return frca_rowOccupiedStatuses;
	}

	public void setFrca_rowOccupiedStatuses(Boolean[] frca_rowOccupiedStatuses) {
		this.frca_rowOccupiedStatuses = frca_rowOccupiedStatuses;
	}

	public BrickInfo getB_brickInfo() {
		return b_brickInfo;
	}

	public void setB_brickInfo(BrickInfo b_brickInfo) {
		this.b_brickInfo = b_brickInfo;
	}

	public BrickInfo getB_nextBrickInfo() {
		return b_nextBrickInfo;
	}

	public void setB_nextBrickInfo(BrickInfo b_nextBrickInfo) {
		this.b_nextBrickInfo = b_nextBrickInfo;
	}

	public int getB_rowPosition() {
		return b_rowPosition;
	}

	public void setB_rowPosition(int b_rowPosition) {
		this.b_rowPosition = b_rowPosition;
	}

	public int getB_columnPosition() {
		return b_columnPosition;
	}

	public void setB_columnPosition(int b_columnPosition) {
		this.b_columnPosition = b_columnPosition;
	}
}
