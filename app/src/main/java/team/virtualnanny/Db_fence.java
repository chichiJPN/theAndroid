package team.virtualnanny;

public class Db_fence {
    private double radius;
    private int safety;
    private double longitude;
    private double latitude;
    private String type;

    public Db_fence() {}

    public Db_fence(
                    double radius,
                    int safety,
                    double longitude,
                    double latitude,
                    String type) {
        this.radius = radius;
        this.safety = safety;
        this.longitude = longitude;
        this.latitude = latitude;
        this.type = type;
    }
    public double getRadius() {
        return radius;
    }
    public int getSafety() {
        return safety;
    }
    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public String getType() {return type; }
}
