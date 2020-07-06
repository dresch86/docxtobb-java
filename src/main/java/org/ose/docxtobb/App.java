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

import java.util.Optional;
import java.io.IOException;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

import javafx.application.Platform;
import javafx.application.Application;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.file.FileSystemException;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLineParser;

public class App extends Application {
    private GUIController gicMainInterfaceController;

    public App() {
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlResource = new FXMLLoader(getClass().getResource("/MainInterface.fxml"));
            gicMainInterfaceController = new GUIController(stage);
            fxmlResource.setController(gicMainInterfaceController);
            Parent root = fxmlResource.load();

            stage.setOnCloseRequest(closeEvent -> {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Quit");
                alert.setHeaderText("Close Application");
                alert.setContentText("Are you sure you want to quit?");
                Optional<ButtonType> opQuitResult = alert.showAndWait();

                if (opQuitResult.isPresent() && (opQuitResult.get() == ButtonType.OK)) {
                    gicMainInterfaceController.shutdown();
                } else {
                    closeEvent.consume();
                }
            });

            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("DocxToBB");
            stage.setScene(new Scene(root));
            stage.setX(10.0);
            stage.setY(10.0);
            stage.show();
        } catch (IOException ioe) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Missing Application Resource");
            alert.setContentText(ioe.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        Options opCLIOps = new Options();
        opCLIOps.addOption("h", "headless", false, "Headless mode");
        CommandLineParser clpCLIHandler = new DefaultParser();

        try {
            CommandLine clCmdInput = clpCLIHandler.parse(opCLIOps, args);

            if (clCmdInput.hasOption("headless")) {
                System.out.println("Starting in headless mode...");
                System.out.println("This feature is not yet implemented!");
                //Vertx vertx = Vertx.vertx();
            } else {
                launch(args);
            }
        } catch (ParseException e) {
            System.err.println("CLI Parse Error: " + e.getMessage());
        }
    }
}