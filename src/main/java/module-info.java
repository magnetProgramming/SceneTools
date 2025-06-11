module com.magnet.scenetools.scenetools {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.magnet.scenetools.scenetools to javafx.fxml;
    exports com.magnet.scenetools.scenetools;
}