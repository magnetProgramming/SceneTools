package com.magnet.scenetools.scenetools.Controllers;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class SceneToolsController implements Initializable {

    @FXML
    public Button themeToggleButton;

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void onGenerateSceneName(ActionEvent actionEvent)
    {

    }

    public void onNormalizeFileNames(ActionEvent actionEvent)
    {

    }

    public void onBatchRename(ActionEvent actionEvent)
    {

    }

    public void onFetchMediaInfo(ActionEvent actionEvent)
    {

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