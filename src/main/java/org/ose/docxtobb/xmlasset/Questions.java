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
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.BiConsumer;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.DecimalFormat;
import java.io.FileOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.github.djeang.vincerdom.VElement;
import com.github.djeang.vincerdom.VDocument;

import org.javatuples.Pair;
import org.apache.commons.lang3.RandomStringUtils;

import org.ose.docxtobb.ImageManager;
import org.ose.docxtobb.types.QuestionType;
import org.ose.docxtobb.types.ImagePresentation;
import org.ose.docxtobb.exceptions.QuestionFormatException;

public class Questions {
    private ImageManager imImageMgr;
    private Document docHTMLfromWord;
    private BigDecimal bdTotalPoints;
    private BigDecimal bdPointsPerQuestion;
    private Map<String, Boolean> mAdvancedOps;

    private VElement<?> veDescription;
    private VElement<?> veInstructions;

    private String sExamID = "";
    private int iQuestionCount = 0;
    private boolean boolEnableQIdxRemover = false;

    private final VDocument vdQuestionXML;
    private final VElement<?> veQuestionSection;
    private final Pattern patQuestionLbl = Pattern.compile("^(?:\\p{Print}*?)(#?[0-9]+[\\.|\\)]\\s*)(?:\\p{Print}*?)$", Pattern.UNICODE_CHARACTER_CLASS);

    public Questions(String _id, String _title, int _numQuestions, int _points) {
        sExamID = _id;
        iQuestionCount = _numQuestions;

        bdTotalPoints = new BigDecimal(_points);
        bdPointsPerQuestion = bdTotalPoints.divide(new BigDecimal(iQuestionCount), RoundingMode.HALF_EVEN);

        vdQuestionXML = VDocument.of("questestinterop");
        veQuestionSection = vdQuestionXML.root()
            .add("assessment").attr("title", _title)
                .add("assessmentmetadata")
                    .add("bbmd_asi_object_id").text(sExamID).__
                    .add("bbmd_asitype").text("Assessment").__
                    .add("bbmd_assessmenttype").text("Test").__
                    .add("bbmd_sectiontype").text("Subsection").__
                    .add("bbmd_questiontype").text("Multiple Choice").__
                    .add("bbmd_is_from_cartridge").text("false").__
                    .add("bbmd_is_disabled").text("false").__
                    .add("bbmd_negative_points_ind").text("N").__
                    .add("bbmd_canvas_fullcrdt_ind").text("false").__
                    .add("bbmd_all_fullcredit_ind").text("false").__
                    .add("bbmd_numbertype").text("none").__
                    .add("bbmd_partialcredit").text("").__
                    .add("bbmd_orientationtype").text("vertical").__
                    .add("bbmd_is_extracredit").text("false").__
                    .add("qmd_absolutescore_max").text(new DecimalFormat("0.000000000000000").format(bdTotalPoints)).__
                    .add("qmd_weighting").text("0").__
                    .add("qmd_instructornotes").text("").__.__
                .add("rubric").attr("view", "All")
                    .add("flow_mat").attr("class", "Block")
                        .add("material")
                            .add("mat_extension")
                                .add("mat_formattedtext").attr("type", "HTML").apply(element -> veInstructions = element).__.__.__.__.__
                .add("presentation_material")
                    .add("flow_mat").attr("class", "Block")
                        .add("material")
                            .add("mat_extension")
                                .add("mat_formattedtext").attr("type", "HTML").apply(element -> veDescription = element).__.__.__.__.__
                .add("section");

        veQuestionSection.add("sectionmetadata")
            .add("bbmd_asi_object_id").text(RandomStringUtils.randomAlphanumeric(32)).__
            .add("bbmd_asitype").text("Section").__
            .add("bbmd_assessmenttype").text("Test").__
            .add("bbmd_sectiontype").text("Subsection").__
            .add("bbmd_questiontype").text("Multiple Choice").__
            .add("bbmd_is_from_cartridge").text("false").__
            .add("bbmd_is_disabled").text("false").__
            .add("bbmd_negative_points_ind").text("N").__
            .add("bbmd_canvas_fullcrdt_ind").text("false").__
            .add("bbmd_all_fullcredit_ind").text("false").__
            .add("bbmd_numbertype").text("none").__
            .add("bbmd_partialcredit").text("").__
            .add("bbmd_orientationtype").text("vertical").__
            .add("bbmd_is_extracredit").text("false").__
            .add("qmd_absolutescore_max").text(new DecimalFormat("0.000000000000000").format(bdTotalPoints)).__
            .add("qmd_weighting").text("0").__
            .add("qmd_instructornotes").text("");
    }

