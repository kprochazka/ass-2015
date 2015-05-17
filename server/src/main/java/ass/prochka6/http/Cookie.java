package ass.prochka6.http;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * HTTP Cookie relaxed representation. Only basic cookie attributes (name,value,expiry) are supported.
 *
 * @author Kamil Prochazka
 */
class Cookie {

    private final String name;
    private final String value;
    private final String expiry;

    public Cookie(String name, String value) {
        this(name, value, 30);
    }

    public Cookie(String name, String value, int numDays) {
        this.name = name;
        this.value = value;
        this.expiry = getHTTPTime(numDays);
    }

    public Cookie(String name, String value, String expires) {
        this.name = name;
        this.value = value;
        this.expiry = expires;
    }

    public String getHTTPHeader() {
        return String.format("%s=%s; expires=%s", this.name, this.value, this.expiry);
    }

    static String getHTTPTime(int days) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Cookie{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", expiry='").append(expiry).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
