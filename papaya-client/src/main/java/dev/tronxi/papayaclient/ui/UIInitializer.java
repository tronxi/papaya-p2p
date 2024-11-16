package dev.tronxi.papayaclient.ui;

import dev.tronxi.papayaclient.PapayaClientApplication;
import dev.tronxi.papayaclient.persistence.FileManager;
import dev.tronxi.papayaclient.persistence.papayafile.PapayaFile;
import dev.tronxi.papayaclient.peer.PeerConnectionManager;
import dev.tronxi.papayaclient.peer.PeerConnectionManagerTCP;
import dev.tronxi.papayaclient.ui.components.CreateFileChooserButton;
import dev.tronxi.papayaclient.ui.components.PapayaProgress;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;

@Service
public class UIInitializer extends Application {

    private FileManager fileManager;
    private PeerConnectionManager peerConnectionManager;

    @Override
    public void init() {
        fileManager = PapayaClientApplication.getContext().getBean(FileManager.class);
        peerConnectionManager = PapayaClientApplication.getContext().getBean(PeerConnectionManagerTCP.class);
    }

    @Override
    public void start(Stage stage) {
        Label createPapayaFileRunning = new Label("CreatePapayaFileRunning...");
        createPapayaFileRunning.managedProperty().bind(createPapayaFileRunning.visibleProperty());
        createPapayaFileRunning.setVisible(false);
        Button createPapayaFileButton = generateCreatePapayaFileButton(stage, createPapayaFileRunning);

        Button downloadButton = generateDownloadButton(stage);

        HBox buttonsBox = new HBox(createPapayaFileButton, downloadButton, createPapayaFileRunning);
        buttonsBox.setPadding(new Insets(10));
        buttonsBox.setSpacing(10);

        TextArea logs = new TextArea();
        logs.setMaxHeight(500);
        logs.setEditable(false);
        logs.setText("");

        peerConnectionManager.start(logs);
        peerConnectionManager.startAllIncompleteDownloads();

        VBox papayaProgressVBox = new VBox();
        fileManager.setNewPapayaStatusFileFunction((papayaStatusFile -> {
            Platform.runLater(() -> {
                PapayaProgress papayaProgress = new PapayaProgress();
                fileManager.addUpdateFunction(papayaStatusFile.getFileId(), papayaProgress::refresh);
                papayaProgressVBox.getChildren().add(papayaProgress.create(getHostServices(), fileManager, papayaStatusFile));
            });
            return null;
        }));
        fileManager.setDeletedPapayaStatusFileFunction(() -> retrieveAllPapayaStatus(papayaProgressVBox));
        retrieveAllPapayaStatus(papayaProgressVBox);
        ScrollPane progressScrollPane = new ScrollPane(papayaProgressVBox);
        progressScrollPane.setFitToWidth(true);

        ScrollPane logsScrollPane = new ScrollPane(logs);
        logsScrollPane.setFitToWidth(true);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(progressScrollPane, logs);
        splitPane.setDividerPositions(0.8);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(buttonsBox);
        borderPane.setCenter(splitPane);

        Scene scene = new Scene(borderPane, 900, 580);
        stage.setTitle("Papaya");
        stage.setScene(scene);
        stage.show();
    }

    private void retrieveAllPapayaStatus(VBox papayaProgressVBox) {
        papayaProgressVBox.getChildren().clear();
        fileManager.findAll().forEach(papayaStatusFile -> {
            PapayaProgress papayaProgress = new PapayaProgress();
            fileManager.addUpdateFunction(papayaStatusFile.getFileId(), papayaProgress::refresh);
            papayaProgressVBox.getChildren().add(papayaProgress.create(getHostServices(), fileManager, papayaStatusFile));
        });
    }


    private Button generateDownloadButton(Stage stage) {
        return new CreateFileChooserButton().create("Download", stage, file -> {
            Optional<PapayaFile> maybePapayaFile = fileManager.retrievePapayaFileFromFile(file);
            maybePapayaFile.ifPresent(papayaFile -> {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        peerConnectionManager.download(papayaFile);
                        return null;
                    }
                };
                new Thread(task).start();
            });
        });
    }

    private Button generateCreatePapayaFileButton(Stage stage, Label label) {
        return new CreateFileChooserButton().
                create("Create", stage, selectedFile -> {
                    if (selectedFile != null) {
                        Task<Optional<Path>> task = new Task<>() {
                            @Override
                            protected Optional<Path> call() {
                                return fileManager.split(selectedFile);
                            }
                        };
                        task.setOnRunning(workerStateEvent -> {
                            label.setVisible(true);
                        });
                        task.setOnSucceeded(workerStateEvent -> {
                            label.setVisible(false);
                            Optional<Path> maybePath = task.getValue();
                            Alert alert;
                            if (maybePath.isPresent()) {
                                alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Papaya File Created");
                                alert.setHeaderText(maybePath.get().toAbsolutePath().toString());
                            } else {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Papaya File Creation Failed");
                            }
                            alert.show();
                        });
                        new Thread(task).start();
                    }
                });
    }

    @Override
    public void stop() {
        peerConnectionManager.stop();
        SpringApplication.exit(PapayaClientApplication.getContext(), () -> 0);
    }
}
