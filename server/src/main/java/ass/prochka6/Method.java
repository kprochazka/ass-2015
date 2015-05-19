package ass.prochka6;

/**
 * HTTP Request methods, with the ability to decode a <code>String</code>
 * back to its enum value.
 */
enum Method {

    GET,
    PUT,
    POST,
    DELETE,
    HEAD,
    OPTIONS;

    static Method lookup(String methodString) {
        for (Method method : Method.values()) {
            if (method.toString().equalsIgnoreCase(methodString)) {
                return method;
            }
        }
        return null;
    }
}
