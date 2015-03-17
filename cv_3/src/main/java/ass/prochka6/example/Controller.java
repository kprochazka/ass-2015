package ass.prochka6.example;


public class Controller implements java.awt.event.ActionListener {

    private Model model;
    private View view;

    private int localValue = 1;

    public Controller() {
        System.out.println("Controller()");
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {

        System.out.println("Controller: Event " + e.getActionCommand());

        if ("Increase".equals(e.getActionCommand())) {
            model.incrementValue();
        }
        if ("Decrease".equals(e.getActionCommand())) {
            model.decrementValue();
        }
        if ("Error".equals(e.getActionCommand())) {
            view.setError("Error message: #" + localValue++);
        }
        if ("Reset".equals(e.getActionCommand())) {
            view.setValue(model.getValue() + "");
        }

    }

    public void addModel(Model m) {
        System.out.println("Controller: adding model");
        this.model = m;
    }

    public void addView(View v) {
        System.out.println("Controller: adding view");
        this.view = v;
    }

    public void initModel(int x) {
        model.setValue(x);
    }

} 
