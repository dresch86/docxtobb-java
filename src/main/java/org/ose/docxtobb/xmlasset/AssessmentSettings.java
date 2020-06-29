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

import org.apache.commons.lang3.RandomStringUtils;

public class AssessmentSettings {
    private final VDocument vdSettingsXML;

    public AssessmentSettings(String examId, int totalPoints) {
        vdSettingsXML = VDocument.of("ASSESSMENTCREATIONSETTINGS").root()
        .add("ASSESSMENTCREATIONSETTING").attr("id", RandomStringUtils.randomAlphanumeric(32)).__
        .add("QTIASSESSMENTID").attr("value", examId).__
        .add("ANSWERFEEDBACKENABLED").text("true").__
        .add("QUESTIONATTACHMENTSENABLED").text("true").__
        .add("ANSWERATTACHMENTSENABLED").text("true").__
        .add("QUESTIONMETADATAENABLED").text("true").__
        .add("DEFAULTPOINTVALUEENABLED").text("true").__
        .add("DEFAULTPOINTVALUE").text(String.valueOf(totalPoints)).__
        .add("ANSWERPARTIALCREDITENABLED").text("true").__
        .add("ANSWERNEGATIVEPOINTSENABLED").text("true").__
        .add("ANSWERRANDOMORDERENABLED").text("true").__
        .add("ANSWERORIENTATIONENABLED").text("true").__
        .add("ANSWERNUMBEROPTIONSENABLED").text("true").__
        .add("USEPOINTSFROMSOURCEBYDEFAULT").text("true").__.__;
    }

    public void writeBBXML(FileOutputStream os) {
        vdSettingsXML.print(os);
    }
}