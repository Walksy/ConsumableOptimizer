package walksy.consumableoptimizer.data;

public enum EntityDataTrackerValues {

    CONSUMPTION(8);

    private final int id;

    EntityDataTrackerValues(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
