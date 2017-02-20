package team.virtualnanny;

public class Db_alarm {
    private boolean enable;
    private boolean entering;
    private boolean leaving;
    private int hour;
    private int minute;
    private String period;
    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;

    public Db_alarm() {}


    public Db_alarm(boolean enable,
                    boolean entering,
                    boolean leaving,
                    int hour,
                    int minute,
                    String period,
                    boolean Sunday,
                    boolean Monday,
                    boolean Tuesday,
                    boolean Wednesday,
                    boolean Thursday,
                    boolean Friday,
                    boolean Saturday
    ) {
        this.enable = enable;
        this.entering = entering;
        this.leaving = leaving;
        this.hour = hour;
        this.minute = minute;
        this.period = period;
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
    public boolean getEntering() {
        return entering;
    }
    public boolean getLeaving() {
        return leaving;
    }
    public int getHour() {
        return hour;
    }
    public int getMinute() {
        return minute;
    }
    public String getPeriod() {
        return period;
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
