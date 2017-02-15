package team.virtualnanny;

public class Db_user {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String role;

    public Db_user() {}

    public Db_user(String firstName,
                   String lastName,
                   String email,
                   String phone,
                   String gender,
                   String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
    }
    public String getEmail() {
        return email;
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
}
