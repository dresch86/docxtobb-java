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

import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;

import java.io.IOException;
import java.io.FileOutputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;

import net.arnx.wmf2svg.gdi.svg.SvgGdi;
import net.arnx.wmf2svg.gdi.wmf.WmfParser;

import org.zwobble.mammoth.Result;
import org.zwobble.mammoth.images.Image;
import org.zwobble.mammoth.DocumentConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.ose.docxtobb.xmlasset.Questions;
import org.ose.docxtobb.xmlasset.IMSManifest;
import org.ose.docxtobb.xmlasset.BBLearnRubric;
import org.ose.docxtobb.xmlasset.CSResourceLinks;
import org.ose.docxtobb.xmlasset.CourseRubricAssociations;
import org.ose.docxtobb.xmlasset.AssessmentSettings;

public class AssessmentPacker {
    private int iPoints;
    private int iWarnCount;
    private int iImageCount;
    private int iQuestionCount;

    private Path pTempOutputDir;
    private Path pTempOutputDirMedia;
    private Path pTempOutputDirResources;

    private String sTitle;
    private boolean boolRemoveNumericalLabels;

    private final String sExamID = RandomStringUtils.randomAlphanumeric(32);
    private final CSResourceLinks csrlResLinkHandler = new CSResourceLinks();
    private static final Logger lMainLogger = LogManager.getLogger("DocxToBB");

    public AssessmentPacker() {
        iPoints = 100;
        iWarnCount = 0;
        iImageCount = 0;
        boolRemoveNumericalLabels = false;
        sTitle = "Autogenerated DocxToBB Exam";
    }

