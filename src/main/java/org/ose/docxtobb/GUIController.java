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
import javafx.fxml.FXML;
import java.util.Optional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ScrollPane;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.ose.docxtobb.dialogs.AboutDialog;
import org.ose.docxtobb.dialogs.CreditsDialog;

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
    private TextArea taDescription;

    @FXML
    private TextArea taInstructions;

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

    private Path pDocxFile;
    private Path pOutputDir;
    private Path pOutputFile;

    private final Stage stMainStage;
    private final ImageView ivWordIcon;
    private final ImageView ivFolderIcon;

    private static final Logger lMainLogger = LogManager.getLogger(GUIController.class.getName());

    public GUIController(Stage mainStage) {
        stMainStage = mainStage;
        ivWordIcon = new ImageView(new Image(getClass().getResourceAsStream("/msword.png")));
        ivFolderIcon = new ImageView(new Image(getClass().getResourceAsStream("/folder.png")));
    }

    private void displayLogResults() {
        
    }

    private boolean pathsExist(String inputFile, String outputDir) {
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
                AssessmentPacker apConverterTool = new AssessmentPacker();
                apConverterTool.setExamHeadingText(taDescription.getText(), taInstructions.getText());
                apConverterTool.setQuestionCount(txtTotalQuestions.getText());
                apConverterTool.setPointTotal(txtPoints.getText());
                apConverterTool.setExamTitle(txtTitle.getText());

                if (chkRemoveQNums.isSelected()) {
                    apConverterTool.removeQuestionNumbering(true);
                }

                apConverterTool.loadFromFile(pDocxFile, pOutputFile);
                apConverterTool.cleanup();
            }
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

        acInputPanes.setExpandedPane(tpGeneral);
    }

    public void shutdown() {

    }
}