package com.magnet.scenetools.scenetools.Controllers;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import com.magnet.scenetools.scenetools.SceneTools;
import com.magnet.scenetools.scenetools.Utils.ParseMediaInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SceneToolsController implements Initializable {

    @FXML
    public Button themeToggleButton;

    private String selectedFileName = "Loading file name";

    public void onGenerateSceneName(ActionEvent actionEvent)
    {

    }

    public void onBatchRename(ActionEvent actionEvent)
    {

    }

    public void onFetchMediaInfo(ActionEvent actionEvent)
    {

        try
        {
            FileChooser fileChooserPrompt = new FileChooser();
            fileChooserPrompt.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Video Files", "*.mkv", "*.mp4")
            );
            File selectedMediaFile = fileChooserPrompt.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());

            if (selectedMediaFile != null)
            {
                // Parse Info
                try {
                    ParseMediaInfo mediaParser = new ParseMediaInfo();
                    String mediaInfo = mediaParser.parseMediaInfo(selectedMediaFile);

                    FXMLLoader loader = new FXMLLoader(SceneTools.class.getResource("DisplayMediaInfo-View.fxml"));
                    Parent root = loader.load();

                    MediaInfoController controller = loader.getController();
                    controller.mediaInfoTextArea.setText(mediaInfo);
                    selectedFileName = selectedMediaFile.getName();
                    controller.mediaInfoHeader.setText(selectedFileName);

                    Stage mediaInfoDisplayStage = new Stage();
                    mediaInfoDisplayStage.setTitle(STR."\{selectedMediaFile.getName()} Information");
                    mediaInfoDisplayStage.setScene(new Scene(root, 900, 900));
                    mediaInfoDisplayStage.show();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else
            {
                Alert noFileSelectedAlert = new Alert(Alert.AlertType.ERROR, "No media file selected. Please select a media file.", ButtonType.OK);
                noFileSelectedAlert.setHeaderText("ERROR!");
                noFileSelectedAlert.setTitle("ERROR!");
                noFileSelectedAlert.show();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void onAboutClicked(ActionEvent actionEvent)
    {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION, "Created by magnetProgramming 2025", ButtonType.OK);
        aboutAlert.setTitle("About");
        aboutAlert.setHeaderText("About SceneTools");
        aboutAlert.show();
    }

    public void onSettingsClicked(ActionEvent actionEvent)
    {

    }

    public void onQuitClicked(ActionEvent actionEvent)
    {
        Platform.exit();
    }

    public void onToggleTheme(ActionEvent actionEvent)
    {
        String currentTheme = themeToggleButton.getText();
        
        if (currentTheme.contains("☀") )
        {
            Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            themeToggleButton.setText("\uD83C\uDF19");
        } else if (currentTheme.contains("\uD83C\uDF19"))
        {
            Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
            themeToggleButton.setText("☀");
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        themeToggleButton.setText("☀");
    }
}