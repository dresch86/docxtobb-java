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

public class BBLearnRubric {
    private final VDocument vdBBLearnRubricXML;

    public BBLearnRubric() {
        vdBBLearnRubricXML = VDocument.of("LEARNRUBRICS");
    }

    public void writeBBXML(FileOutputStream os) {
        vdBBLearnRubricXML.print(os);
    }
}