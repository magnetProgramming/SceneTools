package com.magnet.scenetools.scenetools.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;


public class MediaInfoController implements Initializable
{

    @FXML
    public TextArea mediaInfoTextArea;

    @FXML
    public Label mediaInfoHeader;

    public void handleClose(ActionEvent actionEvent)
    {
        Stage mediaInfoDisplayStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        mediaInfoDisplayStage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

    }
}
