package com.magnet.scenetools.scenetools;

import atlantafx.base.theme.CupertinoDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class SceneTools extends Application {

    public static Scene MenuScene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SceneTools.class.getResource("SceneToolsMenu-View.fxml"));
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
        MenuScene = new Scene(fxmlLoader.load(), 820, 640);
        stage.setTitle("SceneTools");
        stage.setScene(MenuScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
