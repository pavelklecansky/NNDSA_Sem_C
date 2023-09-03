package cz.klecansky.indexsequancefile;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FileApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        FXMLLoader fxmlLoader = new FXMLLoader(FileApplication.class.getResource("file-view.fxml"));
        Parent root = fxmlLoader.load();
        FileController controller = fxmlLoader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root, 1000, 800);
        stage.setTitle("Indexed-sequential file");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}