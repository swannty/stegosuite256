package org.stegosuite.ui.cli;

import org.eclipse.swt.graphics.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.application.StegosuitePresenter;
import org.stegosuite.application.StegosuiteUI;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.ui.gui.ImageUtils;
import org.stegosuite.util.FileUtils;

import java.io.File;
import java.util.List;

public class Cli
		implements
		StegosuiteUI {

	private static final Logger LOG = LoggerFactory.getLogger(Cli.class);
	private StegosuitePresenter presenter;


	public int embed(File steganogram, String message, List<File> filesToBeEmbedded, String key, boolean noNoise, File outputPath) {
		if (!steganogram.exists()){
			LOG.error("Error: Image does not exist: {}", steganogram.getPath());
			return 1;
		}
		if (!steganogram.canRead()){
			LOG.error("Error: Can't read image: {}", steganogram.getPath());
			return 1;
		}
		if (!validImageFormat(steganogram.getPath()))
			return 1;
		if (message == null && filesToBeEmbedded == null) {
			LOG.error("Error: Nothing to embed. Use -m or -f to embed a message or files.");
			return 2;
		}

		pointFilter(noNoise);

		if (message != null) {
			presenter.addMessageToPayload(message);
		}

		if (filesToBeEmbedded != null) {
			for (File f : filesToBeEmbedded) {
				if (!f.exists()){
					LOG.error("Error: Can't find file: {}", f.getPath());
					return 1;
				}
				if (!f.canRead()){
					LOG.error("Error: Can't read file: {}", f.getPath());
					return 1;
				}
				presenter.addFileToPayload(f.getPath());
			}
		}
		File my_outputPath;
		if (outputPath == null){
			my_outputPath = new File(FileUtils.addFileNameSuffix(steganogram.getPath(), "_embed"));
		} else {
			my_outputPath = outputPath;
		}

		embed(key, my_outputPath);
		return 0;
	}

	private void embed(String key, File outputPath) {
		LOG.info("Embedding data...");
		presenter.embedNotifying(new EmbeddingProgress(), key, outputPath);
	}

	public int extract(File steganogram, String key, boolean noNoise ) {
		if (!steganogram.exists()){
			LOG.error("Error: Image does not exist: {}", steganogram.getPath());
			return 1;
		}
		if (!steganogram.canRead()){
			LOG.error("Error: Can't read image: {}", steganogram.getPath());
			return 1;
		}
		if (!validImageFormat(steganogram.getPath()))
			return 1;

		pointFilter(noNoise);
		extract(key);
		return 0;
	}

	private void extract(String key) {
		LOG.info("Extracting data...");
		presenter.extractNotifying(new EmbeddingProgress(), key);
	}

	public int capacity(File steganogram, boolean noNoise) {
		if (!steganogram.exists()){
			LOG.error("Error: Image does not exist: {}", steganogram.getPath());
			return 1;
		}
		if (!steganogram.canRead()){
			LOG.error("Error: Can't read image: {}", steganogram.getPath());
			return 1;
		}
		if (!validImageFormat(steganogram.getPath()))
			return 1;
		pointFilter(noNoise);
		int capacity = presenter.getEmbeddingCapacity();
		LOG.info("Capacity: {}", ImageUtils.formatSize(capacity));
		return 0;
	}

	private void pointFilter(Boolean noNoise) {
		if (noNoise) {
			presenter.setPointFilter(0);
		} else {
			presenter.setPointFilter(1);
		}
	}

	private boolean validImageFormat(String steganogramPath) {
		ImageFormat image = getImageFormat(steganogramPath);
		if (image == null) {
			showFormatNotSupportedError();
			return false;
		}
		presenter = new StegosuitePresenter(image, this);
		return true;
	}

	private ImageFormat getImageFormat(String steganogramPath) {
		try {
			return ImageFormat.getImageFormat(steganogramPath);
		} catch (SteganoImageException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void showFormatNotSupportedError() {
		LOG.error("Error: Currently only these file types are supported: {}", supportedFormats());
	}

	private String supportedFormats() {
		return String.join(", ", ImageFormat.getSupportedFormats());
	}

	@Override
	public void showEmbeddingError(SteganoEmbedException e) {
		LOG.info(e.getMessage());
	}

	@Override
	public void showExtractingError(SteganoExtractException e) {
		e.printStackTrace();
	}

	@Override
	public void extractingCompleted(String extractedMessage, List<String> filePaths, Visualizer visualizer,
			ImageData imageData) {
		LOG.info("Extracting completed");
		if (extractedMessage != null) {
			LOG.info("Extracted message: {}", extractedMessage);
		}
		if (!filePaths.isEmpty()) {
			for (String string : filePaths) {
				LOG.info("Extracted file saved to {}", string);
			}
		}
	}

	@Override
	public void embeddingCompleted(ImageFormat embeddedImage, String outputPath, Visualizer visualizer) {}

	@Override
	public void addPayloadFile(String filename, String extension, long fileSize) {}
}
