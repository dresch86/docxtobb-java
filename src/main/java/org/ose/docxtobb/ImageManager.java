package org.ose.docxtobb;

import java.util.Map;
import java.util.Base64;
import java.util.HashMap;

import java.io.IOException;
import java.io.FileOutputStream;

import java.nio.file.Path;
import java.nio.file.Files;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.commons.codec.digest.DigestUtils;

import org.ose.docxtobb.xmlasset.IMSManifest;
import org.ose.docxtobb.types.ImagePresentation;
import org.ose.docxtobb.xmlasset.CSResourceLinks;

public class ImageManager {
    private Path pTempOutputDir;
    private Path pTempOutputDirMedia;
    private Path pTempOutputDirResources;
    private Path pTempOutputDirRespResources;

    private IMSManifest imsManifestHandler;
    private Map<String, String> hmFileMappedIds;

    private final CSResourceLinks csrlResLinkHandler = new CSResourceLinks();
    private static final Logger lMainLogger = LogManager.getLogger(ImageManager.class.getName());

    public ImageManager() {
        hmFileMappedIds = new HashMap<>();
    }

    public void setTempOutputDir(Path outputDir) {
        try {
            pTempOutputDir = outputDir;
            pTempOutputDirResources = Files.createDirectory(outputDir.resolve("csfiles"));
            pTempOutputDirRespResources = Files.createDirectory(outputDir.resolve("questions"));
            pTempOutputDirMedia = Files.createDirectory(pTempOutputDirResources.resolve("home_dir"));
            csrlResLinkHandler.setResourceOutputDir(pTempOutputDirMedia);
        } catch (IOException ioe) {
            lMainLogger.error(ioe.getMessage());
        }
    }

    public void finalizeQuestionResource(String resourceId, String parentId) {
        if (hmFileMappedIds.containsKey(resourceId)) {
            csrlResLinkHandler.addResource(resourceId, parentId, hmFileMappedIds.get(resourceId));
        } else {
            lMainLogger.error("Missing question resource [id=" + resourceId + "]");
        }
    }

    public String finalizeResponseResource(String resourceId, ImagePresentation imPresentation) {
        if (hmFileMappedIds.containsKey(resourceId)) {
            String sFilename = hmFileMappedIds.get(resourceId);
            
            try {
                if (imPresentation == ImagePresentation.INLINE) {
                    Path pImagePath = pTempOutputDirMedia.resolve(sFilename);
                    byte[] byaImage = Files.readAllBytes(pImagePath);
                    String base64 = Base64.getEncoder().encodeToString(byaImage);
                    Files.delete(pImagePath);

                    return ("data:" + Common.imageMime(sFilename) + ";base64," + base64);
                } else {
                    Path pRespResHashDir = Files.createDirectory(pTempOutputDirRespResources.resolve(DigestUtils.md5Hex(sFilename).toLowerCase()));
                    Path pMovedFilename = Files.move(pTempOutputDirMedia.resolve(sFilename), pRespResHashDir.resolve(sFilename));
                    String sFileRelPath = pTempOutputDirRespResources.relativize(pMovedFilename).toString();
                    imsManifestHandler.addResponseFileResource(sFileRelPath);

                    return sFileRelPath;
                }
            } catch (IOException ioe) {
                lMainLogger.error(ioe.getMessage());
                return "";
            } catch (Exception e) {
                lMainLogger.error("Image file extension not supported");
                return "";
            }
        } else {
            lMainLogger.error("Missing response resource [id=" + resourceId + "]");
            return "";
        }
    }

    public void setManifestHandler(IMSManifest manifestHandler) {
        imsManifestHandler = manifestHandler;
    }

    public void cacheImage(String resourceId, String filename, byte[] data) {
        try {
            Files.write(pTempOutputDirMedia.resolve(filename), data);
            hmFileMappedIds.put(resourceId, filename);
        } catch (IOException ioe) {
            lMainLogger.error(ioe.getMessage());
        }
    }

    public String getFilename(String resourceId) {
        if (hmFileMappedIds.containsKey(resourceId)) {
            return hmFileMappedIds.get(resourceId);
        } else {
            lMainLogger.error("Missing response resource filename [id=" + resourceId + "]");
            return null;
        }
    }

    public void flushAssets() {
        try {
            FileOutputStream fosResourcesXML = new FileOutputStream(pTempOutputDir.resolve("cslinks.dat").toFile());
            csrlResLinkHandler.writeBBXML(fosResourcesXML);
            fosResourcesXML.close();
        } catch (IOException ioe) {
            lMainLogger.error(ioe.getMessage());
        }
    }
}