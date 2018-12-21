package bwg.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Client App Starter
 * @author ssvs
 */
public class ClientApp extends Application {
    public static final int DEFAULT_MANAGEMENT_PORT = 27000;
    public static final int DEFAULTPORTCLIENT = 27001;
    public static final int DEFAULTPORTMESSAGE = 27002;

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            Parent root = FXMLLoader.load(this.getClass().getClassLoader().getResource("fxml/client_startapp.fxml"));
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


    public static void main(String[] args) {
        launch(args);
    }
}