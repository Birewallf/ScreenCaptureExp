package bwg.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {
    public static final int DEFAULTPORTSERVER = 27000;
    public static final int DEFAULTPORTCLIENT = 27001;
    public static final int DEFAULTPORTMESSAGE = 27002;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            Parent root = FXMLLoader.load(this.getClass().getClassLoader().getResource("fxml/server_startapp.fxml"));
            primaryStage.setTitle("ScreenCapture");

            Scene scene = new Scene (root);
            primaryStage.setScene(scene);
            scene.getStylesheets().add(this.getClass().getClassLoader().getResource("style/css.css").toExternalForm());

            primaryStage.setOnCloseRequest(e ->  {
                Platform.exit();
                System.exit(0);
            });
            primaryStage.show();
        } catch (Exception ignore) {
            System.exit(1);
        }

    }
}
