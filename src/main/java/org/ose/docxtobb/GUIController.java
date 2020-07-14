/*
 * DocxToBB converts specially formatted .doc(x) files to Blackboard Learn
 * test packages.
 * 
 * Copyright (C) 2020  Daniel J. Resch, Ph.D.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.ose.docxtobb;

import java.io.File;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.application.Platform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;

import org.ose.docxtobb.dialogs.AboutDialog;
import org.ose.docxtobb.dialogs.CreditsDialog;

import org.ose.docxtobb.logging.TextStackAppender;

public class GUIController {
    @FXML
    private MenuItem miQuit;

    @FXML
    private MenuItem miAbout;

    @FXML
    private MenuItem miCredits;

    @FXML
    private Button btnReset;

    @FXML
    private Button btnConvert;

    @FXML
    private Button btnDirDialog;

    @FXML
    private Button btnFileDialog;

    @FXML
    private TextArea taDirections;

    @FXML
    private TextArea taDescription;

    @FXML
    private TextField txtTitle;

    @FXML
    private TextField txtPoints;

    @FXML
    private TextField txtInputFile;

    @FXML
    private Accordion acInputPanes;

    @FXML
    private TitledPane tpGeneral;

    @FXML
    private TextField txtOutputDir;

    @FXML
    private TextField txtTotalQuestions;

    @FXML
    private CheckBox chkRemoveQNums;

    @FXML
    private AnchorPane apLogOutputBox;

    @FXML
    private ScrollPane spLogScroller;

    @FXML
    private ChoiceBox<String> cbxRandomResp;

    @FXML
    private ChoiceBox<String> cbxInlineQImgs;

    @FXML
    private ChoiceBox<String> cbxInlineRImgs;

    private Path pDocxFile;
    private Path pOutputDir;
    private Path pOutputFile;

    private final VBox vbLogStack;
    private final Stage stMainStage;
    private final ImageView ivWordIcon;
    private final ImageView ivFolderIcon;

    public GUIController(Stage mainStage) {
        stMainStage = mainStage;
        vbLogStack = new VBox();
        Common.configureTextStackLogger();
        TextStackAppender.setStackBox(vbLogStack);

        ivWordIcon = new ImageView(new Image(getClass().getResourceAsStream("/msword.png")));
        ivFolderIcon = new ImageView(new Image(getClass().getResourceAsStream("/folder.png")));
    }

    private void fixTextAreaBlurriness(TextArea textArea) {
        ScrollPane spTextScroller = (ScrollPane) textArea.getChildrenUnmodifiable().get(0);
        spTextScroller.setCache(false);

        for (Node n : spTextScroller.getChildrenUnmodifiable()) {
            n.setCache(false);
        }
    }

    private boolean pathsExist(String inputFile, String outputDir) {
        String sInputFile = inputFile.trim();
        String sOutputDir = outputDir.trim();

        if (!sInputFile.endsWith(".docx") && !sInputFile.endsWith(".doc")) {
            return false;
        }

        if (sOutputDir.length() < 1) {
            return false;
        }

        pDocxFile = Paths.get(inputFile);
        pOutputDir = Paths.get(outputDir);

        String sFilebase = pDocxFile.getFileName().toString().split("\\.")[0];
        pOutputFile = pOutputDir.resolve(sFilebase + ".zip");

        if (!Files.exists(pDocxFile)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("FileSystem Error");
            alert.setContentText("The selected Word file no longer exists!");
            alert.showAndWait();
            return false;
        } 

        if (!Files.exists(pOutputDir)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("FileSystem Error");
            alert.setContentText("The output directory no longer exists!");
            alert.showAndWait();
            return false;
        } 

        if (Files.exists(pOutputFile)) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("File Conflict");
            alert.setHeaderText("Overwrite Existing File");
            alert.setContentText("Do you want to overwrite existing Blackboard package?");
            Optional<ButtonType> opOverwriteResult = alert.showAndWait();

            if (opOverwriteResult.isPresent() && (opOverwriteResult.get() == ButtonType.OK)) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    @FXML
    public void initialize() {
        apLogOutputBox.getChildren().add(vbLogStack);
        AnchorPane.setTopAnchor(vbLogStack, 0.0);
        AnchorPane.setLeftAnchor(vbLogStack, 0.0);
        AnchorPane.setRightAnchor(vbLogStack, 0.0);
        AnchorPane.setBottomAnchor(vbLogStack, 0.0);

        btnFileDialog.setText("");
        btnFileDialog.setGraphic(ivWordIcon);
        btnFileDialog.setOnAction(event -> {
            String sFilePath = txtInputFile.getText().trim();
            FileChooser fcFileChooserDialog = new FileChooser();
            fcFileChooserDialog.setTitle("Select Word File...");
            fcFileChooserDialog.getExtensionFilters().addAll(
                    new ExtensionFilter("Word File", "*.docx"),
                    new ExtensionFilter("Word File", "*.doc"));

            if (sFilePath.length() > 0) {
                Path pFile = Path.of(sFilePath);

                if (Files.exists(pFile)) {
                    Path pFileDir = pFile.getParent();

                    if (pFileDir != null) {
                        fcFileChooserDialog.setInitialDirectory(pFileDir.toFile());
                    } else {
                        fcFileChooserDialog.setInitialDirectory(pFile.toFile());
                    }
                } else {
                    fcFileChooserDialog.setInitialDirectory(new File(System.getProperty("user.home")));
                }
            } else {
                fcFileChooserDialog.setInitialDirectory(new File(System.getProperty("user.home")));
            }

            File fiSelectedFile = fcFileChooserDialog.showOpenDialog(stMainStage);

            if (fiSelectedFile != null) {
                txtInputFile.setText(fiSelectedFile.getAbsolutePath());

                if (txtOutputDir.getText().length() == 0) {
                    txtOutputDir.setText(fiSelectedFile.getParent());
                }
            }
        });

        btnDirDialog.setText("");
        btnDirDialog.setGraphic(ivFolderIcon);
        btnDirDialog.setOnAction(event -> {
            String sDirOutPath = txtOutputDir.getText().trim();
            DirectoryChooser dcDirChooserDialog = new DirectoryChooser();
            dcDirChooserDialog.setTitle("Select Output Directory...");

            if (sDirOutPath.length() > 0) {
                Path pDir = Path.of(sDirOutPath);

                if (Files.exists(pDir)) {
                    dcDirChooserDialog.setInitialDirectory(pDir.toFile());
                } else {
                    dcDirChooserDialog.setInitialDirectory(new File(System.getProperty("user.home")));
                }
            } else {
                dcDirChooserDialog.setInitialDirectory(new File(System.getProperty("user.home")));
            }

            File fiSelectedDir = dcDirChooserDialog.showDialog(stMainStage);

            if (fiSelectedDir != null) {
                txtOutputDir.setText(fiSelectedDir.getAbsolutePath());
            }
        });

        btnConvert.setOnAction(event -> {
            if (pathsExist(txtInputFile.getText(), txtOutputDir.getText())) {
                Map<String, Boolean> mAdvOps = new HashMap<>();
                mAdvOps.put("INLINE_QIMGS", (cbxInlineQImgs.getValue() == "Yes"));
                mAdvOps.put("INLINE_RIMGS", (cbxInlineRImgs.getValue() == "Yes"));
                mAdvOps.put("RANDOM_RESPS", (cbxRandomResp.getValue() == "Yes"));

                AssessmentPacker apConverterTool = new AssessmentPacker();
                apConverterTool.setExamHeadingText(taDescription.getText(), taDirections.getText());
                apConverterTool.setQuestionCount(txtTotalQuestions.getText());
                apConverterTool.setPointTotal(txtPoints.getText());
                apConverterTool.setExamTitle(txtTitle.getText());
                apConverterTool.setAdvancedOptions(mAdvOps);

                if (chkRemoveQNums.isSelected()) {
                    apConverterTool.removeQuestionNumbering(true);
                }

                apConverterTool.loadFromFile(pDocxFile, pOutputFile);
                apConverterTool.cleanup();
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid Input");
                alert.setContentText("Please make sure to choose a valid .docx or .doc file!");
                alert.showAndWait();
            }
        });

        btnReset.setOnAction(event -> {
            Platform.runLater(() -> {
                txtTitle.setText("");
                txtPoints.setText("");
                txtInputFile.setText("");
                txtOutputDir.setText("");
                txtTotalQuestions.setText("");

                taDirections.setText("");
                taDescription.setText("");

                vbLogStack.getChildren().clear();
                chkRemoveQNums.setSelected(false);
            });
        });

        miQuit.setOnAction(event -> stMainStage.close());

        miAbout.setOnAction(event -> {
            AboutDialog adAboutDialogInfo = new AboutDialog();
            adAboutDialogInfo.showAndWait();
        });

        miCredits.setOnAction(event -> {
            CreditsDialog cdCreditsDialogInfo = new CreditsDialog();
            cdCreditsDialogInfo.showAndWait();
        });

        txtPoints.setTextFormatter(new TextFormatter<String>((change) -> {
            if (change.isAdded() || change.isReplaced()) {
                if (!change.getControlNewText().matches("[0-9]+")) {
                    return null;
                } else {
                    return change;
                }
            } else {
                return change;
            }
        }));

        txtTotalQuestions.setTextFormatter(new TextFormatter<String>((change) -> {
            if (change.isAdded() || change.isReplaced()) {
                if (!change.getControlNewText().matches("[0-9]+")) {
                    return null;
                } else {
                    return change;
                }
            } else {
                return change;
            }
        }));

        Platform.runLater(() -> {
            fixTextAreaBlurriness(taDirections);
            fixTextAreaBlurriness(taDescription);
        });

        acInputPanes.setExpandedPane(tpGeneral);
    }

    public void shutdown() {

    }
}