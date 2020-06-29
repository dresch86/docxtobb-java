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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Common {
    private static final Logger lMainLogger = LogManager.getLogger("DocxToBB");
    
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
                    lMainLogger.error(ioe.getMessage());
                }
            });

            zipOutput.close();
        } catch (IOException ioe) {
            lMainLogger.error(ioe.getMessage());
        }
    }
}