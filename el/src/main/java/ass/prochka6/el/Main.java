package ass.prochka6.el;

import com.sun.el.lang.VariableMapperImpl;

import org.apache.jasper.runtime.ELContextImpl;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.MethodExpression;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import ass.prochka6.model.Event;
import ass.prochka6.model.User;

/**
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class Main {

    static ExpressionFactory factory = ExpressionFactory.newInstance();

    static CustomElResolver resolver;

    public static void main(String[] args) {

        VariableMapper variableMapper = new VariableMapperImpl();
        resolver = new CustomElResolver();
        ELContextImpl context = createContext();
        context.setVariableMapper(variableMapper);
        //

        // test data
        Event parent = new Event();
        parent.setTitle("Parent Event Title");
        Event event = new Event();
        event.setTitle("Event Title");
        event.setParent(parent);

        resolver.put("events", new Event[]{event, parent});

        User user = new User();
        user.setId(0L);
        variableMapper.setVariable("user", factory.createValueExpression(user, User.class));

//        variableMapper.setVariable("i", factory.createValueExpression(0, Integer.class));
        variableMapper.setVariable("i", factory.createValueExpression(context, "#{user.id}", Long.class));

        //

        // tests

        ValueExpression valueExpression = factory.createValueExpression(context, "#{events[i].parent.title}", Object.class);

        System.out.println(valueExpression.getExpectedType());
        System.out.println(valueExpression.getType(context));

        System.out.println(valueExpression.getValue(context));

        valueExpression.setValue(context, "Title 2");

        System.out.println(valueExpression.getValue(context));

        ValueExpression textVO = factory.createValueExpression(context, "${text}", String.class);
//        System.out.println(textVO.getValue(context));
        textVO.setValue(context, "SimpleText");
        System.out.println(textVO.getValue(context));

        MethodExpression
            me =
            factory.createMethodExpression(context, "${events[i].printX(i , events[i].returnX() , 5)}", null, new Class[0]);
        me.invoke(context, new Object[0]);

        MethodExpression
            me2 =
            factory.createMethodExpression(context, "${events[i].returnX}", null, new Class[0]);
        Event result = (Event) me2.invoke(context, new Object[0]);
        System.out.println(result);
        System.out.println(result.getTitle());

        ValueExpression ve2 = factory.createValueExpression(context, "${events[i].title} pepa ${events[1].title}", Object.class);
        System.out.println(ve2.getValue(context));

        ve2.setValue(context, "test");
    }

    private static ELContextImpl createContext() {
        ELContextImpl context = new ELContextImpl(createResolvers());
        return context;
    }

    private static ELResolver createResolvers() {
        CompositeELResolver cResolver = new CompositeELResolver();

        cResolver.add(new MapELResolver());
        cResolver.add(new ResourceBundleELResolver());
        cResolver.add(new ListELResolver());
        cResolver.add(new ArrayELResolver());
        cResolver.add(new BeanELResolver());
        cResolver.add(resolver);

        return cResolver;
    }

}
