package com.magnet.scenetools.scenetools.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


public class MediaInfoController
{

    @FXML
    public TextArea mediaInfoTextArea;

    public void handleClose(ActionEvent actionEvent)
    {
        Stage mediaInfoDisplayStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        mediaInfoDisplayStage.close();
    }
}
