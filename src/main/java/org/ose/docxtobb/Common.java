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

import java.nio.file.Path;
import java.nio.file.Files;

import java.io.File;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javafx.scene.layout.VBox;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;

public class Common { 
    public static void configureTextStackLogger() {
        PluginManager.addPackage("org.ose.docxtobb");
        
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR);
        builder.setConfigurationName("DocxToBB_GUI");
        builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
            .addAttribute("level", Level.DEBUG));

        AppenderComponentBuilder appenderBuilder = builder.newAppender("JFXTextStack", "TextStackAppender");
        appenderBuilder.add(builder.newLayout("PatternLayout")
            .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
            .addAttribute("marker", "FLOW"));
        builder.add(appenderBuilder);

        builder.add(builder.newLogger("org.ose.docxtobb", Level.INFO)
        .add(builder.newAppenderRef("JFXTextStack")).addAttribute("additivity", false));
        builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("JFXTextStack")));

        Configurator.initialize(builder.build());
    }

    public static void zipDirectory(Path directory, ZipOutputStream zipOutput) {
        try {
            Files.walk(directory).forEach(path -> {
                try {
                    Path pRelativePath = directory.relativize(path);
                    File file = path.toFile();
    
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        
                        if (files == null || files.length == 0) {
                            zipOutput.putNextEntry(new ZipEntry(
                                pRelativePath.toString() + File.separator));
                                    zipOutput.closeEntry();
                        }
                    } else {
                        zipOutput.putNextEntry(new ZipEntry(pRelativePath.toString()));
                        zipOutput.write(Files.readAllBytes(path));
                        zipOutput.closeEntry();
                    }
                } catch (IOException ioe) {
                    LogManager.getLogger(Common.class.getName()).error(ioe.getMessage());
                }
            });

            zipOutput.close();
        } catch (IOException ioe) {
            LogManager.getLogger(Common.class.getName()).error(ioe.getMessage());
        }
    }
}