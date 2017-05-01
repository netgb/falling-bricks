package fallingbricks.infrastructure;

/**
 *
 * @author Aaron
 */
public abstract class AbstractGameTask {
    public int accruedTime;
    public int timeBetweenRuns;

    public abstract boolean execute();
}
