package ass.pools.prochka6;

import javax.annotation.Nonnull;

/**
 *
 * @author Kamil Prochazka
 */
public class User implements CloneableObject<User> {

    private final String username;
    private final int age;

    public User(String username, int age) {
        this.username = username;
        this.age = age;
    }

    @Nonnull
    @Override
    public User clone() {
        return new User(username, age);
    }

    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", age=" + age +
               '}';
    }
}
