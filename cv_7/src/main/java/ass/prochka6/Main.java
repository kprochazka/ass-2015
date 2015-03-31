package ass.prochka6;

import de.odysseus.el.util.SimpleContext;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;

/**
 * @author Kamil Prochazka
 */
public class Main {

    public static void main(String[] args) {
        ExpressionFactory expressionFactory = ExpressionFactory.newInstance();

        SimpleContext context = new SimpleContext();
        CompositeELResolver compositeELResolver = new CompositeELResolver();
        compositeELResolver.add(new ArrayELResolver());
        compositeELResolver.add(new ListELResolver());
        compositeELResolver.add(new MapELResolver());
        compositeELResolver.add(new BeanELResolver());
        context.setELResolver(compositeELResolver);

        // assign mapping of person to context
        context.getVariableMapper().setVariable("p", expressionFactory.createValueExpression(new Person("Kamil"), Person.class));

        ValueExpression ve = expressionFactory.createValueExpression(context, "${p.id.concat('a')}", String.class);
        Object value = ve.getValue(context);
        System.out.println(value);
    }

}
