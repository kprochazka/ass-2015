package ass.prochka6;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Simple Object to XML printer which support adding xml attributes based on Annotation inspection on property read method.
 *
 * @author Kamil Prochazka
 * @see ass.prochka6.XmlPrinter.AnnotationMetadataExtractor
 */
public class XmlPrinter {

    private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory tf = TransformerFactory.newInstance();

    private static final List<AnnotationMetadataExtractor> ANNOTATION_METADATA_EXTRACTORS = new CopyOnWriteArrayList<>();

    static {
        ANNOTATION_METADATA_EXTRACTORS.add(new AnnotationMetadataExtractor<NotNull>(NotNull.class) {
            @Override
            public Map<String, String> extractMetadata(Annotation annotation) {
                Map<String, String> properties = new HashMap<String, String>();
                properties.put("notnull", "true");
                return properties;
            }
        });
        ANNOTATION_METADATA_EXTRACTORS.add(new AnnotationMetadataExtractor<Size>(Size.class) {
            @Override
            public Map<String, String> extractMetadata(Annotation annotation) {
                int min = ((Size) annotation).min();
                int max = ((Size) annotation).max();

                Map<String, String> properties = new HashMap<String, String>();
                properties.put("minlength", Integer.toString(min));
                properties.put("maxlength", Integer.toString(max));
                return properties;
            }
        });
    }

    /**
     * Prints given instance object to XML representation.
     *
     * @param instance the instance to be printed to XML
     * @return XML representation of given instance object
     * @throws java.lang.NullPointerException if the instance parameter is null
     */
    public String printToXml(Object instance) {
        if (instance == null) {
            throw new NullPointerException();
        }

        try {
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = db.newDocument();
            Element classElement = document.createElement(instance.getClass().getSimpleName());
            document.appendChild(classElement);

            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(Person.class);
            for (PropertyDescriptor pd : propertyDescriptors) {
                if (pd.getName().contains("class")) {
                    // ignore getClass method
                    continue;
                }

                Method readMethod = pd.getReadMethod();
                if (readMethod == null) {
                    continue;
                }

                Element propertyElement = document.createElement(WordUtils.capitalize(pd.getName()));
                classElement.appendChild(propertyElement);

                // Get value of property
                Object propertyValue = PropertyUtils.getProperty(instance, pd.getName());

                Text propertyValueElement = document.createTextNode(String.valueOf(propertyValue));
                propertyElement.appendChild(propertyValueElement);

                Annotation[] annotations = readMethod.getAnnotations();
                for (Annotation annotation : annotations) {
                    AnnotationMetadataExtractor<?> annotationExtractor = findAnnotationExtractor(annotation);
                    if (annotationExtractor != null) {
                        Map<String, String> attributes = annotationExtractor.extractMetadata(annotation);
                        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                            propertyElement.setAttribute(attribute.getKey(), attribute.getValue());
                        }
                    }
                }
            }

            return printDocument(document);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception occurred :(", e);
        }
    }

    public static <T> void addCustomExtractor(AnnotationMetadataExtractor<T> extractor) {
        if (extractor == null) {
            throw new NullPointerException();
        }
        ANNOTATION_METADATA_EXTRACTORS.add(extractor);
    }

    private AnnotationMetadataExtractor<?> findAnnotationExtractor(Annotation annotation) {
        for (AnnotationMetadataExtractor extractor : ANNOTATION_METADATA_EXTRACTORS) {
            if (extractor.canExtract(annotation)) {
                return extractor;
            }
        }

        return null;
    }

    private String printDocument(Document document) throws IOException, TransformerException {
        StringWriter writer = new StringWriter();

        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(document),
                              new StreamResult(writer));

        return writer.toString();
    }

    /**
     * Annotation metadata extractor for extracting map of settings from supported annotation.
     *
     * @param <T> the supported annotation class type
     */
    static abstract class AnnotationMetadataExtractor<T> {

        protected final Class<T> annotationClass;

        public AnnotationMetadataExtractor(Class<T> annotationClass) {
            if (annotationClass == null) {
                throw new NullPointerException();
            }
            this.annotationClass = annotationClass;
        }

        public boolean canExtract(Annotation annotation) {
            Class<?> actualAnnotationClass = getActualAnnotationClass(annotation);
            if (actualAnnotationClass == annotationClass) {
                return true;
            }

            return false;
        }

        public abstract Map<String, String> extractMetadata(Annotation annotation);

        private Class<?> getActualAnnotationClass(Annotation annotation) {
            if (Proxy.isProxyClass(annotation.getClass())) {
                return annotation.getClass().getInterfaces()[0];
            }

            return annotation.getClass();
        }

    }

}
