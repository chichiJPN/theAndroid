package team.virtualnanny;

public class Db_user {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String role;
    private boolean enablePhone;
    private double lastLatitude;
    private double lastLongitude;
    private int numSteps;
    public Db_user() {}

    public Db_user(String firstName,
                   String lastName,
                   String email,
                   String phone,
                   String gender,
                   String role,
                   boolean phoneEnable,
                   double lastLatitude,
                   double lastLongitude,
                   int numSteps) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
        this.enablePhone = phoneEnable;
        this.lastLatitude = lastLatitude; // coordinate of UC
        this.lastLongitude = lastLongitude; // coordinate of UC
        this.numSteps = numSteps; // coordinate of UC
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getPhone() {
        return phone;
    }
    public String getGender() {
        return gender;
    }
    public String getRole() {
        return role;
    }
    public double getLastLatitude() {return lastLatitude; }
    public double getLastLongitude() {return lastLongitude; }
    public int getNumSteps() {return numSteps; }
}
