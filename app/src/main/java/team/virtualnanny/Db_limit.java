package team.virtualnanny;

public class Db_limit {
    private boolean enable;
    private int numHours;
    private boolean Sunday;
    private boolean Monday;
    private boolean Tuesday;
    private boolean Wednesday;
    private boolean Thursday;
    private boolean Friday;
    private boolean Saturday;

    public Db_limit() {}

    public Db_limit(boolean enable,
                    int numHours,
                    boolean Sunday,
                    boolean Monday,
                    boolean Tuesday,
                    boolean Wednesday,
                    boolean Thursday,
                    boolean Friday,
                    boolean Saturday
                    ) {
        this.enable = enable;
        this.numHours = numHours;
        this.Sunday = Sunday;
        this.Monday = Monday;
        this.Tuesday = Tuesday;
        this.Wednesday = Wednesday;
        this.Thursday = Thursday;
        this.Friday = Friday;
        this.Saturday = Saturday;
    }
    public boolean getEnable() {
        return enable;
    }
    public int getNumHours() {
        return numHours;
    }
    public boolean getSunday() {
        return Sunday;
    }
    public boolean getMonday() {
        return Monday;
    }
    public boolean getTuesday() {
        return Tuesday;
    }
    public boolean getWednesday() {
        return Wednesday;
    }
    public boolean getThursday() {
        return Thursday;
    }
    public boolean getFriday() {
        return Friday;
    }
    public boolean getSaturday() {
        return Saturday;
    }
}
