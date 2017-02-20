package team.virtualnanny;

public class Db_alarm {
    private boolean enable;
    private int hour;
    private int minute;
    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;

    public Db_alarm() {}
    public Db_alarm(boolean enable,
                    int hour,
                    int minute,
                    boolean Sunday,
                    boolean Monday,
                    boolean Tuesday,
                    boolean Wednesday,
                    boolean Thursday,
                    boolean Friday,
                    boolean Saturday
    ) {
        this.enable = enable;
        this.hour = hour;
        this.minute = minute;
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
    public int getHour() {
        return hour;
    }
    public int getMinute() {
        return minute;
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