    private boolean readyTempDir(String filename) {
        String sMD5Filename = DigestUtils.md5Hex(filename).toUpperCase();
        String sSysTmpDir = System.getProperty("java.io.tmpdir");
        Path pTempRoot = Paths.get(sSysTmpDir);

        try {    
            if (Files.exists(pTempRoot)) {
                pTempOutputDir = Files.createDirectory(pTempRoot.resolve(sMD5Filename));
                pTempOutputDirResources = Files.createDirectory(pTempOutputDir.resolve("csfiles"));
                pTempOutputDirMedia = Files.createDirectory(pTempOutputDirResources.resolve("home_dir"));

                return true;
            } else {
                return false;
            }
        } catch (FileAlreadyExistsException de) {
            if (FileUtils.deleteQuietly(pTempRoot.resolve(sMD5Filename).toFile())) {
                return readyTempDir(filename);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String imageExt(String mimeType) throws Exception {
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

    private Map<String, String> processImage(Image image) {
        try {
            String sFileExt = imageExt(image.getContentType());
            FileOutputStream fosImageFile = null;
            String sFilename = "";
            String sResId = "";

            if (sFileExt == "wmf") {
                final SvgGdi sgWMFtoSVGHandler = new SvgGdi(true);
                sgWMFtoSVGHandler.setReplaceSymbolFont(true);

                final WmfParser wpWmfHandler = new WmfParser();
                wpWmfHandler.parse(image.getInputStream(), sgWMFtoSVGHandler);

                sResId = RandomStringUtils.random(7, false, true) + "_1";
                sFilename = "Image_" + iImageCount + ".svg";
                csrlResLinkHandler.addResource(sResId, sFilename);

                try {
                    fosImageFile = new FileOutputStream(pTempOutputDirMedia.resolve(sFilename).toFile());
                    sgWMFtoSVGHandler.write(fosImageFile);
                    fosImageFile.close();
                } catch (IOException ioe) {
                    lMainLogger.error(ioe.getMessage());
                } finally {
                    if (fosImageFile != null) fosImageFile.close();
                }
            } else {
                // String base64 = Base64.getEncoder().encodeToString(IOUtils.toByteArray(image.getInputStream()));
                // String src = "data:" + image.getContentType() + ";base64," + base64;
                sResId = RandomStringUtils.random(7, false, true) + "_1";
                sFilename = "Image_" + iImageCount + "." + sFileExt;
                csrlResLinkHandler.addResource(sResId, sFilename);

                try {
                    fosImageFile = new FileOutputStream(pTempOutputDirMedia.resolve(sFilename).toFile());
                    fosImageFile.write(IOUtils.toByteArray(image.getInputStream()));
                    fosImageFile.close();
                } catch (IOException ioe) {
                    lMainLogger.error(ioe.getMessage());
                } finally {
                    if (fosImageFile != null) fosImageFile.close();
                }
            }

            Map<String, String> attributes = new HashMap<>();
            attributes.put("data-resource-id", sResId);
            attributes.put("src", "@X@EmbeddedFile.requestUrlStub@X@bbcswebdav/xid-" + sResId);
            ++iImageCount;

            return attributes;
        } catch (Exception e) {
            String sWarnImgId = RandomStringUtils.random(7, false, true) + "_1";
            String sFilename = "Warning_" + iWarnCount + ".svg";

            Map<String, String> attributes = new HashMap<>();
            attributes.put("data-resource-id", sWarnImgId);
            attributes.put("src", "@X@EmbeddedFile.requestUrlStub@X@bbcswebdav/xid-" + sWarnImgId);

            try {
                Files.copy(getClass().getClassLoader().getResourceAsStream("dist/warning.svg"), 
                pTempOutputDir.resolve(sFilename));
            } catch (IOException ioe) {
                lMainLogger.error(ioe.getMessage());
            }

            csrlResLinkHandler.addResource(sWarnImgId, sFilename);
            lMainLogger.error(e.getMessage());
            ++iWarnCount;

            return attributes;
        }
    }

    private void createQuestionParser(String html) {
        Questions qQuestionHandler = new Questions(sExamID, sTitle, iQuestionCount, iPoints);
        qQuestionHandler.enableQuestionIndexRemover(boolRemoveNumericalLabels);
        qQuestionHandler.setResourceHandler((resourceId, parentId) -> csrlResLinkHandler.addParentId(resourceId, parentId));
        String sResult = qQuestionHandler.processHTML(html);

        if (sResult == "") {
            lMainLogger.info("Conversion completed successfully without warnings!");
        } else {
            lMainLogger.warn(sResult);
        }

        try {
            FileOutputStream fosQuestionsXML = new FileOutputStream(pTempOutputDir.resolve("questions.dat").toFile());
            qQuestionHandler.writeBBXML(fosQuestionsXML);
            fosQuestionsXML.close();

            FileOutputStream fosResourcesXML = new FileOutputStream(pTempOutputDir.resolve("cslinks.dat").toFile());
            csrlResLinkHandler.writeBBXML(fosResourcesXML);
            fosResourcesXML.close();
        } catch (IOException ioe) {
            lMainLogger.error(ioe.getMessage());
        }
    }

    public void setPointTotal(String points) {
        if (points.trim().length() > 0) {
            try {
                iPoints = Integer.parseInt(points);
            } catch (NumberFormatException nfe) {
                iPoints = 100;
            }
        }
    }

    public void setQuestionCount(String numQuestions) {
        if (numQuestions.trim().length() > 0) {
            try {
                iQuestionCount = Integer.parseInt(numQuestions);
            } catch (NumberFormatException nfe) {
                lMainLogger.error("Question counts must be an integer value");
            }
        }
    }

    public void setExamTitle(String title) {
        if (title.trim().length() > 0) {
            sTitle = title;
        }
    }

    public void removeQuestionNumbering(boolean removeNumericalLabels) {
        boolRemoveNumericalLabels = removeNumericalLabels;
    }

    public String loadFromFile(Path inputFile, Path outputFile) {
        try {
            if (readyTempDir(inputFile.getFileName().toString())) {
                csrlResLinkHandler.setResourceOutputDir(pTempOutputDirMedia);
                iImageCount = 0;

                DocumentConverter dcDocxHandler = new DocumentConverter()    
                    .imageConverter(image -> processImage(image));
                Result<String> result = dcDocxHandler.convertToHtml(inputFile.toFile());
                createQuestionParser(result.getValue());
                
                try {
                    AssessmentSettings asSettingsHandler = new AssessmentSettings(sExamID, iPoints);
                    FileOutputStream fosSettingsXMLOut = new FileOutputStream(pTempOutputDir.resolve("settings.dat").toFile());
                    asSettingsHandler.writeBBXML(fosSettingsXMLOut);
                    fosSettingsXMLOut.close();

                    IMSManifest imsManifestHandler = new IMSManifest(sTitle);
                    FileOutputStream fosManifestXMLOut = new FileOutputStream(pTempOutputDir.resolve("imsmanifest.xml").toFile());
                    imsManifestHandler.writeBBXML(fosManifestXMLOut);
                    fosManifestXMLOut.close();

                    BBLearnRubric blrBBLRubricHandler = new BBLearnRubric();
                    FileOutputStream fosBBLRubricXMLOut = new FileOutputStream(pTempOutputDir.resolve("bbrubrics.dat").toFile());
                    blrBBLRubricHandler.writeBBXML(fosBBLRubricXMLOut);
                    fosBBLRubricXMLOut.close();

                    CourseRubricAssociations craCourseRubricAssocsHandler = new CourseRubricAssociations();
                    FileOutputStream fosCourseRubricAssocsXMLOut = new FileOutputStream(pTempOutputDir.resolve("assocrubric.dat").toFile());
                    craCourseRubricAssocsHandler.writeBBXML(fosCourseRubricAssocsXMLOut);
                    fosCourseRubricAssocsXMLOut.close();

                    FileOutputStream fosPackageOut = new FileOutputStream(outputFile.toFile());
                    ZipOutputStream zosZipPackageOut = new ZipOutputStream(fosPackageOut);
                    Common.zipDirectory(pTempOutputDir, zosZipPackageOut);
                } catch (IOException ioe) {
                    lMainLogger.error(ioe.getMessage());
                }
        
                return String.join("\n", result.getWarnings()); 
            } else {
                return "Error establishing temporary file system resource";
            }
        } catch (IOException ioe) {
            return ioe.getMessage();
        }
    }
}