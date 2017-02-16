package team.virtualnanny;

public class Db_limit {
    private boolean enable;
    private int numHours;
    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;

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
        this.sunday = Sunday;
        this.monday = Monday;
        this.tuesday = Tuesday;
        this.wednesday = Wednesday;
        this.thursday = Thursday;
        this.friday = Friday;
        this.saturday = Saturday;
    }
    public boolean getEnable() {
        return enable;
    }
    public int getNumHours() {
        return numHours;
    }
    public boolean getSunday() {
        return sunday;
    }
    public boolean getMonday() {
        return monday;
    }
    public boolean getTuesday() {
        return tuesday;
    }
    public boolean getWednesday() {
        return wednesday;
    }
    public boolean getThursday() {
        return thursday;
    }
    public boolean getFriday() {
        return friday;
    }
    public boolean getSaturday() {
        return saturday;
    }
}
