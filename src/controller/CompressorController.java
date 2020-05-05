package controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import entity.MyFile;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import service.HFMTask;
import util.Huffman;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class CompressorController
{
    @FXML
    private JFXButton compress_btn;

    @FXML
    private JFXButton decompress_btn;

    @FXML
    private JFXButton add_file_btn;

    @FXML
    private JFXButton remove_file_btn;

    @FXML
    private JFXButton display_btn;

    @FXML
    private Label label;

    @FXML
    private BorderPane borderPane;

    private int fileNum = 0;

    private boolean initialized;

    private TreeTableView treeTableView;

    private final TreeItem<MyFile> root = new TreeItem<>(new MyFile("root", 0L, ""), null);;

    private void initialize()
    {
        TreeTableColumn<MyFile, String> fileColumn =
                new TreeTableColumn<>("文件名");
        fileColumn.setMinWidth(200);
        fileColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<MyFile, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );

        TreeTableColumn<MyFile, String> fileSizeColumn = new TreeTableColumn<>("文件大小");
        fileSizeColumn.setMinWidth(150);
        fileSizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("size"));

        TreeTableColumn<MyFile, String> filePathColumn = new TreeTableColumn<>("文件路径");
        filePathColumn.setMinWidth(448);
        filePathColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<MyFile, String> param) ->
                        new ReadOnlyStringWrapper(param.getValue().getValue().getPath())
        );
        treeTableView = new TreeTableView<>(root);
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeTableView.getColumns().setAll(fileColumn, fileSizeColumn, filePathColumn);
        treeTableView.setShowRoot(false);
        treeTableView.setPlaceholder(new Label(""));
        treeTableView.getStylesheets().add(getClass().getResource("../view/TreeTableView.css").toExternalForm());
        fileSizeColumn.setStyle("-fx-alignment: TOP-RIGHT;");
        borderPane.setCenter(treeTableView);
        initialized = true;
    }

    private void putFile(List<File> files)
    {
        if (files!=null)
        {
            files.stream().forEach((file) -> {
                //避免读入重复的文件
                for(TreeItem<MyFile> other : root.getChildren())
                {
                    String str = file.getAbsolutePath();
                    if(file.isDirectory())
                        str += File.separator;
                    if(str.equals(other.getValue().getPath()))
                        return;
                }
                ImageView fileIcon = getIcon(file);
//                ImageView fileIcon = new ImageView (new Image(getClass().getResourceAsStream("../res/graphics/icon_file.png")));
                root.getChildren().add(new TreeItem<>(
                        new MyFile(file.getName(), file.length(), file.getAbsolutePath()), fileIcon));
            });
        }
    }

    private void putFile(List<File> files, TreeItem<MyFile> folder)
    {
        if(files.isEmpty())
            return;
        if(folder == null)
        {
            putFile(files);
        }
        else
            files.stream().forEach((file) ->{
//                //避免读入重复的文件
//                AtomicBoolean repeated = new AtomicBoolean(false);
//                folder.getChildren().forEach((other)->{
//                    if(file.getAbsolutePath().equals(other.getValue().getPath()))
//                        repeated.set(true);
//                });
//                if(repeated.get())
//                    return;
                ImageView fileIcon = getIcon(file);
                if(file.isDirectory())
                {
                    List lower = Arrays.asList(file.listFiles());
                    TreeItem item = new TreeItem<>(
                            new MyFile(file.getName(), 0L, file.getAbsolutePath()+File.separator), fileIcon);
                    folder.getChildren().add(item);
                    if(!lower.isEmpty())
                        putFile(lower, item);
                }
                else
                {
                    folder.getChildren().add(new TreeItem<>(
                            new MyFile(file.getName(), file.length(), file.getAbsolutePath()), fileIcon));
                }
            });
    }


    private ImageView getIcon(File file)
    {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        ImageIcon icon = (ImageIcon)fsv.getSystemIcon(file);
        java.awt.Image awtImage = icon.getImage();
        BufferedImage bImg;

        int width = (int) (awtImage.getWidth(null) * 1.2);
        int height = (int) (awtImage.getHeight(null) * 1.2);
        bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bImg.getGraphics();
        graphics.drawImage(awtImage, 0, 0, width, height, null);
        graphics.dispose();

        return new ImageView(SwingFXUtils.toFXImage(bImg, null));
    }

    private void getChildrenNum(TreeItem<MyFile> parent)
    {
        fileNum++;
        if(!parent.isLeaf())
            for(TreeItem child : parent.getChildren())
                getChildrenNum(child);
    }


    private boolean toDecompress = false;

    private boolean isHFMFile(List<File> files)
    {
        String path = files.get(0).getAbsolutePath();
        if(files.size() == 1 &&
                path.endsWith(".hfm"))
        {
            compress_btn.setDisable(true);
            decompress_btn.setDisable(false);
            remove_file_btn.setDisable(false);
            add_file_btn.setDisable(true);
            toDecompress = true;
        }
        else
        {
            compress_btn.setDisable(false);
            decompress_btn.setDisable(true);
            remove_file_btn.setDisable(false);
            add_file_btn.setDisable(false);
            toDecompress = false;
        }
        return toDecompress;
    }

    private void putHFMFile(File file)
    {
        try
        {
            String rootPath = file.getName().substring(0, file.getName().lastIndexOf('.'));
            TreeItem<MyFile> hfm = new TreeItem<>(
                    new MyFile(file.getName(), file.length(), file.getAbsolutePath()), getIcon(file));
            root.getChildren().add(hfm);

            Huffman hf = new Huffman();
            String[] filePath = hf.getDictionary(file.getAbsolutePath());
            fileNum = filePath.length;
            TreeItem<MyFile>[] items = new TreeItem[fileNum];
            String fileName;
            TreeItem<MyFile> parent, temp;
            File tempFile;
            ImageView iv = getIcon(file.getParentFile());
            for(int i = 0; i < fileNum; i++)
            {
                parent = hfm;
                if(filePath[i].charAt(0) == '<')
                {
                    int index = Integer.parseInt(filePath[i].substring(1, filePath[i].indexOf('>')));
                    filePath[i] = filePath[index]
                            + filePath[i].substring(filePath[i].indexOf('>')+File.separator.length()+1);
                    parent = items[index];
                }
                fileName = filePath[i].substring(filePath[i].lastIndexOf(File.separator)+File.separator.length());
                if(filePath[i].endsWith(File.separator))
                {
                    fileName = filePath[i].substring(0, filePath[i].length()-1);
                    temp = new TreeItem<>(new MyFile(fileName, 0L, filePath[i]), iv);
                    items[i] = temp;
                    parent.getChildren().add(temp);
                }
                else
                {
                    tempFile = new File(file.getParentFile().getAbsolutePath() + "\\" + fileName);
                    boolean tag = tempFile.exists();
                    if(!tag)
                        tempFile.createNewFile();
                    temp = new TreeItem<>(new MyFile(fileName, 0L, filePath[i]), getIcon(tempFile));
                    parent.getChildren().add(temp);
                    if(!tag)
                        tempFile.delete();
                }
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    @FXML
    void addFile(ActionEvent event)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择一个或多个文件");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ALL", "*"),
                new FileChooser.ExtensionFilter("HFM", "*.hfm"));
        List<File> files = chooser.showOpenMultipleDialog(null);
        if(files == null)
            return;
        if(!initialized)
            initialize();
        if(isHFMFile(files))
            putHFMFile(files.get(0));
        else if(!toDecompress)
            putFile(files);
    }

    @FXML
    void compress(ActionEvent event)
    {
        //复制一下，避免之后删除子节点时造成数据丢失
        ObservableList<TreeItem<MyFile>> files = FXCollections.observableArrayList(root.getChildren());
        if(files == null)
            return;

        String iniPath = files.get(0).getValue().getPath();
        String iniName = "";
        if(files.size() != 1)
        {
            iniPath = iniPath.substring(0, iniPath.lastIndexOf(File.separator, iniPath.length()-2));
            iniName = iniPath.substring(iniPath.lastIndexOf(File.separator) + File.separator.length());
        }
        else
        {
            iniName = iniPath.substring(iniPath.lastIndexOf(File.separator, iniPath.length()-2) + File.separator.length());
            iniPath = iniPath.substring(0, iniPath.lastIndexOf(File.separator, iniPath.length()-2));
            if(iniName.endsWith(File.separator))
                iniName = iniName.substring(0, iniName.length()-File.separator.length());
            else
            {
                int pos;
                if((pos = iniName.indexOf('.')) != -1)
                    iniName = iniName.substring(0, pos);
            }
        }
        iniName += ".hfm";

        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择要解压到的路径");
        chooser.setInitialDirectory(new File(iniPath));
        chooser.setInitialFileName(iniName);
        File hfmFile = chooser.showSaveDialog(null);

        if(hfmFile == null)
            return;
        fileNum = -1;
        getChildrenNum(root);

        if(controller.progressNum == 0)
            progressWinIni();
        Huffman hf = new Huffman();
        HFMTask task = new HFMTask(hf, files, fileNum, hfmFile.getAbsolutePath(), controller);
        hf.setTask(task);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        removeAll();
    }

    private ProgressController controller = new ProgressController();

    @FXML
    void decompress(ActionEvent event) throws IOException
    {
        if(root.getChildren().isEmpty())
            return;
        String outPath = root.getChildren().get(0).getValue().getPath();
        String iniPath = outPath.substring(0, outPath.lastIndexOf(File.separator, outPath.length()-2));

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择要解压到的路径");
        chooser.setInitialDirectory(new File(iniPath));
        File saveFolder = chooser.showDialog(null);
        if(saveFolder == null)
            return;

        if(controller.progressNum == 0)
            progressWinIni();
        Huffman hf = new Huffman();
        HFMTask task = new HFMTask(hf, outPath, saveFolder.getAbsolutePath(), controller);
        hf.setTask(task);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        removeAll();
    }

    private Stage progressStage;

    void progressWinIni()
    {
        try
        {
            progressStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/ProgressWindow.fxml"));
            Parent winRoot = loader.load();
            progressStage.setTitle("HFMCoder-正在编码");
            progressStage.setScene(new Scene(winRoot));
            progressStage.setOnCloseRequest(e->{
                    controller.progressNum = 0;
                    controller.cancelTasks();
            });
            progressStage.getIcons().add(
                    new javafx.scene.image.Image(this.getClass().getResource("../res/graphics/icon.jpg").toString()));
            progressStage.show();
            controller = loader.getController();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    void display(ActionEvent event) {

    }

    @FXML
    void removeFile(ActionEvent event) {
        if(toDecompress)
        {
            if(root.getChildren().size() == 0)
                return;
            removeAll();
        }
        else
        {
            ObservableList<TreeItem> selected = treeTableView.getSelectionModel().getSelectedItems();
            if (selected != null) {
                ArrayList<TreeItem> rows = new ArrayList<>(selected);
                rows.forEach(row ->
                {
                    row.getParent().getChildren().remove(row);
                    if(row==null)
                        return;
                });
            }

        }
        if(root.getChildren().size() == 0)
        {
            toDecompress = false;
            remove_file_btn.setDisable(true);
            compress_btn.setDisable(true);
            decompress_btn.setDisable(true);
            add_file_btn.setDisable(false);
        }
    }

    void removeAll()
    {
        toDecompress = false;
        root.getChildren().removeAll(root.getChildren());
        remove_file_btn.setDisable(true);
        compress_btn.setDisable(true);
        decompress_btn.setDisable(true);
        add_file_btn.setDisable(false);
    }

    @FXML
    void onDragOver(DragEvent event)
    {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles())
        {
            event.acceptTransferModes(TransferMode.COPY);//接受拖入文件
        }
    }

    @FXML
    void onDragDropped(DragEvent event) {
        if(toDecompress)
            return;
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles())
        {
            if(!initialized)
                initialize();
            List<File> files = dragboard.getFiles();
            for(File file : files)
            {
                //避免读入重复的文件
                for(TreeItem<MyFile> other : root.getChildren())
                {
                    String str = file.getAbsolutePath();
                    if(file.isDirectory())
                        str += File.separator;
                    if(str.equals(other.getValue().getPath()))
                    {
                        files.remove(file);
                        break;
                    }
                }
            }
            if(!files.isEmpty())
            {
                if(root.getChildren().size() == 0)
                {
                    if(isHFMFile(files))
                        putHFMFile(files.get(0));
                    else
                        putFile(files, root);
                }
                else if(!toDecompress)
                {
                    compress_btn.setDisable(false);
                    decompress_btn.setDisable(true);
                    add_file_btn.setDisable(false);
                    remove_file_btn.setDisable(false);
                    putFile(files, root);
                }
            }
        }
    }

    @FXML
    void onMouseClicked(MouseEvent event)
    {
        treeTableView.getSelectionModel().clearSelection();
    }



}