    private String removeNumericalIndex(String questionHTML) {
        Matcher matQuestionLbl = patQuestionLbl.matcher(questionHTML);
        String sSanitizedQuestionHTML = questionHTML;

        if (matQuestionLbl.matches()) {
            sSanitizedQuestionHTML = questionHTML.substring(0, matQuestionLbl.start(1)) + questionHTML.substring(matQuestionLbl.end(1));
        } else {
            System.out.println(questionHTML);
        }

        return sSanitizedQuestionHTML;
    }

    private boolean isIsolatedImg(Element responseBox) {
        Elements elsNonImgTags = responseBox.select("p :not(img)");

        if (elsNonImgTags.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    private void inlineResponseImgs(String responseId, Element responseBox, final VElement<?> renderChoiceRoot) {
        Elements elsAnsImgs = responseBox.select("img");
        int iImgCount = elsAnsImgs.size();
        Element elCurrentImgTag = null;

        for (int i=0; i<iImgCount; i++) {
            elCurrentImgTag = elsAnsImgs.get(i);
            String sBase64Embed = imImageMgr.finalizeResponseResource(elCurrentImgTag.attr("data-resource-id"), ImagePresentation.INLINE);
            elCurrentImgTag.removeAttr("data-resource-id");
            elCurrentImgTag.attr("src", sBase64Embed);
        }

        renderChoiceRoot.add("flow_label").attr("class", "Block")
        .add("response_label").attr("ident", responseId).attr("shuffle", "Yes").attr("rarea", "Ellipse").attr("rrange", "Exact")
            .add("flow_mat").attr("class", "FORMATTED_TEXT_BLOCK")
                .add("material")
                    .add("mat_extension")
                        .add("mat_formattedtext").attr("type", "HTML").text(responseBox.html());        
    }

    private void addResponses(int qid,
                                QuestionType questionType, 
                                VElement<?> questionRoot, 
                                int correctAnswerCount,
                                Map<String, Pair<Boolean, Element>> answerKey) {
        String sShuffle = "Yes";

        if (!mAdvancedOps.get("RANDOM_RESPS")) {
            sShuffle = "No";
        }

        final VElement<?> elRenderChoiceRoot = questionRoot.get("presentation").get("flow").add("flow").attr("class", "RESPONSE_BLOCK")
        .add("response_lid").attr("ident", "response").attr("rcardinality", "Single").attr("rtiming", "No")
            .add("render_choice").attr("shuffle", sShuffle).attr("minnumber", "0").attr("maxnumber", "0");

        final VElement<?> elResprocessingRoot = questionRoot.add("resprocessing").attr("scoremodel", "SumOfScores")
        .add("outcomes")
            .add("decvar").attr("varname", "SCORE")
                            .attr("vartype", "Decimal")
                            .attr("defaultval", "0")
                            .attr("minvalue", "0")
                            .attr("maxvalue", new DecimalFormat("0.000000000000000").format(bdPointsPerQuestion)).__.__;

        final VElement<?> elCorrectConds = elResprocessingRoot.add("respcondition").attr("title", "correct");
        
        elResprocessingRoot.add("respcondition").attr("title", "incorrect")
        .add("conditionvar")
            .add("other").__.__
        .add("setvar").attr("variablename", "SCORE").attr("action", "Set").text("0.0").__
        .add("displayfeedback").attr("linkrefid", "incorrect").attr("feedbacktype", "Response");

        questionRoot.add("itemfeedback").attr("ident", "correct").attr("view", "All")
        .add("flow_mat").attr("class", "Block")
            .add("flow_mat").attr("class", "FORMATTED_TEXT_BLOCK")
                .add("material")
                    .add("mat_extension")
                        .add("mat_formattedtext").attr("type", "HTML");

        questionRoot.add("itemfeedback").attr("ident", "incorrect").attr("view", "All")
        .add("flow_mat").attr("class", "Block")
        .add("flow_mat").attr("class", "FORMATTED_TEXT_BLOCK")
            .add("material")
                .add("mat_extension")
                    .add("mat_formattedtext").attr("type", "HTML");

        BiConsumer<Boolean, String> bmrRegisterResponseCondVars = null;

        if (correctAnswerCount > 1) {
            final VElement<?> elCorrectCondsVars = elCorrectConds.add("conditionvar").add("and").__.__
            .add("setvar").attr("variablename", "SCORE").attr("action", "Set").text("SCORE.max").__
            .add("displayfeedback").attr("linkrefid", "correct").attr("feedbacktype", "Response").__
            .get("conditionvar").get("and");

            BigDecimal bdPointsPerAnswer = bdPointsPerQuestion.divide(new BigDecimal(correctAnswerCount), RoundingMode.HALF_EVEN);
            String sPointsPerAnsFmt = new DecimalFormat("0.000000000000000").format(bdPointsPerAnswer);

            bmrRegisterResponseCondVars = (correctness, responseId) -> {
                if (correctness == Boolean.TRUE) {
                    elCorrectCondsVars.add("varequal").attr("respident", "response").attr("case", "No").text(responseId);
                    elResprocessingRoot.add("respcondition").add("conditionvar").add("varequal").attr("respident", responseId).attr("case", "No").__.__
                    .add("setvar").attr("variablename", "SCORE").attr("action", "Set").text(sPointsPerAnsFmt);
                } else {
                    elCorrectCondsVars.add("not").add("varequal").attr("respident", "response").attr("case", "No").text(responseId);
                    elResprocessingRoot.add("respcondition").add("conditionvar").add("varequal").attr("respident", responseId).attr("case", "No").__.__
                    .add("setvar").attr("variablename", "SCORE").attr("action", "Set").text("0");
                }
            };
        } else {
            final VElement<?> elCorrectCondsVars = elCorrectConds.add("conditionvar").__
            .add("setvar").attr("variablename", "SCORE").attr("action", "Set").text("SCORE.max").__
            .add("displayfeedback").attr("linkrefid", "correct").attr("feedbacktype", "Response").__
            .get("conditionvar");

            String sPointsPerAnsFmt = new DecimalFormat("0.000000000000000").format(bdPointsPerQuestion);

            bmrRegisterResponseCondVars = (correctness, responseId) -> {
                if (correctness == Boolean.TRUE) {
                    elCorrectCondsVars.add("varequal").attr("respident", "response").attr("case", "No").text(responseId);
                    elResprocessingRoot.add("respcondition").add("conditionvar").add("varequal").attr("respident", responseId).attr("case", "No").__.__
                    .add("setvar").attr("variablename", "SCORE").attr("action", "Set").text(sPointsPerAnsFmt).__
                    .add("displayfeedback").attr("linkrefid", responseId).attr("feedbacktype", "Response");
                } else {
                    elResprocessingRoot.add("respcondition").add("conditionvar").add("varequal").attr("respident", responseId).attr("case", "No").__.__
                    .add("setvar").attr("variablename", "SCORE").attr("action", "Set").text("0").__
                    .add("displayfeedback").attr("linkrefid", responseId).attr("feedbacktype", "Response");
                }
            };
        }

        String sResponseId = "";
        Elements elsAnsImgs = null;

        for (Map.Entry<String, Pair<Boolean, Element>> response : answerKey.entrySet()) {
            sResponseId = RandomStringUtils.randomAlphanumeric(32);
            elsAnsImgs = response.getValue().getValue1().select("img");

            if (elsAnsImgs.size() == 0) {
                elRenderChoiceRoot.add("flow_label").attr("class", "Block")
                .add("response_label").attr("ident", sResponseId).attr("shuffle", "Yes").attr("rarea", "Ellipse").attr("rrange", "Exact")
                    .add("flow_mat").attr("class", "FORMATTED_TEXT_BLOCK")
                        .add("material")
                            .add("mat_extension")
                                .add("mat_formattedtext").attr("type", "HTML").text(response.getValue().getValue1().html());
            } else {
                if (isIsolatedImg(response.getValue().getValue1())) {
                    // Must inline image since response field cannot be empty
                    inlineResponseImgs(sResponseId, response.getValue().getValue1(), elRenderChoiceRoot);
                } else {
                    if (!mAdvancedOps.get("INLINE_RIMGS") && (elsAnsImgs.size() == 1)) {
                        // Image can only be added as an attachment if there are 1) other nodes; 2) inlining is disabled; 3) there is only 1 image
                        Element elImgTag = elsAnsImgs.get(0);
                        String sImgFilename = imImageMgr.getFilename(elImgTag.attr("data-resource-id"));
                        String sRelativePath = imImageMgr.finalizeResponseResource(elImgTag.attr("data-resource-id"), ImagePresentation.ATTACHMENT);
                        elImgTag.remove();
                        
                        elRenderChoiceRoot.add("flow_label").attr("class", "Block")
                        .add("response_label").attr("ident", sResponseId).attr("shuffle", "Yes").attr("rarea", "Ellipse").attr("rrange", "Exact")
                            .add("flow_mat").attr("class", "FORMATTED_TEXT_BLOCK")
                                .add("material")
                                    .add("mat_extension")
                                        .add("mat_formattedtext").attr("type", "HTML").text(response.getValue().getValue1().html()).__.__.__.__
                            .add("flow_mat").attr("class", "FILE_BLOCK")
                                .add("material")
                                    .add("matapplication").attr("label", sImgFilename).attr("apptype", "application/octet-stream").attr("uri", sRelativePath).attr("embedded", "Inline");
                    } else {
                        inlineResponseImgs(sResponseId, response.getValue().getValue1(), elRenderChoiceRoot);
                    }
                }
            }        

            bmrRegisterResponseCondVars.accept(response.getValue().getValue0(), sResponseId);

            questionRoot.add("itemfeedback").attr("ident", sResponseId).attr("view", "All")
            .add("solution").attr("view", "All").attr("feedbackstyle", "Complete")
                .add("solutionmaterial")
                    .add("flow_mat").attr("class", "Block")
                        .add("flow_mat").attr("class", "FORMATTED_TEXT_BLOCK")
                            .add("material")
                                .add("mat_extension")
                                    .add("mat_formattedtext").attr("type", "HTML");
        }
    }

    private void addQuestions(int startId, List<Pair<QuestionType, Element>> questions) {
        int iQuestionCount = questions.size();
        QuestionType qtQuestionType;
        Element elQuestionBox;

        for (int i=0; i<iQuestionCount; i++) {
            elQuestionBox = questions.get(i).getValue1();
            qtQuestionType = questions.get(i).getValue0();

            Element elQuestionContentBox = elQuestionBox.selectFirst("tr").selectFirst("td");
            String sParentId = RandomStringUtils.randomAlphanumeric(32);
            Elements elsImgResources = elQuestionContentBox.select("img");
            int iImgTagCount = elsImgResources.size();
            int iNextId = startId + i;

            if (iImgTagCount > 0) {
                elsImgResources.forEach(imgTag -> {
                    imImageMgr.finalizeQuestionResource(imgTag.attr("data-resource-id"), sParentId);
                    imgTag.removeAttr("data-resource-id");
                });
            }

            Map<String, Pair<Boolean, Element>> tmResponses = new TreeMap<>();
            Elements elQuestionBoxStack = elQuestionBox.select("tr");

            int iCorrectAnswers = 0;
            Elements elsAnswerLayer;
            Element elAnsLabelBox;
            Element elAnsContent;

            // Skip the question text layer again and start processing response layers
            // Cache answers into tree map (TreeMap@tmResponses) in the event ordering is important
            for (int j=1; j<elQuestionBoxStack.size(); j++) {
                elsAnswerLayer = elQuestionBoxStack.get(j).select("td");

                for (int k=0; k<elsAnswerLayer.size(); k+=2) {
                    elAnsLabelBox = elsAnswerLayer.get(k).selectFirst("p");
                    elAnsContent = elsAnswerLayer.get(k+1).selectFirst("p");

                    if (!elAnsLabelBox.select("strong").isEmpty()) {
                        // Found a correct answer indicated by bold face font
                        Elements elsAnswerContentBolded = elAnsContent.select("strong");
                        ++iCorrectAnswers;

                        if (elsAnswerContentBolded.size() == 1) {
                            tmResponses.put(elAnsLabelBox.text(), 
                                            new Pair<Boolean,Element>(Boolean.TRUE, elsAnswerContentBolded.get(0)));
                        } else {
                            tmResponses.put(elAnsLabelBox.text(), 
                                            new Pair<Boolean,Element>(Boolean.TRUE, elAnsContent));
                        }
                    } else {
                        tmResponses.put(elAnsLabelBox.text(), 
                                        new Pair<Boolean,Element>(Boolean.FALSE, elAnsContent));
                    }
                }
            }

            VElement<?> elQuestionRoot = veQuestionSection.add("item").attr("title", ("Q" + iNextId)).attr("maxattempts", "0");
            elQuestionRoot.add("itemmetadata")            
                .add("bbmd_asi_object_id").text(sParentId).__
                .add("bbmd_asitype").text("Item").__
                .add("bbmd_assessmenttype").text("Test").__
                .add("bbmd_sectiontype").text("Subsection").__
                .add("bbmd_questiontype").text(qtQuestionType.toString()).__
                .add("bbmd_is_from_cartridge").text("false").__
                .add("bbmd_is_disabled").text("false").__
                .add("bbmd_negative_points_ind").text("N").__
                .add("bbmd_canvas_fullcrdt_ind").text("false").__
                .add("bbmd_all_fullcredit_ind").text("false").__
                .add("bbmd_numbertype").text("none").__
                .add("bbmd_partialcredit").text("false").__
                .add("bbmd_orientationtype").text("vertical").__
                .add("bbmd_is_extracredit").text("false").__
                .add("qmd_absolutescore_max").text(new DecimalFormat("0.000000000000000").format(bdPointsPerQuestion)).__
                .add("qmd_weighting").text("0").__
                .add("qmd_instructornotes").text("");
    
            elQuestionRoot.add("presentation")
                .add("flow").attr("class", "Block")
                    .add("flow").attr("class", "QUESTION_BLOCK")
                        .add("flow").attr("class", "FORMATTED_TEXT_BLOCK")
                            .add("material")
                                .add("mat_extension")
                                    .add("mat_formattedtext").attr("type", "HTML").text(elQuestionContentBox.html());
    
            addResponses(iNextId, qtQuestionType, elQuestionRoot, iCorrectAnswers, tmResponses);
        }
    }

    private List<Pair<QuestionType, Element>> validateQuestion(int nextQuestionIndex, Element primaryQuestionBox) throws QuestionFormatException {
        Elements elsPrimaryQuestionBoxStack = primaryQuestionBox.select("tr");
        int iPrimaryQuestionBoxStackCount = elsPrimaryQuestionBoxStack.size();

        if (iPrimaryQuestionBoxStackCount == 0) {
            throw new QuestionFormatException("Invalid question format [Q" + (nextQuestionIndex + 1) + "]");
        } else if (iPrimaryQuestionBoxStackCount == 1) {
            if (boolEnableQIdxRemover) {
                Element elQuestionCell = primaryQuestionBox.selectFirst("tr").selectFirst("td");
                String sQuestionHTML = removeNumericalIndex(elQuestionCell.html());
                elQuestionCell.empty();
                elQuestionCell.append(sQuestionHTML);
            }

            List<Pair<QuestionType, Element>> lValidQuestions = new ArrayList<>();
            lValidQuestions.add(new Pair<QuestionType, Element>(QuestionType.FILE_UPLOAD, primaryQuestionBox));
            
            return lValidQuestions;
        } else {
            Element elFirstCell = elsPrimaryQuestionBoxStack.get(0).selectFirst("td");

            if (elFirstCell.hasAttr("colspan")) {
                // The box's first row spans multiple columns so it should be standard question

                if (boolEnableQIdxRemover) {
                    Element elQuestionCell = primaryQuestionBox.selectFirst("tr").selectFirst("td");
                    String sQuestionHTML = removeNumericalIndex(elQuestionCell.html());
                    elQuestionCell.empty();
                    elQuestionCell.append(sQuestionHTML);
                }

                List<Pair<QuestionType, Element>> lValidQuestions = new ArrayList<>();
                int iAnsSelected = 0;

                // Start checking second row for responses
                for (int i=1; i<iPrimaryQuestionBoxStackCount; i++) {
                    Elements elsQuestionAnswersLayer = elsPrimaryQuestionBoxStack.get(i).select("td");
                    int iAnsCellCount = elsQuestionAnswersLayer.size();

                    if ((iAnsCellCount % 2) != 0) {
                        throw new QuestionFormatException("Invalid question format [Q" + (nextQuestionIndex + 1) + "]");
                    }

                    for (int j=0; j<iAnsCellCount; j+=2) {
                        // Increment correct answer counter to determine question type
                        iAnsSelected += elsQuestionAnswersLayer.get(j).select("p > strong").size();

                        if (elsQuestionAnswersLayer.get(j+1).select("p img").size() > 1) {
                            // Blackboard only allows 1 image attachment per response...
                            throw new QuestionFormatException("Response fields cannot contain more than one image attachment [Q" + (nextQuestionIndex + 1) + "]");
                        }
                    }
                }

                if (iAnsSelected == 0) {
                    throw new QuestionFormatException("No answer selected [Q" + (nextQuestionIndex + 1) + "]");
                } else if (iAnsSelected == 1) {
                    lValidQuestions.add(new Pair<QuestionType, Element>(QuestionType.MULTIPLE_CHOICE, primaryQuestionBox));
                } else {
                    lValidQuestions.add(new Pair<QuestionType, Element>(QuestionType.MULTIPLE_ANSWER, primaryQuestionBox));
                }
                
                return lValidQuestions;
            } else {
                // First row does not span multiple columns so it is likely a nested question

                List<Pair<QuestionType, Element>> lValidQuestions = new ArrayList<>();
                String sCommonInstruction = elFirstCell.html();

                // This is a nested question group so the sub-questions should be in row i=1
                for (int i=1; i<iPrimaryQuestionBoxStackCount; i++) {
                    Elements elsSubQuestionBoxes = elsPrimaryQuestionBoxStack.get(i).select("td > table");
                    int iSubQuestionCount = elsSubQuestionBoxes.size();

                    // Make sure nested questions are properly formatted
                    for (int j=0; j<iSubQuestionCount; j++) {
                        if (boolEnableQIdxRemover) {
                            Element elQuestionCell = elsSubQuestionBoxes.get(j).selectFirst("tr").selectFirst("td");
                            String sQuestionHTML = removeNumericalIndex(elQuestionCell.html());
                            elQuestionCell.empty();
                            elQuestionCell.append(sQuestionHTML);
                        }

                        Elements elSubQuestionBoxStack = elsSubQuestionBoxes.get(j).select("tr");
                        int iSubQuestionBoxStackCount = elSubQuestionBoxStack.size();

                        if (iSubQuestionBoxStackCount == 1) {
                            elsSubQuestionBoxes.get(j).selectFirst("tr").selectFirst("td").prepend(sCommonInstruction);
                            lValidQuestions.add(new Pair<QuestionType, Element>(QuestionType.FILE_UPLOAD, elsSubQuestionBoxes.get(j)));
                        } else {
                            elsSubQuestionBoxes.get(j).selectFirst("tr").selectFirst("td").prepend(sCommonInstruction);
                            int iSubAnsSelected = 0;

                            // Response choices for sub-question should begin on k=1 since k=0 is question text
                            for (int k=1; k<iSubQuestionBoxStackCount; k++) {
                                Elements elsSubQuestionAnswersLayer = elSubQuestionBoxStack.get(k).select("td");
                                int iAnsCellCount = elsSubQuestionAnswersLayer.size();

                                if ((iAnsCellCount % 2) != 0) {
                                    throw new QuestionFormatException("Invalid question format [Q" + (nextQuestionIndex + 1) + "." + (j + 1) + "]");
                                }

                                // Only need to check the label column so query every other cell
                                for (int l=0; l<iAnsCellCount; l+=2) {
                                    iSubAnsSelected += elsSubQuestionAnswersLayer.get(l).select("p > strong").size();
                                }
                            }

                            if (iSubAnsSelected == 0) {
                                throw new QuestionFormatException("No answer selected [Q" + (nextQuestionIndex + 1) + "." + (j + 1) + "]");
                            } else if (iSubAnsSelected == 1) {
                                lValidQuestions.add(new Pair<QuestionType, Element>(QuestionType.MULTIPLE_CHOICE, elsSubQuestionBoxes.get(j)));
                            } else {
                                lValidQuestions.add(new Pair<QuestionType, Element>(QuestionType.MULTIPLE_ANSWER, elsSubQuestionBoxes.get(j)));
                            }
                        }
                    }
                }

                return lValidQuestions;
            }
        }
    }

    public String processHTML(String html) {
        docHTMLfromWord = Jsoup.parse(html);
        Elements elsPrimaryQuestionBoxes = docHTMLfromWord.select("body > table");
        List<Pair<QuestionType, Element>> lValidQuestions;

        int iPrimaryQuestionBoxesFound = elsPrimaryQuestionBoxes.size();
        int iNextQuestionId = 0;

        for (int i=0; i<iPrimaryQuestionBoxesFound; i++) {
            try {
                lValidQuestions = validateQuestion(iNextQuestionId, elsPrimaryQuestionBoxes.get(i));
                addQuestions(iNextQuestionId, lValidQuestions);
                iNextQuestionId += lValidQuestions.size();
            } catch (QuestionFormatException qfe) {
                return qfe.getMessage();
            }
        }

        if (iPrimaryQuestionBoxesFound == 0) {
            return "No valid primary questions found in supplied Word document";
        } else if (iNextQuestionId != iQuestionCount) {
            return "DocxToBB found " + iNextQuestionId + " question(s), but you identified the presence of " + iQuestionCount + ". This discrepancy should be reconciled for an accurate point value per question calculation!";
        } else {
            return "";
        }
    }

    public void enableQuestionIndexRemover(boolean enabled) {
        boolEnableQIdxRemover = enabled;
    }

    public void setAdvancedOptions(Map<String, Boolean> advOps) {
        mAdvancedOps = advOps;
    }

    public void setImageManager(ImageManager imageHandler) {
        imImageMgr = imageHandler;
    }

    public void setExamDescription(String description) {
        veDescription.text(description);
    }

    public void setExamInstructions(String instructions) {
        veInstructions.text(instructions);
    }

    public void writeBBXML(FileOutputStream os) {
        vdQuestionXML.print(os);
    }
}