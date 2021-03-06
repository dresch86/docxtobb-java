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
package org.ose.docxtobb.xmlasset;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

import java.nio.file.Path;
import java.io.IOException;
import java.io.FileOutputStream;

import com.github.djeang.vincerdom.VElement;
import com.github.djeang.vincerdom.VDocument;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.CDATASection;

import org.apache.commons.lang3.RandomStringUtils;

public class CSResourceLinks {
    private Path pTempOutputDirMedia;
    private Consumer<VElement<?>> csrCDataHandler;

    private final Document docW3CDoc;
    private final VDocument vdResourceLinksXML;
    private final Map<String, VElement<?>> maResourceMap;
    private static final Logger lMainLogger = LogManager.getLogger(CSResourceLinks.class.getName());

    public CSResourceLinks() {
        maResourceMap = new HashMap<>();
        vdResourceLinksXML = VDocument.of("cms_resource_link_list");
        docW3CDoc = vdResourceLinksXML.getW3cDocument();

        csrCDataHandler = (element) -> {
            Element w3cElement = element.getW3cElement();
            CDATASection cdsNodeData = null;

            if (w3cElement.getNodeName() == "storageType") {
                cdsNodeData = docW3CDoc.createCDATASection("PUBLIC");
                w3cElement.appendChild(cdsNodeData);
            } else if (w3cElement.getNodeName() == "resourceId") {
                cdsNodeData = docW3CDoc.createCDATASection(w3cElement.getTextContent());
                w3cElement.replaceChild(cdsNodeData, w3cElement.getFirstChild());
            }
        };
    }

    private void buildResourceRelation(String resourceId, String filename) {
        VDocument vdRelation = VDocument.of("lom");
        vdRelation.root().attr("xmlns", "http://www.imsglobal.org/xsd/imsmd_rootv1p2p1")
        .attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        .attr("xsi:schemaLocation", "http://www.imsglobal.org/xsd/imsmd_rootv1p2p1 imsmd_rootv1p2p1.xsd")
            .add("relation")
                .add("resource")
                    .add("identifier").text(resourceId + "#/courses/docxtobb/" + filename);

        String sXMLFilename = filename + ".xml";

        try {
            FileOutputStream fosRelationXML = new FileOutputStream(pTempOutputDirMedia.resolve(sXMLFilename).toFile());
            vdRelation.print(fosRelationXML);
            fosRelationXML.close();
        } catch (IOException ioe) {
            lMainLogger.error(ioe.getMessage());
        }
    }

    public void addResource(String resourceId, String parentId, String filename) {
        String sCMSResourceLinkId = "_" + RandomStringUtils.random(7, false, true) + "_1";

        maResourceMap.put(resourceId, vdResourceLinksXML.root()
        .add("cms_resource_link")
            .add("courseId").attr("data-type", "blackboard.data.course.Course").text("DocxToBBImports").__
            .add("parentId").attr("parent_data_type", "asiobject").text(parentId).__
            .add("resourceId").text(resourceId).apply(csrCDataHandler).__
            .add("storageType").apply(csrCDataHandler).__
            .add("id").attr("data-type", "blackboard.platform.contentsystem.data.CSResourceLink").text(sCMSResourceLinkId).__);

        buildResourceRelation(resourceId, filename);
    }

    public void setResourceOutputDir(Path outputDir) {
        pTempOutputDirMedia = outputDir;
    }

    public void writeBBXML(FileOutputStream os) {
        vdResourceLinksXML.print(os);
    }
}