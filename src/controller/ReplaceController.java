package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import service.HFMTask;

public class ReplaceController
{

    @FXML
    private Label filename_label;

    @FXML
    private JFXButton replace_btn;

//    @FXML
//    private JFXButton skip_btn;

    @FXML
    private JFXButton reserve_btn;

    @FXML
    private JFXCheckBox check_box;

    public enum Mode{ REPLACE, RESERVE, CANCEL}

    public Mode mode = Mode.CANCEL;

    private HFMTask task;

    public void initialize(HFMTask task, String filename)
    {
        this.task = task;
        filename_label.setText(filename);
        Stage stage = (Stage) filename_label.getScene().getWindow();
        stage.setOnCloseRequest(event ->
        {
            task.onAnswered(mode, assigned());
        });
    }

    void closeWindow()
    {
        Stage stage = (Stage) filename_label.getScene().getWindow();
        stage.close();
        task.onAnswered(mode, assigned());
    }

    @FXML
    void replace(ActionEvent event) {
        mode = Mode.REPLACE;
        closeWindow();
    }

    @FXML
    void reserve(ActionEvent event) {
        mode = Mode.RESERVE;
        closeWindow();
    }

//    @FXML
//    void skip(ActionEvent event) {
//        mode = Mode.SKIP;
//        closeWindow();
//    }

    public boolean assigned()
    {
        return check_box.isSelected();
    }

}
