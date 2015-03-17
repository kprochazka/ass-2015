package ass.prochka6.model;


/**
 * @author Kamil Prochazka
 */
public class User extends IdEntity {

    private String username;

    private String firstName;

    private String lastName;

    private String email;


    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @Override
    public Long getId() {
        return super.getId();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
