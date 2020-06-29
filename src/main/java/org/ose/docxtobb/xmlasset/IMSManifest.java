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

import java.io.FileOutputStream;
import com.github.djeang.vincerdom.VDocument;

public class IMSManifest {
    private final VDocument vdIMSManifestXML;

    public IMSManifest(String title) {
        vdIMSManifestXML = VDocument.of("manifest").root().attr("identifier", "man00001").attr("xmlns:bb", "http://www.blackboard.com/content-packaging/")
        .add("organizations").__
        .add("resources")
            .add("resource")
                .attr("bb:file", "questions.dat")
                .attr("bb:title", title)
                .attr("identifier", "questions")
                .attr("type", "assessment/x-bb-qti-test")
                .attr("xml:base", "questions").__
            .add("resource")
                .attr("bb:file", "settings.dat")
                .attr("bb:title", "Assessment Creation Settings")
                .attr("identifier", "settings")
                .attr("type", "course/x-bb-courseassessmentcreationsettings")
                .attr("xml:base", "settings").__
            .add("resource")
                .attr("bb:file", "bbrubrics.dat")
                .attr("bb:title", "LearnRubrics")
                .attr("identifier", "bbrubrics")
                .attr("type", "course/x-bb-rubrics")
                .attr("xml:base", "bbrubrics").__
            .add("resource")
                .attr("bb:file", "cslinks.dat")
                .attr("bb:title", "CSResourceLinks")
                .attr("identifier", "cslinks")
                .attr("type", "course/x-bb-csresourcelinks")
                .attr("xml:base", "cslinks").__
            .add("resource")
                .attr("bb:file", "assocrubric.dat")
                .attr("bb:title", "CourseRubricAssociation")
                .attr("identifier", "assocrubric")
                .attr("type", "course/x-bb-crsrubricassocation")
                .attr("xml:base", "assocrubric").__.__.__;
    }

    public void writeBBXML(FileOutputStream os) {
        vdIMSManifestXML.print(os);
    }
}