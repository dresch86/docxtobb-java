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
package org.ose.docxtobb.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.application.Platform;

import javafx.scene.Cursor;
import javafx.scene.layout.VBox;

import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonType;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CreditsDialog extends Dialog<Void> {
    private VBox vbMainContainer = new VBox();
    private static final Logger lMainLogger = LogManager.getLogger("DocxToBB");

    public CreditsDialog() {
        setTitle("Credits");
        setHeaderText("Resource Usage Attributions");
        initStyle(StageStyle.UTILITY);
        initModality(Modality.APPLICATION_MODAL);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        addCreditsText();
        getDialogPane().setContent(vbMainContainer);
    }

    private void addCreditsText() {
        TextArea taCredits = new TextArea();
        taCredits.setId("taCredits");
        taCredits.setWrapText(true);
        taCredits.setEditable(false);
        taCredits.setCursor(Cursor.DEFAULT);
        taCredits.setFocusTraversable(false);

        try {
            InputStream isCreditsFile = getClass().getClassLoader().getResourceAsStream("CREDITS.txt");
            taCredits.setText(IOUtils.toString(isCreditsFile, StandardCharsets.UTF_8.name()));
        } catch (IOException ioe) {
            taCredits.setText("Error loading credits file");
            lMainLogger.error("Error loading credits file");
        }

        vbMainContainer.getChildren().add(taCredits);
        setOnShown(event -> {
            Platform.runLater(() -> {
                taCredits.getScene().lookup("#taCredits .content").setCursor(Cursor.DEFAULT);
            });
        });
    }
}