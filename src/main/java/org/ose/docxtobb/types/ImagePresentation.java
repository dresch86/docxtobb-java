package org.ose.docxtobb.types;

public enum ImagePresentation {
    INLINE("Inline"),
    ATTACHMENT("Attachment");

    private String sPresentation;

    private ImagePresentation(String presentation) {
        sPresentation = presentation;
    }
   
    @Override
    public String toString() {
        return sPresentation;
    } 
}