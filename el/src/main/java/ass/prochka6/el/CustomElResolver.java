package ass.prochka6.el;

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;

/**
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class CustomElResolver extends ELResolver {

    private Map<String, Object> map = new HashMap<>();

    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base != null) {
            return null;
        }
        if (property == null) {
            throw new PropertyNotFoundException("Property not found :(");
        }

        Object value = map.get((String) property);
        if (value != null) {
            context.setPropertyResolved(true);
            return value;
        }

        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        if (base != null) {
            return null;
        }
        if (property == null) {
            throw new PropertyNotFoundException("Property not found :(");
        }

        context.setPropertyResolved(true);
        return Object.class;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if (base != null) {
            return;
        }
        if (property == null) {
            throw new PropertyNotFoundException("Property not found :(");
        }

        context.setPropertyResolved(true);
        String attribute = (String) property;
        map.put(attribute, value);
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        // fuck this
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base != null) {
            return null;
        }
        return Object.class;
    }

    public static FeatureDescriptor getFeatureDescriptor(String name, String
        displayName, String desc, boolean expert, boolean hidden,
                                                         boolean preferred, Object type, Boolean designTime) {

        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName(name);
        fd.setDisplayName(displayName);
        fd.setShortDescription(desc);
        fd.setExpert(expert);
        fd.setHidden(hidden);
        fd.setPreferred(preferred);
        fd.setValue(ELResolver.TYPE, type);
        fd.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, designTime);
        return fd;
    }
}
