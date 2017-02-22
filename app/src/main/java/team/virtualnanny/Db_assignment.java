package team.virtualnanny;

public class Db_assignment {
    private String status;
    private int startHour;
    private int startMinute;
    private int startMonth;
    private int startDay;
    private int endMonth;
    private int endDay;
    private int numCompletion;
    private int numCompletionForReward;
    private String consequence;
    private String reward;

    public Db_assignment() {}

    public Db_assignment(String status,
                         int startHour,
                         int startMinute,
                         int startMonth,
                         int startDay,
                         int endMonth,
                         int endDay,
                         int numCompletion,
                         int numCompletionForReward,
                         String consequence,
                         String reward
                    ) {
        this.status = status;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.numCompletion = numCompletion;
        this.numCompletionForReward = numCompletionForReward;
        this.consequence = consequence;
        this.reward = reward;
    }
    public String getStatus() {
        return status;
    }
    public int getStartHour() {
        return startHour;
    }
    public int getStartMinute() {
        return startMinute;
    }
    public int getStartMonth() {
        return startMonth;
    }
    public int getStartDay() { return startDay; }
    public int getEndMonth() {
        return endMonth;
    }
    public int getEndDay() {
        return endDay;
    }
    public int getNumCompletion() {
        return numCompletion;
    }
    public int getNumCompletionForReward() {
        return numCompletionForReward;
    }
    public String getConsequence() { return consequence; }
    public String getReward() { return reward; }
}
