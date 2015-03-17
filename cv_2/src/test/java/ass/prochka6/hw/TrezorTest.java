package ass.prochka6.hw;

import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Trezor breaker reflection test for {@link ass.prochka6.hw.Trezor}.
 *
 * @author Kamil Prochazka
 */
public class TrezorTest {

    private Trezor trezor = new Trezor();

    @Test
    public void testBreakIntoTrezor() throws NoSuchFieldException, IllegalAccessException {
        // unlock
        trezor.firstLocked = false;

        Field secondLockedField = trezor.getClass().getDeclaredField("secondLocked");
        secondLockedField.setAccessible(true);
        secondLockedField.set(trezor, false);

        trezor.open();
    }

}
