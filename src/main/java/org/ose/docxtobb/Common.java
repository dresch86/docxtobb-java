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

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

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

    public static String imageMime(String filename) throws Exception {
        switch (FilenameUtils.getExtension(filename)) {
            case "wmf":
                return "image/x-wmf";
            case "png":
                return "image/png";
            case "bmp":
                return "image/bmp";
            case "cod":
                return "image/cis-cod";
            case "gif":
                return "image/gif";
            case "ief":
                return "image/ief";
            case "jpg":
            case "jpeg":
                return "image/jpg";
            case "jfif":
                return "image/pipeg";
            case "svg":
                return "image/svg+xml";
            case "tiff":
                return "image/tiff";
            case "ras":
                return "image/x-cmu-raster";
            case "cmx":
                return "image/x-cmx";
            case "ico":
                return "image/x-icon";
            case "pnm":
                return "image/x-portable-anymap";
            case "pbm":
                return "image/x-portable-bitmap";
            case "pgm":
                return "image/x-portable-graymap";
            case "xwd":
                return "image/x-xwindowdump";
            default:
                throw new Exception("Unsupported Image Type");
        }
    }

    public static String imageExt(String mimeType) throws Exception {
        switch (mimeType) {
            case "image/x-wmf":
                return "wmf";
            case "image/png":
                return "png";
            case "image/bmp":
                return "bmp";
            case "image/cis-cod":
                return "cod";
            case "image/gif":
                return "gif";
            case "image/ief":
                return "ief";
            case "image/jpg":
            case "image/jpeg":
                return "jpg";
            case "image/pipeg":
                return "jfif";
            case "image/svg+xml":
                return "svg";
            case "image/tiff":
                return "tiff";
            case "image/x-cmu-raster":
                return "ras";
            case "image/x-cmx":
                return "cmx";
            case "image/x-icon":
                return "ico";
            case "image/x-portable-anymap":
                return "pnm";
            case "image/x-portable-bitmap":
                return "pbm";
            case "image/x-portable-graymap":
                return "pgm";
            case "image/x-xwindowdump":
                return "xwd";
            default:
                throw new Exception("Unsupported Image Type");
        }
    }

    public static void zipDirectory(Path directory, ZipOutputStream zipOutput) {
        try {
            Files.walk(directory).forEach(path -> {
                try {
                    Path pRelativePath = directory.relativize(path);
                    File file = path.toFile();

                    if (file.isFile()) {
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