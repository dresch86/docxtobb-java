package org.ose.docxtobb;

import java.io.IOException;
import java.io.OutputStream;

import java.lang.StringBuffer;

import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

public class TextStackStream extends OutputStream {
    private VBox vbTextStack;
    private StringBuffer sbTextReceived;

    public TextStackStream() {
        sbTextReceived = new StringBuffer(100);
    }

    public void setVBoxDisplay(VBox vbox) {
        vbTextStack = vbox;
    }

    @Override
    public void write(int charCode) throws IOException {
        char cCharIn = (char) charCode;

        if (cCharIn == 0x0A) {
            Text txtLogEntry = new Text(sbTextReceived.toString());
            sbTextReceived.delete(0, sbTextReceived.length());

            Platform.runLater(() -> {
                txtLogEntry.wrappingWidthProperty().bind(vbTextStack.widthProperty());
                vbTextStack.getChildren().add(txtLogEntry);
            });
        } else {
            sbTextReceived.append(cCharIn);
        }
    }
}