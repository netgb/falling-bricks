package fallingbricks.infrastructure;

import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author Aaron
 */
public abstract class MyGameCanvas
        extends GameCanvas implements Runnable {

    /*
     * Variables used for gathering statistics.
     */

    // Record stats every 1 second (roughly)
    private static long MAX_STATS_INTERVAL = 1000L;

    // Number of FPS values stored to get an average
    private static int NUM_FPS = 10;

    private long statsInterval = 0L;    // in ms
    private long prevStatsTime;
    private long totalElapsedTime = 0L;
    private long gameStartTime;
    private int timeSpentInGame = 0;    // in seconds

    private long frameCount = 0;
    private double fpsStore[];
    private long statsCount = 0;
    private double averageFPS = 0.0;

    private long framesSkipped = 0L;
    private long totalFramesSkipped = 0L;
    private double upsStore[];
    private double averageUPS = 0.0;

    /**
     * Variables not involved in statistics gathering start here.
     */

    // Number of frames with a delay of 0 ms before the
    // animation thread yields to other running threads.
    private static final int NO_DELAYS_PER_YIELD = 16;

    // Number of frames that can be skipped in any one animation loop
    // i.e the games state is updated but not rendered.
    private static final int MAX_FRAME_SKIPS = 5;

    private Thread animator;           // the thread that performs the animation
    private volatile boolean running = false; // ends the animation.
    private volatile boolean paused = false;

    private int period;
    private boolean storeStats;

    public MyGameCanvas(int period, boolean storeStats) {
        super(true);

        this.period = period;
        this.storeStats = storeStats;

        if (storeStats) {
            // initialise timing elements
            fpsStore = new double[NUM_FPS];
            upsStore = new double[NUM_FPS];
            for (int i=0; i < NUM_FPS; i++) {
              fpsStore[i] = 0.0;
              upsStore[i] = 0.0;
            }
        }
    }

    // Initialise and start the thread
    private void startGame()
    {
        if (animator == null || !running) {
            animator = new Thread(this);
            animator.start();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void pauseGame()
    {
        paused = true;
    }

    public void resumeGame() {
        paused = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void stopGame()
    {
        running = false;
        if (animator != null && animator != Thread.currentThread()) {
            try {
                animator.join();
            } catch (InterruptedException ex) {}
        }
    }

    protected void showNotify() {
        if (animator == null)
            startGame();
        else
            resumeGame();
    }

    protected void hideNotify() {
        pauseGame();
    }

    public int getPeriod() {
        return period;
    }

    /**
     * Repeatedly process user input, update, render and sleep so looping
     * occurs at approximately period ms.
     * Overruns in update/renders will cause extra updates
     * to be carried out so UPS is almost the same as requested FPS.
     */
    public void run() {
        long beforeTime, afterTime, timeDiff, sleepTime;
        long overSleepTime = 0L;
        int noDelays = 0;
        long excess = 0L;

        gameStartTime = System.currentTimeMillis();
        prevStatsTime = gameStartTime;
        beforeTime = gameStartTime;

        running = true;
        while (running) {
            updateGame();
            renderGame();
            flushGraphics();

            afterTime = System.currentTimeMillis();
            timeDiff = afterTime - beforeTime;
            sleepTime = (period - timeDiff) - overSleepTime;

            if (sleepTime > 0) {   // some time left in this cycle
                try {
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException ex) { }
                overSleepTime = (System.currentTimeMillis() - afterTime) -
                        sleepTime;
            }
            else {    // sleepTime <= 0; frame took longer than the period
                excess -= sleepTime;  // store excess time value
                overSleepTime = 0L;

                if (++noDelays >= NO_DELAYS_PER_YIELD) {
                    Thread.yield();   // give another thread a chance to run
                    noDelays = 0;
                }
            }

            beforeTime = System.currentTimeMillis();

            // If frame animation is taking too long, update the game state
            // without rendering it, to get the updates/sec nearer to
            // the required FPS.
            int skips = 0;
            while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
                excess -= period;
                updateGame();      // update state but don't render
                skips++;
            }
            framesSkipped += skips;

            storeStats();
        }

        printStats();
    }

    protected abstract void updateGame();

    protected abstract void renderGame();

    /**
     * The statistics:
     * - the summed periods for all the iterations in this interval
     *   (period is the amount of time a single frame iteration should take),
     *   the actual elapsed time in this interval,
     *   the error between these two numbers;
     *
     * - the total frame count, which is the total number of calls to run();
     *
     * - the frames skipped in this interval, the total number of frames
     *    skipped. A frame skip is a game update without a corresponding render;
     *
     * - the FPS (frames/sec) and UPS (updates/sec) for this interval,
     *   the average FPS & UPS over the last NUM_FPSs intervals.
     *
     * The data is collected every MAX_STATS_INTERVAL  (1 sec).
     */
    private void storeStats()
    {
        if (!storeStats) return;
        frameCount++;
        statsInterval += period;

        if (statsInterval >= MAX_STATS_INTERVAL) {     // record stats every MAX_STATS_INTERVAL
            long timeNow = System.currentTimeMillis();
            timeSpentInGame = (int) ((timeNow - gameStartTime) / 1000L);  // ms --> secs

            long realElapsedTime = timeNow - prevStatsTime;   // time since last stats collection
            totalElapsedTime += realElapsedTime;

            double timingError
                    = ((double) (realElapsedTime - statsInterval) / statsInterval) * 100.0;

            totalFramesSkipped += framesSkipped;

            double actualFPS = 0;     // calculate the latest FPS and UPS
            double actualUPS = 0;
            if (totalElapsedTime > 0) {
                actualFPS = (((double) frameCount / totalElapsedTime) * 1000L);
                actualUPS = (((double) (frameCount + totalFramesSkipped) / totalElapsedTime)
                        * 1000L);
            }

            // store the latest FPS and UPS
            fpsStore[ (int) statsCount % NUM_FPS] = actualFPS;
            upsStore[ (int) statsCount % NUM_FPS] = actualUPS;
            statsCount = statsCount + 1;

            double totalFPS = 0.0;     // total the stored FPSs and UPSs
            double totalUPS = 0.0;
            for (int i = 0; i < NUM_FPS; i++) {
                totalFPS += fpsStore[i];
                totalUPS += upsStore[i];
            }

            if (statsCount < NUM_FPS) { // obtain the average FPS and UPS
                averageFPS = totalFPS / statsCount;
                averageUPS = totalUPS / statsCount;
            } else {
                averageFPS = totalFPS / NUM_FPS;
                averageUPS = totalUPS / NUM_FPS;
            }

            /*System.out.println(round((double) statsInterval/1000L, 2) + " " +
            round((double) realElapsedTime/1000L, 4) + "s " +
            round(timingError, 2) + "% " +
            frameCount + "c " +
            framesSkipped + "/" + totalFramesSkipped + " skip; " +
            round(actualFPS, 2) + " " + round(averageFPS, 2) + " afps; " +
            round(actualUPS, 2) + " " + round(averageUPS, 2) + " aups" );*/

            framesSkipped = 0;
            prevStatsTime = timeNow;
            statsInterval = 0L;   // reset
        }
  }


    private void printStats()
    {
        if (!storeStats) return;
        System.out.println("Frame Count/Loss: " + frameCount + " / " +
                totalFramesSkipped);
        System.out.println("Average FPS: " + round(averageFPS, 2));
        System.out.println("Average UPS: " + round(averageUPS, 2));
        System.out.println("Time Spent: " + timeSpentInGame + " secs");
        //System.out.println("Boxes used: " + obs.getNumObstacles());
    }

    private static double round(double num, int dp) {
        double mul = 1;
        for (int i = 0; i < dp; i++)
            mul *= 10;
        return Math.floor(mul * num + 0.5 / mul) / mul;
    }
}
