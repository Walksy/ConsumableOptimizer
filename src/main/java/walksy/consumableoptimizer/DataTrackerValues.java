package walksy.consumableoptimizer;

public enum DataTrackerValues {

    HEALTH(9),
    ABSORPTION(15);


    private final int id;
    DataTrackerValues(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }
}
