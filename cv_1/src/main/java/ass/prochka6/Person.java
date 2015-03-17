package ass.prochka6;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Person {

    private int age;
    private String name;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @NotNull
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Size(min = 1, max = 5)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
               "age=" + age +
               ", name='" + name + '\'' +
               '}';
    }

}
