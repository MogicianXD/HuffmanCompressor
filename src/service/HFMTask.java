package service;

import controller.ProgressController;
import controller.ReplaceController;
import entity.MyFile;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import util.Huffman;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class HFMTask extends Task
{
    private ObservableList<TreeItem<MyFile>> files;
    private int fileNum;
    private String in_file_path;
    private String out_file_path;
    private Huffman hf;
    private ProgressController controller;
    private boolean isToCompress;
    private GridPane bar;

    public HFMTask(){}

    public HFMTask(Huffman hf, ObservableList<TreeItem<MyFile>> files,
            final int fileNum, final String out_file_path, ProgressController controller)
    {
        super();
        this.isToCompress = true;
        this.hf = hf;
        this.fileNum = fileNum;
        this.files = files;
        this.out_file_path = out_file_path;
        this.controller = controller;
        this.bar = controller.add(this);
    }

    public HFMTask(Huffman hf, String in_file_path, String out_file_path, ProgressController controller)
    {
        super();
        this.hf = hf;
        this.isToCompress = false;
        this.in_file_path = in_file_path;
        this.out_file_path = out_file_path;
        this.controller = controller;
        this.bar = controller.add(this);
    }

    public void updateLog(String log)
    {
        this.updateMessage(log);
    }

    public void updateProgressIndex(int progressIndex)
    {
        if(bar != null)
            updateProgress(progressIndex, 100);
    }

    public ReadOnlyStringProperty clock()
    {
        return titleProperty();
    }

    public void pause() {
        hf.pause();
    }

    public void resume()
    {
        hf.resume();
        synchronized (hf)
        {
            hf.notify();
        }
        synchronized (timerTask)
        {
            timerTask.notify();
        }
    }

    private ReplaceController.Mode mode;
    public ReplaceController.Mode getMode() { return mode; }
    private boolean assigned = false;

    public void replaceAsk(String filename)
    {
        try
        {
            if(assigned)
                return;
            Platform.runLater(()->
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/ReplaceDialog.fxml"));
                Parent winRoot = null;
                try
                {
                    winRoot = loader.load();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                Stage dialogStage = new Stage();
                dialogStage.setTitle("包含同名文件，是否覆盖");
                dialogStage.setScene(new Scene(winRoot));
                dialogStage.getIcons().add(
                        new Image(this.getClass().getResource("../res/graphics/icon.jpg").toString()));
                dialogStage.show();
                ReplaceController replaceController = loader.getController();
                replaceController.initialize(this, filename);
            });
            synchronized (hf)
            {
                pause();
                hf.wait();
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void onAnswered(ReplaceController.Mode mode, boolean assigned)
    {
        this.mode = mode;
        this.assigned = assigned;
        resume();
    }

    @Override
    protected void cancelled()
    {
        hf.pause();
        super.cancelled();
    }

    private TimerTask timerTask;

    @Override
    protected Object call() throws Exception
    {
        Timer timer = new Timer();
        timerTask = new TimerTask() {
            long t = 0;
            DecimalFormat df = new DecimalFormat("00");
            public void run() {
                if(hf.isPaused())
                    synchronized (this)
                    {
                        try
                        {
                            this.wait();
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                t++;
                long hh = t / 60 / 60 % 60;
                long mm = t / 60 % 60;
                long ss = t % 60;
                updateTitle(df.format(hh) + ':' + df.format(mm) + ':' + df.format(ss));
            }
        };
        timer.schedule(timerTask, 0, 1000);
        if(isToCompress)
        {
            hf.compress(files, fileNum, out_file_path);
            updateMessage("压缩完成");
        }
        else
        {
            hf.decompress(in_file_path, out_file_path);
            updateMessage("解压完成");
        }
        timer.cancel();
        Platform.runLater(()->{
            if(isCancelled())
                return;
            if(controller.isAutoClose())
                controller.remove(bar);
        });
        return null;

    }
}
