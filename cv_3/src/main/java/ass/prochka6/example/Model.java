package ass.prochka6.example;

//Model doesn't know about View or Controller

public class Model extends java.util.Observable {

    private int counter;

    public Model() {
        System.out.println("Model()");
    }

    public int getValue() {
        return counter;
    }

    public void setValue(int value) {
        this.counter = value;
        doNotify();
    }

    public void incrementValue() {
        ++counter;
        doNotify();
    }

    public void decrementValue() {
        --counter;
        doNotify();
    }

    private void doNotify() {
        System.out.println("Model     : counter = " + counter);
        setChanged();
        // if Push - send counter as part of the message
        notifyObservers(counter);
        // if Pull, then can use notifyObservers()
        // notifyObservers()
    }
}
