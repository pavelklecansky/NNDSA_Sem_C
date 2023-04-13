module cz.klecansky.indexsequancefile {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.lang3;
            
                            
    opens cz.klecansky.indexsequancefile to javafx.fxml;
    exports cz.klecansky.indexsequancefile;
    exports cz.klecansky.indexsequancefile.files;
    opens cz.klecansky.indexsequancefile.files to javafx.fxml;
    exports cz.klecansky.indexsequancefile.blocks;
    opens cz.klecansky.indexsequancefile.blocks to javafx.fxml;
    exports cz.klecansky.indexsequancefile.records;
    opens cz.klecansky.indexsequancefile.records to javafx.fxml;
}