package ass.prochka6.example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;

public class View implements java.util.Observer {

    private TextField inputTextField;
    private TextField errTextField;
    private Button buttonInc;
    private Button buttonDec;
    private Button buttonErr;
    private Button buttonReset;

    private boolean error = false;

    public View(int verticalPosition) {
        System.out.println("View()");

        Frame frame = new Frame("simple MVC");
        frame.add("North", new Label("counter"));

        inputTextField = new TextField();
        frame.add("Center", inputTextField);

        Panel panel = new Panel();
        buttonInc = new Button("Increase");
        panel.add(buttonInc);
        buttonDec = new Button("Decrease");
        panel.add(buttonDec);
        buttonErr = new Button("Error");
        panel.add(buttonErr);
        buttonReset = new Button("Reset");
        panel.add(buttonReset);

        frame.add("South", panel);

        frame.addWindowListener(new CloseListener());
        frame.setSize(350, 100);
        frame.setLocation(100, verticalPosition);
        frame.setVisible(true);

    }

    public void update(Observable obs, Object obj) {

        if (!error) {
            // System.out.println ("View      : Observable is " + obs.getClass() +
            // ", object passed is " + obj.getClass());

            // uncomment next line to do Model Pull
            // myTextField.setText("" + model.getValue());

            // if Push
            inputTextField.setText("" + ((Integer) obj).intValue());
        }

    }

    public void addController(ActionListener controller) {
        System.out.println("View      : adding controller");
        buttonInc.addActionListener(controller);
        buttonDec.addActionListener(controller);
        buttonErr.addActionListener(controller);
        buttonReset.addActionListener(controller);
    }


    public void setValue(String v) {
        error = false;
        buttonInc.setEnabled(true);
        buttonDec.setEnabled(true);
        inputTextField.setText(v);
    }

    public void setError(String v) {
        error = true;
        buttonInc.setEnabled(false);
        buttonDec.setEnabled(false);
        inputTextField.setText(v);
    }

    public static class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            e.getWindow().setVisible(false);
            System.exit(0);
        }
    }

} 
