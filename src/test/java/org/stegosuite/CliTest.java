package org.stegosuite;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.stegosuite.Resources.pathOf;

public class CliTest {

	@After
	public void tearDown() throws Exception {
		Resources.delete("snow_embed.bmp");
	}

	@Test
	public void testEmbedAndExtract() throws Exception {
		// TODO: Remove temporal coupling and separate in two tests
		testEmbed();
		testExtract();
	}

	private void testEmbed() {
		String imagePath = pathOf("snow.bmp");
		String key = "password";
		String message = "message";
		String[] args = getEmbedCommand(imagePath, key, message);

		Stegosuite.main(args);

		assertTrue(new File(pathOf("snow_embed.bmp")).exists());
	}

	private void testExtract() {
		String imagePath = pathOf("snow_embed.bmp");
		String key = "password";
		String[] args = getExtractCommand(imagePath, key);

		Stegosuite.main(args);
		// TODO: Assert something (Until now, the only side-effect is the logging)
	}

	private String[] getEmbedCommand(String imagePath, String key, String message) {
		String command = String.format("embed %s -k %s -m %s",
				imagePath, key, message);
		return command.split(" ");
	}

	private String[] getExtractCommand(String imagePath, String key) {
		String command = String.format("extract %s -k %s",
				imagePath, key);
		return command.split(" ");
	}
}
