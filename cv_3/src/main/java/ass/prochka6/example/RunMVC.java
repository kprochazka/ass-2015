package ass.prochka6.example;

public class RunMVC {

    private int start_value = 10;

    public RunMVC() {

        //create Model and View
        Model model = new Model();

        View view = new View(50);
        View view2 = new View(150);

        //Add listeners
        model.addObserver(view);
        model.addObserver(view2);

        Controller controller = new Controller();
        controller.addModel(model);
        controller.addView(view);

        Controller controller2 = new Controller();
        controller2.addModel(model);
        controller2.addView(view2);

        view.addController(controller);
        view2.addController(controller2);

        model.setValue(start_value);
    }

} 
