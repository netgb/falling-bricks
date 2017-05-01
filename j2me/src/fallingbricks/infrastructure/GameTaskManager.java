package fallingbricks.infrastructure;

/**
 *
 * @author Aaron
 */
public class GameTaskManager {
    private final int period;

    public GameTaskManager(int period) {
        this.period = period;
    }

    public boolean runTask(AbstractGameTask task) {
        task.accruedTime += period;
        if (task.accruedTime < task.timeBetweenRuns) {
            return false;
        }
        task.accruedTime = 0;
        return task.execute();
    }
}
