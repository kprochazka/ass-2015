package ass.prochka6.hw;

/**
 * @author Kamil Prochazka
 */
public class Trezor {

    public boolean firstLocked = true;
    private boolean secondLocked = true;

    public void open() {
        if (!this.firstLocked && !this.secondLocked) {
            System.out.println("Two gold bricks. (Received 2000XP)");
        } else if (!this.firstLocked || !this.secondLocked) {
            System.out.println("Gold brick. (Received 1000XP)");
        }
    }

}
