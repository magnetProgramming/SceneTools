package com.magnet.scenetools.scenetools.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MediaInfoController implements Initializable
{
    private static final Pattern STRING_FIELD_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private static final Pattern NUMBER_FIELD_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*([\\d.]+)");

    private String mediaInfoRaw = "";

    @FXML
    public TextArea mediaInfoTextArea;

    @FXML
    public Label mediaInfoHeader;

    @FXML
    public VBox cleanInfoContainer;

    public void setMediaInfo(String fileName, String mediaInfo)
    {
        mediaInfoHeader.setText(fileName);
        mediaInfoRaw = mediaInfo == null ? "" : mediaInfo;
        mediaInfoTextArea.setText(mediaInfoRaw);
        populateCleanView();
    }

    public void handleClose(ActionEvent actionEvent)
    {
        Stage mediaInfoDisplayStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        mediaInfoDisplayStage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

    }

    private void populateCleanView()
    {
        cleanInfoContainer.getChildren().clear();

        if (mediaInfoRaw.isBlank())
        {
            cleanInfoContainer.getChildren().add(new Label("No media info was returned."));
            return;
        }

        String formatBlock = extractObjectBlock(mediaInfoRaw, "format");
        List<String> streamBlocks = extractArrayObjectBlocks(mediaInfoRaw, "streams");

        String videoBlock = findStreamByType(streamBlocks, "video");
        String audioBlock = findStreamByType(streamBlocks, "audio");
        List<String> subtitleBlocks = findStreamsByType(streamBlocks, "subtitle");

        cleanInfoContainer.getChildren().add(createSection("General",
                createInfoRow("Container", joinNonEmpty(
                        getStringValue(formatBlock, "format_long_name"),
                        getStringValue(formatBlock, "format_name"))),
                createInfoRow("Duration", formatDuration(getStringValue(formatBlock, "duration"))),
                createInfoRow("Size", formatBytes(getStringValue(formatBlock, "size"))),
                createInfoRow("Overall Bitrate", formatBitrate(getStringValue(formatBlock, "bit_rate"))),
                createInfoRow("Streams", String.valueOf(streamBlocks.size()))
        ));

        if (videoBlock != null)
        {
            cleanInfoContainer.getChildren().add(createSection("Video",
                    createInfoRow("Codec", joinNonEmpty(
                            getStringValue(videoBlock, "codec_long_name"),
                            getStringValue(videoBlock, "codec_name"))),
                    createInfoRow("Resolution", formatResolution(videoBlock)),
                    createInfoRow("Frame Rate", formatFrameRate(getStringValue(videoBlock, "r_frame_rate"))),
                    createInfoRow("Pixel Format", getStringValue(videoBlock, "pix_fmt")),
                    createInfoRow("Aspect Ratio", getStringValue(videoBlock, "display_aspect_ratio")),
                    createInfoRow("Bitrate", formatBitrate(getStringValue(videoBlock, "bit_rate")))
            ));
        }

        if (audioBlock != null)
        {
            cleanInfoContainer.getChildren().add(createSection("Audio",
                    createInfoRow("Codec", joinNonEmpty(
                            getStringValue(audioBlock, "codec_long_name"),
                            getStringValue(audioBlock, "codec_name"))),
                    createInfoRow("Channels", formatChannels(audioBlock)),
                    createInfoRow("Sample Rate", formatSampleRate(getStringValue(audioBlock, "sample_rate"))),
                    createInfoRow("Language", getNestedStringValue(audioBlock, "tags", "language")),
                    createInfoRow("Bitrate", formatBitrate(getStringValue(audioBlock, "bit_rate")))
            ));
        }

        if (!subtitleBlocks.isEmpty())
        {
            List<String> subtitleSummaries = new ArrayList<>();

            for (int i = 0; i < subtitleBlocks.size(); i++)
            {
                String subtitleBlock = subtitleBlocks.get(i);
                String language = getNestedStringValue(subtitleBlock, "tags", "language");
                String codec = joinNonEmpty(
                        getStringValue(subtitleBlock, "codec_long_name"),
                        getStringValue(subtitleBlock, "codec_name"));
                subtitleSummaries.add("Track " + (i + 1) + ": " + joinNonEmpty(language, codec));
            }

            cleanInfoContainer.getChildren().add(createSection("Subtitles",
                    createInfoRow("Tracks", String.valueOf(subtitleBlocks.size())),
                    createInfoRow("Details", String.join("\n", subtitleSummaries))
            ));
        }
    }

    private VBox createSection(String title, VBox... rows)
    {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox section = new VBox(8);
        section.setStyle("-fx-background-color: rgba(127,127,127,0.08); -fx-background-radius: 8; -fx-padding: 14;");
        section.getChildren().add(titleLabel);
        section.getChildren().addAll(rows);
        return section;
    }

    private VBox createInfoRow(String label, String value)
    {
        VBox row = new VBox(2);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Label valueNode = new Label(defaultIfBlank(value));
        valueNode.setWrapText(true);
        VBox.setVgrow(valueNode, Priority.NEVER);

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    private String formatResolution(String block)
    {
        String width = getNumericValue(block, "width");
        String height = getNumericValue(block, "height");

        if (width.isBlank() || height.isBlank())
        {
            return "";
        }

        return width + " x " + height;
    }

    private String formatChannels(String block)
    {
        String channels = getNumericValue(block, "channels");
        String layout = getStringValue(block, "channel_layout");
        return joinNonEmpty(channels.isBlank() ? "" : channels + " channels", layout);
    }

    private String formatDuration(String secondsText)
    {
        if (secondsText == null || secondsText.isBlank())
        {
            return "";
        }

        try
        {
            double totalSeconds = Double.parseDouble(secondsText);
            long roundedSeconds = Math.round(totalSeconds);
            long hours = roundedSeconds / 3600;
            long minutes = (roundedSeconds % 3600) / 60;
            long seconds = roundedSeconds % 60;

            if (hours > 0)
            {
                return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
            }

            return String.format(Locale.US, "%d:%02d", minutes, seconds);
        } catch (NumberFormatException ignored)
        {
            return secondsText;
        }
    }

    private String formatBytes(String bytesText)
    {
        if (bytesText == null || bytesText.isBlank())
        {
            return "";
        }

        try
        {
            double size = Double.parseDouble(bytesText);
            String[] units = {"B", "KB", "MB", "GB", "TB"};
            int unitIndex = 0;

            while (size >= 1024 && unitIndex < units.length - 1)
            {
                size /= 1024;
                unitIndex++;
            }

            return String.format(Locale.US, "%.2f %s", size, units[unitIndex]);
        } catch (NumberFormatException ignored)
        {
            return bytesText;
        }
    }

    private String formatBitrate(String bitrateText)
    {
        if (bitrateText == null || bitrateText.isBlank())
        {
            return "";
        }

        try
        {
            double bitrate = Double.parseDouble(bitrateText);
            if (bitrate >= 1_000_000)
            {
                return String.format(Locale.US, "%.2f Mbps", bitrate / 1_000_000d);
            }
            if (bitrate >= 1_000)
            {
                return String.format(Locale.US, "%.0f kbps", bitrate / 1_000d);
            }
            return String.format(Locale.US, "%.0f bps", bitrate);
        } catch (NumberFormatException ignored)
        {
            return bitrateText;
        }
    }

    private String formatSampleRate(String sampleRateText)
    {
        if (sampleRateText == null || sampleRateText.isBlank())
        {
            return "";
        }

        try
        {
            double sampleRate = Double.parseDouble(sampleRateText);
            return String.format(Locale.US, "%.1f kHz", sampleRate / 1000d);
        } catch (NumberFormatException ignored)
        {
            return sampleRateText;
        }
    }

    private String formatFrameRate(String frameRateText)
    {
        if (frameRateText == null || frameRateText.isBlank())
        {
            return "";
        }

        if (frameRateText.contains("/"))
        {
            String[] parts = frameRateText.split("/", 2);
            try
            {
                double numerator = Double.parseDouble(parts[0]);
                double denominator = Double.parseDouble(parts[1]);
                if (denominator != 0)
                {
                    return String.format(Locale.US, "%.3f fps", numerator / denominator);
                }
            } catch (NumberFormatException ignored)
            {
                return frameRateText;
            }
        }

        return frameRateText;
    }

    private String defaultIfBlank(String value)
    {
        return value == null || value.isBlank() ? "Not available" : value;
    }

    private String findStreamByType(List<String> streamBlocks, String type)
    {
        for (String streamBlock : streamBlocks)
        {
            if (type.equals(getStringValue(streamBlock, "codec_type")))
            {
                return streamBlock;
            }
        }

        return null;
    }

    private List<String> findStreamsByType(List<String> streamBlocks, String type)
    {
        List<String> matches = new ArrayList<>();

        for (String streamBlock : streamBlocks)
        {
            if (type.equals(getStringValue(streamBlock, "codec_type")))
            {
                matches.add(streamBlock);
            }
        }

        return matches;
    }

    private String getStringValue(String block, String fieldName)
    {
        return getPatternValue(block, Pattern.compile(String.format(STRING_FIELD_PATTERN.pattern(), Pattern.quote(fieldName)), Pattern.DOTALL));
    }

    private String getNumericValue(String block, String fieldName)
    {
        return getPatternValue(block, Pattern.compile(String.format(NUMBER_FIELD_PATTERN.pattern(), Pattern.quote(fieldName))));
    }

    private String getNestedStringValue(String block, String objectName, String fieldName)
    {
        String nestedBlock = extractObjectBlock(block, objectName);
        return getStringValue(nestedBlock, fieldName);
    }

    private String getPatternValue(String block, Pattern pattern)
    {
        if (block == null || block.isBlank())
        {
            return "";
        }

        Matcher matcher = pattern.matcher(block);
        if (matcher.find())
        {
            return matcher.group(1).replace("\\/", "/");
        }

        return "";
    }

    private String extractObjectBlock(String json, String objectName)
    {
        if (json == null || json.isBlank())
        {
            return "";
        }

        String marker = "\"" + objectName + "\"";
        int markerIndex = json.indexOf(marker);
        if (markerIndex < 0)
        {
            return "";
        }

        int objectStart = json.indexOf("{", markerIndex);
        if (objectStart < 0)
        {
            return "";
        }

        return extractBalancedBlock(json, objectStart, '{', '}');
    }

    private List<String> extractArrayObjectBlocks(String json, String arrayName)
    {
        List<String> objectBlocks = new ArrayList<>();
        if (json == null || json.isBlank())
        {
            return objectBlocks;
        }

        String marker = "\"" + arrayName + "\"";
        int markerIndex = json.indexOf(marker);
        if (markerIndex < 0)
        {
            return objectBlocks;
        }

        int arrayStart = json.indexOf("[", markerIndex);
        int arrayEnd = findMatchingIndex(json, arrayStart, '[', ']');

        if (arrayStart < 0 || arrayEnd < 0)
        {
            return objectBlocks;
        }

        int index = arrayStart;
        while (index < arrayEnd)
        {
            int objectStart = json.indexOf("{", index);
            if (objectStart < 0 || objectStart > arrayEnd)
            {
                break;
            }

            String objectBlock = extractBalancedBlock(json, objectStart, '{', '}');
            if (!objectBlock.isBlank())
            {
                objectBlocks.add(objectBlock);
                index = objectStart + objectBlock.length();
            } else
            {
                break;
            }
        }

        return objectBlocks;
    }

    private String extractBalancedBlock(String text, int startIndex, char openChar, char closeChar)
    {
        int endIndex = findMatchingIndex(text, startIndex, openChar, closeChar);
        if (startIndex < 0 || endIndex < 0)
        {
            return "";
        }

        return text.substring(startIndex, endIndex + 1);
    }

    private int findMatchingIndex(String text, int startIndex, char openChar, char closeChar)
    {
        if (text == null || startIndex < 0 || startIndex >= text.length())
        {
            return -1;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = startIndex; i < text.length(); i++)
        {
            char currentChar = text.charAt(i);

            if (escaped)
            {
                escaped = false;
                continue;
            }

            if (currentChar == '\\')
            {
                escaped = true;
                continue;
            }

            if (currentChar == '"')
            {
                inString = !inString;
                continue;
            }

            if (inString)
            {
                continue;
            }

            if (currentChar == openChar)
            {
                depth++;
            } else if (currentChar == closeChar)
            {
                depth--;
                if (depth == 0)
                {
                    return i;
                }
            }
        }

        return -1;
    }

    private String joinNonEmpty(String first, String second)
    {
        boolean hasFirst = first != null && !first.isBlank();
        boolean hasSecond = second != null && !second.isBlank();

        if (hasFirst && hasSecond)
        {
            if (first.equals(second))
            {
                return first;
            }
            return first + " (" + second + ")";
        }

        if (hasFirst)
        {
            return first;
        }

        return hasSecond ? second : "";
    }
}
