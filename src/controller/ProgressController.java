package controller;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.HFMTask;

import java.io.IOException;
import java.util.ArrayList;

public class ProgressController
{
    @FXML
    private VBox vbox;

    @FXML
    private VBox progress_vbox;

    @FXML
    private Button cancel_button;

    @FXML
    private Button pause_button;

    @FXML
    private JFXCheckBox auto_check;

    public boolean isAutoClose()
    {
        return auto_check.isSelected();
    }

    private ArrayList<HFMTask> tasks = new ArrayList<>();

    private boolean paused = false;

    @FXML
    void pauseOrResume(ActionEvent event)
    {
        if(paused)
            resume();
        else
            pause();
    }

    private void pause()
    {
        for(HFMTask task : tasks)
        {
            task.pause();
        }
        paused = true;
        pause_button.setText("继续");
    }

    private void resume()
    {
        paused = false;
        pause_button.setText("暂停");
        for(HFMTask task : tasks)
        {
            task.resume();
        }
    }

    public void cancelTasks()
    {
        for(HFMTask task : tasks)
        {
            task.cancel();
        }
    }

    @FXML
    void cancel(ActionEvent event)
    {
        progressNum = 0;
        if(paused)
            resume();
        cancelTasks();
        Stage stage = (Stage) progress_vbox.getScene().getWindow();
        stage.close();
    }

    public int progressNum = 0;

    public GridPane add(HFMTask task)
    {
        try
        {
            GridPane bar = FXMLLoader.load(getClass().getResource("../view/ProgressBar.fxml"));
            JFXProgressBar progress_bar = (JFXProgressBar) bar.lookup("#progress_bar");
            Label progress_label = (Label) bar.lookup("#progress_label");
            Label time_label = (Label) bar.lookup("#time_label");
            Label log_label = (Label) bar.lookup("#log_label");
            progress_bar.progressProperty().bind(task.progressProperty());
            log_label.textProperty().bind(task.messageProperty());
            StringBinding progress = new StringBinding()
            {
                {
                    super.bind(task.progressProperty());
                }
                @Override
                protected String computeValue()
                {
                    return ((int)(task.progressProperty().get() * 100) + "%");
                }
            };
            progress_label.textProperty().bind(progress);
            time_label.textProperty().bind(task.clock());
            Stage stage = (Stage) progress_vbox.getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.setAlwaysOnTop(false);
            stage.setHeight(stage.getHeight()+bar.getPrefHeight());
            vbox.setPrefHeight(vbox.getHeight()+bar.getPrefHeight());
            progress_vbox.getChildren().add(bar);
            progressNum++;
            tasks.add(task);
            return bar;
        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void remove(GridPane bar)
    {
        Stage stage = (Stage) progress_vbox.getScene().getWindow();
        progress_vbox.getChildren().remove(bar);
//        vbox.setPrefHeight(vbox.getHeight()-bar.getPrefHeight());
        stage.setHeight(stage.getHeight() - bar.getPrefHeight());
        progressNum--;
        if(progressNum == 0)
        {
            stage.close();
        }
    }



}
