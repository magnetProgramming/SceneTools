module com.magnet.scenetools.scenetools {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.sql;


    opens com.magnet.scenetools.scenetools to javafx.fxml;
    exports com.magnet.scenetools.scenetools;
    exports com.magnet.scenetools.scenetools.Controllers;
    opens com.magnet.scenetools.scenetools.Controllers to javafx.fxml;
}