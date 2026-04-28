package com.magnet.scenetools.scenetools.Controllers;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.magnet.scenetools.scenetools.SceneTools;
import com.magnet.scenetools.scenetools.Utils.GenerateSceneName;
import com.magnet.scenetools.scenetools.Utils.ParseMediaInfo;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneToolsController implements Initializable {

	@FXML
	public Button themeToggleButton;

	@FXML
	public ComboBox<String> contentTypeBox;

	@FXML
	public TextField titleField;

	@FXML
	public TextField yearField;

	@FXML
	public HBox yearRow;

	@FXML
	public TextField seasonField;

	@FXML
	public TextField startEpisodeField;

	@FXML
	public ComboBox<String> resolutionBox;

	@FXML
	public ComboBox<String> codecBox;

	@FXML
	public ComboBox<String> audioBox;

	@FXML
	public ComboBox<String> ripTypeBox;

	@FXML
	public TextField streamingServiceField;

	@FXML
	public TextField groupField;

	@FXML
	public TextArea outputArea;

	@FXML
	public HBox seasonEpisodeRow;

	@FXML
	public Button batchRenameButton;

	@FXML
	public Button fetchMediaInfoButton;

	@FXML
	public ProgressBar taskProgressBar;

	@FXML
	public Label progressLabel;

	@FXML
	public Button clearOutputButton;

	@FXML
	public CheckBox openDirectoryAfterRenameCheckBox;

	@FXML
	public HBox openDirectoryRow;

	public void onGenerateSceneName(ActionEvent actionEvent) {
		List<String> missingFields = getMissingSceneNameFields();
		if (!missingFields.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Scene Name Incomplete",
					"Please fill in all required fields before generating a scene name.\n\nMissing: "
							+ String.join(", ", missingFields),
					"Missing Required Fields");
			outputArea.clear();
			return;
		}

		if (!hasValidEpisodeFields()) {
			return;
		}

		try {
			outputArea.setText(buildPreviewSceneName());
		} catch (Exception e) {
			showAlert(Alert.AlertType.ERROR, "ERROR!", "An error occurred while generating the scene name.", "ERROR!");
			e.printStackTrace();
		}
	}

	public void onClearOutput(ActionEvent actionEvent) {
		outputArea.clear();
	}

	public void onBatchRename(ActionEvent actionEvent) {
		if (isMovieMode()) {
			showAlert(Alert.AlertType.WARNING, "Batch Rename Unavailable",
					"Batch rename is only available when Content Type is set to Show.", "Show Mode Required");
			return;
		}

		List<String> missingFields = getMissingSceneNameFields();
		if (!missingFields.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Batch Rename Incomplete",
					"Please fill in all required fields before batch renaming.\n\nMissing: "
							+ String.join(", ", missingFields),
					"Missing Required Fields");
			return;
		}

		if (!hasValidEpisodeFields()) {
			return;
		}

		FileChooser fileChooserPrompt = new FileChooser();
		fileChooserPrompt.setTitle("Select Episodes In Order");
		fileChooserPrompt.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Video Files", "*.mkv", "*.mp4", "*.avi", "*.mov"));

		List<File> selectedFiles = fileChooserPrompt
				.showOpenMultipleDialog(((Node) actionEvent.getSource()).getScene().getWindow());

		if (selectedFiles == null || selectedFiles.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Batch Rename Cancelled",
					"No files selected. Choose the episodes in the order you want them renamed.", "No Files Selected");
			return;
		}

		int startingEpisode;
		try {
			startingEpisode = getStartingEpisodeNumber();
		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.WARNING, "Invalid Episode Number",
					"Start Episode must be a whole number.", "Invalid Start Episode");
			return;
		}

		Task<List<String>> renameTask = new Task<>() {
			@Override
			protected List<String> call() throws Exception {
				int totalSteps = selectedFiles.size() * 2;
				updateProgress(0, totalSteps);
				updateMessage("Preparing rename plan...");

				List<Path> targetPaths = new ArrayList<>();
				List<String> previewNames = new ArrayList<>();

				for (int i = 0; i < selectedFiles.size(); i++) {
					File sourceFile = selectedFiles.get(i);
					int episodeNumber = startingEpisode + i;
					String targetFileName = buildSceneName(episodeNumber) + getFileExtension(sourceFile.getName());
					Path targetPath = sourceFile.toPath().resolveSibling(targetFileName);

					if (!sourceFile.toPath().equals(targetPath) && Files.exists(targetPath)) {
						throw new IllegalStateException(
								"Cannot rename because this file already exists: " + targetFileName);
					}

					targetPaths.add(targetPath);
					previewNames.add(targetFileName);
					updateProgress(i + 1, totalSteps);
					updateMessage("Planned " + (i + 1) + " of " + selectedFiles.size());
				}

				for (int i = 0; i < selectedFiles.size(); i++) {
					updateMessage("Renaming " + (i + 1) + " of " + selectedFiles.size());
					Files.move(selectedFiles.get(i).toPath(), targetPaths.get(i));
					updateProgress(selectedFiles.size() + i + 1, totalSteps);
				}

				updateMessage("Batch rename complete.");
				return previewNames;
			}
		};

		renameTask.setOnSucceeded(event -> {
			outputArea.setText(String.join("\n", renameTask.getValue()));
			if (openDirectoryAfterRenameCheckBox.isSelected()) {
				try {
					openDirectory(selectedFiles.getFirst().toPath().getParent());
				} catch (Exception e) {
					showAlert(Alert.AlertType.WARNING, "Open Directory Failed",
							"Batch rename finished, but the output directory could not be opened.",
							"Directory Open Failed");
				}
			}
			completeTaskState("Batch rename complete: " + selectedFiles.size() + " file(s).");
		});

		renameTask.setOnFailed(event -> {
			showAlert(Alert.AlertType.ERROR, "ERROR!", renameTask.getException().getMessage(), "Rename Stopped");
			clearTaskState();
		});

		runTask(renameTask, "batch-rename-task");
	}

	public void onFetchMediaInfo(ActionEvent actionEvent) {
		FileChooser fileChooserPrompt = new FileChooser();
		fileChooserPrompt.setTitle("Select Media File");
		fileChooserPrompt.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("Video Files", "*.mkv", "*.mp4", "*.avi", "*.mov"));
		File selectedMediaFile = fileChooserPrompt
				.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());

		if (selectedMediaFile == null) {
			showAlert(Alert.AlertType.ERROR, "ERROR!", "No media file selected. Please select a media file.", "ERROR!");
			return;
		}

		Task<String> mediaInfoTask = new Task<>() {
			@Override
			protected String call() {
				updateProgress(-1, 1);
				updateMessage("Reading media info...");
				ParseMediaInfo mediaParser = new ParseMediaInfo();
				return mediaParser.parseMediaInfo(selectedMediaFile);
			}
		};

		mediaInfoTask.setOnSucceeded(event -> {
			try {
				FXMLLoader loader = new FXMLLoader(SceneTools.class.getResource("DisplayMediaInfo-View.fxml"));
				Parent root = loader.load();

				MediaInfoController controller = loader.getController();
				controller.setMediaInfo(selectedMediaFile.getName(), mediaInfoTask.getValue());

				Stage mediaInfoDisplayStage = new Stage();
				mediaInfoDisplayStage.setTitle(selectedMediaFile.getName() + " Information");
				mediaInfoDisplayStage.setScene(new Scene(root, 900, 900));
				mediaInfoDisplayStage.show();
			} catch (Exception e) {
				showAlert(Alert.AlertType.ERROR, "ERROR!", "Failed to open the Media Info window.", "ERROR!");
			}
			completeTaskState("Media info loaded.");
		});

		mediaInfoTask.setOnFailed(event -> {
			String message = mediaInfoTask.getException() == null ? "Failed to fetch media info."
					: mediaInfoTask.getException().getMessage();
			showAlert(Alert.AlertType.ERROR, "ERROR!", message, "ERROR!");
			clearTaskState();
		});

		runTask(mediaInfoTask, "media-info-task");
	}

	public void onAboutClicked(ActionEvent actionEvent) {
		showAlert(Alert.AlertType.INFORMATION, "About", "Created by magnetProgramming 2025", "About SceneTools");
	}

	public void onQuitClicked(ActionEvent actionEvent) {
		System.exit(0);
	}

	public void onToggleTheme(ActionEvent actionEvent) {
		String currentTheme = themeToggleButton.getText();

		if (currentTheme.contains("☀")) {
			Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
			themeToggleButton.setText("\uD83C\uDF19");
		} else if (currentTheme.contains("\uD83C\uDF19")) {
			Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
			themeToggleButton.setText("☀");
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		themeToggleButton.setText("☀");
		contentTypeBox.getItems().addAll("Movie", "Show");
		contentTypeBox.setValue("Movie");
		progressLabel.setText("");
		taskProgressBar.setVisible(false);
		taskProgressBar.setManaged(false);
		contentTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> updateContentTypeState());
		updateContentTypeState();
	}

	private void updateContentTypeState() {
		boolean showMode = !isMovieMode();
		yearRow.setVisible(!showMode);
		yearRow.setManaged(!showMode);
		seasonEpisodeRow.setVisible(showMode);
		seasonEpisodeRow.setManaged(showMode);
		batchRenameButton.setVisible(showMode);
		batchRenameButton.setManaged(showMode);
		openDirectoryRow.setVisible(showMode);
		openDirectoryRow.setManaged(showMode);
	}

	private String buildSceneName(int episodeNumber) {
		if (isMovieMode()) {
			return GenerateSceneName.generateMovieSceneName(titleField.getText(), yearField.getText(),
					resolutionBox.getValue(), streamingServiceField.getText(), ripTypeBox.getValue(),
					audioBox.getValue(),
					codecBox.getValue(), groupField.getText());
		}

		return GenerateSceneName.generateShowSceneName(titleField.getText(), seasonField.getText(), episodeNumber,
				resolutionBox.getValue(), streamingServiceField.getText(), ripTypeBox.getValue(), audioBox.getValue(),
				codecBox.getValue(), groupField.getText());
	}

	private String buildPreviewSceneName() {
		if (isMovieMode()) {
			return buildSceneName(0);
		}

		return buildSceneName(getStartingEpisodeNumber());
	}

	private int getStartingEpisodeNumber() {
		return Integer.parseInt(startEpisodeField.getText().trim());
	}

	private boolean isMovieMode() {
		return "Movie".equalsIgnoreCase(contentTypeBox.getValue());
	}

	private List<String> getMissingSceneNameFields() {
		List<String> missingFields = new ArrayList<>();

		if (contentTypeBox.getValue() == null) {
			missingFields.add("Content Type");
		}
		if (isBlank(titleField.getText())) {
			missingFields.add("Title");
		}
		if (isMovieMode()) {
			if (isBlank(yearField.getText())) {
				missingFields.add("Year");
			}
		} else {
			if (isBlank(seasonField.getText())) {
				missingFields.add("Season");
			}
			if (isBlank(startEpisodeField.getText())) {
				missingFields.add("Start Episode");
			}
		}
		if (resolutionBox.getValue() == null) {
			missingFields.add("Resolution");
		}
		if (codecBox.getValue() == null) {
			missingFields.add("Codec");
		}
		if (audioBox.getValue() == null) {
			missingFields.add("Audio");
		}
		if (ripTypeBox.getValue() == null) {
			missingFields.add("Rip Type");
		}
		if (isBlank(groupField.getText())) {
			missingFields.add("Group Name");
		}

		return missingFields;
	}

	private boolean hasValidEpisodeFields() {
		if (isMovieMode()) {
			return true;
		}

		if (!isPositiveInteger(seasonField.getText())) {
			showAlert(Alert.AlertType.WARNING, "Invalid Season", "Season must be a whole number.", "Invalid Season");
			return false;
		}

		if (!isPositiveInteger(startEpisodeField.getText())) {
			showAlert(Alert.AlertType.WARNING, "Invalid Start Episode",
					"Start Episode must be a whole number.", "Invalid Start Episode");
			return false;
		}

		return true;
	}

	private void runTask(Task<?> task, String threadName) {
		taskProgressBar.progressProperty().unbind();
		progressLabel.textProperty().unbind();
		taskProgressBar.progressProperty().bind(task.progressProperty());
		progressLabel.textProperty().bind(task.messageProperty());
		taskProgressBar.setVisible(true);
		taskProgressBar.setManaged(true);
		setActionsDisabled(true);

		Thread workerThread = new Thread(task);
		workerThread.setName(threadName);
		workerThread.setDaemon(true);
		workerThread.start();
	}

	private void clearTaskState() {
		taskProgressBar.progressProperty().unbind();
		progressLabel.textProperty().unbind();
		taskProgressBar.setProgress(0);
		taskProgressBar.setVisible(false);
		taskProgressBar.setManaged(false);
		progressLabel.setText("");
		setActionsDisabled(false);
	}

	private void completeTaskState(String message) {
		taskProgressBar.progressProperty().unbind();
		progressLabel.textProperty().unbind();
		taskProgressBar.setProgress(1.0);
		taskProgressBar.setVisible(true);
		taskProgressBar.setManaged(true);
		progressLabel.setText(message);
		setActionsDisabled(false);

		PauseTransition hideProgressDelay = new PauseTransition(Duration.seconds(3));
		hideProgressDelay.setOnFinished(event -> clearTaskState());
		hideProgressDelay.play();
	}

	private void setActionsDisabled(boolean disabled) {
		batchRenameButton.setDisable(disabled);
		fetchMediaInfoButton.setDisable(disabled);
		themeToggleButton.setDisable(disabled);
		contentTypeBox.setDisable(disabled);
	}

	private void showAlert(Alert.AlertType alertType, String title, String message, String headerText) {
		Alert alert = new Alert(alertType, message, ButtonType.OK);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.show();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private boolean isPositiveInteger(String value) {
		if (isBlank(value)) {
			return false;
		}

		try {
			return Integer.parseInt(value.trim()) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private String getFileExtension(String fileName) {
		int extensionIndex = fileName.lastIndexOf('.');
		if (extensionIndex < 0) {
			return "";
		}

		return fileName.substring(extensionIndex);
	}

	private void openDirectory(Path directoryPath) throws Exception {
		if (directoryPath == null) {
			return;
		}

		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().open(directoryPath.toFile());
			return;
		}

		throw new IllegalStateException("Desktop operations are not supported.");
	}
}
