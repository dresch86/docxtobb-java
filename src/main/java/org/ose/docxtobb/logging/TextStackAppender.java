package org.ose.docxtobb.logging;

import java.io.Serializable;

import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.application.Platform;

import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;

@Plugin(
	  name = "TextStackAppender", 
	  category = Core.CATEGORY_NAME, 
	  elementType = Appender.ELEMENT_TYPE)
	public class TextStackAppender extends AbstractAppender {
		private static VBox vbLogStack;
	 
	    protected TextStackAppender(String name, Filter filter,
		Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
			super(name, filter, layout, ignoreExceptions, null);
	    }
	 
	    @PluginFactory
	    public static TextStackAppender createAppender(
          @PluginAttribute("name") String name, 
		  @PluginElement("Filter") final Filter filter,  
		  @PluginElement("Layout") Layout<? extends Serializable> layout) {
	        return new TextStackAppender(name, filter, layout, false);
	    }
	 
		public static void setStackBox(VBox stackBox) {
			vbLogStack = stackBox;
		}

	    @Override
	    public void append(LogEvent event) {
			if (vbLogStack != null) {
				Text txtLogEntry = new Text(event.getMessage().getFormattedMessage());

				switch (event.getLevel().getStandardLevel()) {
					case FATAL:
					txtLogEntry.setFill(Color.CRIMSON);
					break;
					case ERROR:
					txtLogEntry.setFill(Color.RED);
					break;
					case WARN:
					txtLogEntry.setFill(Color.ORANGE);
					break;
					case INFO:
					txtLogEntry.setFill(Color.BLUE);
					break;
					case TRACE:
					case DEBUG:
					txtLogEntry.setFill(Color.BLACK);
					break;
					case ALL:
					break;
					case OFF:
					return;
				}

				Platform.runLater(() -> {
					txtLogEntry.wrappingWidthProperty().bind(vbLogStack.widthProperty());
					vbLogStack.getChildren().add(txtLogEntry);
				});
			}
	    }
	}