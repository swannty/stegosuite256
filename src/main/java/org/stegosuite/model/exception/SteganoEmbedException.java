package org.stegosuite.model.exception;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SteganoEmbedException
		extends Exception {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SteganoExtractException.class);

	public SteganoEmbedException(String message) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

		if (root.getLevel() == Level.DEBUG) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			printStackTrace(pw);
			LOG.error(sw.toString());
		}
		LOG.error(message);
		LOG.error("Error: Could not embed.");
		System.exit(1);
	}
}
