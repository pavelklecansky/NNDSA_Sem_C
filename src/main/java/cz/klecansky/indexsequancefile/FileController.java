package cz.klecansky.indexsequancefile;

import cz.klecansky.indexsequancefile.files.IndexSequenceFile;
import cz.klecansky.indexsequancefile.logging.LogManager;
import cz.klecansky.indexsequancefile.logging.Logger;
import cz.klecansky.indexsequancefile.utils.RecordGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static javafx.scene.control.Alert.*;

public class FileController implements Initializable {
    private static Logger logger = LogManager.getLogger();

    private static final String FILE_NAME = "data";

    @FXML
    public ListView<Integer> keyList;
    @FXML
    public ListView<String> blockLog;
    @FXML
    public Button listAllKeysButton;
    @FXML
    public Button findByKeyButton;
    @FXML
    public Label keyValue;


    private RecordGenerator recordGenerator;
    private IndexSequenceFile indexSequenceFile;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(event -> {
            // close the file if it is open
            if (indexSequenceFile != null) {
                try {
                    indexSequenceFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recordGenerator = new RecordGenerator();
    }

    @FXML
    public void build(ActionEvent actionEvent) throws IOException {
        indexSequenceFile = new IndexSequenceFile(FILE_NAME);
        indexSequenceFile.build(recordGenerator.generate(10000));
        activateButtonsAfterBuild();
    }

    @FXML
    public void findByKey(ActionEvent actionEvent) {
        Dialog<String> dialog = findByKeyDialog();
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            Integer key = Integer.valueOf(name);
            blockLog.getItems().clear();
            logger.getLogs().clear();
            try {
                String value = indexSequenceFile.find(key);
                blockLog.getItems().addAll(logger.getLogs());
                keyValue.setText(value);
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Key not find");
                alert.setContentText("Ooops, keys was not find");

                alert.showAndWait();
            }
        });
    }

    @FXML
    public void listAllKeys(ActionEvent actionEvent) throws IOException {
        keyList.getItems().clear();
        blockLog.getItems().clear();
        logger.getLogs().clear();
        List<Integer> integers = indexSequenceFile.listOfKeys();
        keyList.getItems().addAll(integers);
        blockLog.getItems().addAll(logger.getLogs());
    }

    private void activateButtonsAfterBuild() {
        listAllKeysButton.setDisable(false);
        findByKeyButton.setDisable(false);
    }

    private Dialog<String> findByKeyDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Find Key");
        dialog.setHeaderText("Find key dialog");
        dialog.setContentText("Enter key:");
        return dialog;
    }


}