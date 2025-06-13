package com.magnet.scenetools.scenetools.Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.SQLOutput;

public class ParseMediaInfo
{
    public String parseMediaInfo(File mediaFile)
    {

        String mediaFilePath = mediaFile.getAbsolutePath();
        ProcessBuilder fetchMediaProcessBuild = new ProcessBuilder
                ("ffprobe", "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", mediaFilePath);

        try
        {
            Process fetchMediaProcess = fetchMediaProcessBuild.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(fetchMediaProcess.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null)
            {
                output.append(line).append("\n");
            }

            int exitCode = fetchMediaProcess.waitFor();
            if (exitCode == 0)
            {
                String jsonOutput = output.toString();
                System.out.println(jsonOutput);
                return jsonOutput;
            } else
            {
                System.out.println(STR."ffprobe exited with error code\{exitCode}");
            }

        } catch (Exception e)
        {
            Alert ffmpegNotInstalledAlert = new Alert(Alert.AlertType.ERROR, "You need ffmpeg to run this please install ffmpeg.", ButtonType.OK);
            ffmpegNotInstalledAlert.setTitle("ERROR!");
            ffmpegNotInstalledAlert.setHeaderText("ERROR!");
            ffmpegNotInstalledAlert.show();
            throw new RuntimeException(e);
        }
        return "";
    }
}
