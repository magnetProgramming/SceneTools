package com.magnet.scenetools.scenetools.Controllers;

import com.magnet.scenetools.scenetools.SceneTools;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.HashMap;

public class ScreenController {
    private HashMap<String, Pane> screenMap = new HashMap<>();
    public Scene MenuScene;

    public ScreenController(Scene MenuScene)
    {
        this.MenuScene = MenuScene;
    }

    public void addScreen(String name, Pane pane)
    {
        screenMap.put(name, pane);
    }

    public void removeScreen(String name)
    {
        screenMap.remove(name);
    }

    public void Activate(String name) {
        Pane pane = screenMap.get(name);
        if (pane != null) {
            SceneTools.MenuScene.setRoot(pane);
        } else {
            // Handle the case where the screen isn't found
            System.out.println("Screen " + name + " not found.");
        }
    }

}