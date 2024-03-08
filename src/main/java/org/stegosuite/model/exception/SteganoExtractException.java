package org.stegosuite.model.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SteganoExtractException
        extends Exception {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SteganoExtractException.class);

    public SteganoExtractException(String message) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        printStackTrace(pw);
        LOG.debug(sw.toString());
        LOG.debug(message);

        LOG.error("Error: Could not extract.");
        System.exit(1);
    }

    public SteganoExtractException() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        printStackTrace(pw);
        LOG.debug(sw.toString());
        LOG.error("Error: Could not extract. Wrong key?");
        System.exit(1);
    }
}
