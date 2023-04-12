module cz.klecansky.indexsequancefile {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.lang3;
            
                            
    opens cz.klecansky.indexsequancefile to javafx.fxml;
    exports cz.klecansky.indexsequancefile;
}