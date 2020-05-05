import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Compressor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("view/Compressor.fxml"));
        primaryStage.setTitle("HFMCoder");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(
                new Image(this.getClass().getResource("res/graphics/icon.jpg").toString()));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
