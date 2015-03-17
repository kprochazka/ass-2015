package ass.prochka6;

import org.junit.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link ass.prochka6.XmlPrinter}.
 *
 * @author Kamil Prochazka
 */
public class XmlPrinterTest {

    private XmlPrinter printer = new XmlPrinter();

    @Test
    public void testPersonPrint() throws Exception {
        // test data preparation
        Person person = new Person("Kamil Prochazka", 25);

        // test execution
        String xml = printer.printToXml(person);
        System.out.println(xml);

        // test verification
        String testXml = loadResource("/personTest.xml");
        assertEquals(xml, testXml);
    }

    @Test(expected = NullPointerException.class)
    public void testNotNullParameter() {
        printer.printToXml(null);
    }

    private String loadResource(String resource) throws Exception {
        URI pathString = this.getClass().getResource(resource).toURI();
        Path path = Paths.get(pathString);
        byte[] strings = Files.readAllBytes(path);
        return new String(strings);
    }

}
