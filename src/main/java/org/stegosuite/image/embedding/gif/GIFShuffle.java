package org.stegosuite.image.embedding.gif;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.GIFImage;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoKeyException;
import org.stegosuite.model.payload.Payload;
import org.stegosuite.model.payload.PayloadEmbedder;
import org.stegosuite.model.payload.PayloadExtractor;
import org.stegosuite.util.ByteUtils;
import org.stegosuite.util.ColorDistance;
import org.stegosuite.util.CryptoUtils;

/**
 * GIFShuffle embedding and extracting procedures. Source: http://www.darkside.com.au/gifshuffle/
 *
 */
public class GIFShuffle
		extends EmbeddingMethod<GIFImage> {

	private static final Logger LOG = LoggerFactory.getLogger(GIFShuffle.class);
	
	public GIFShuffle(GIFImage image, PointFilter<GIFImage> pointFilter) {
		super(image, pointFilter);
	}

	private static final ColorDistance DISTANCE = ColorDistance.CIEDE_2000;

	/**
	 * Capacity in bits: sum(log2(n)) for n=2..colorTable.size()
	 */
	@Override
	protected int doCapacity(GIFImage image) {
		int numColors = image.getColorTable().size();
		double sum = IntStream.range(2, numColors + 1).mapToDouble(i -> Math.log(i) / Math.log(2)).sum();
		LOG.debug("Capacity: {}",(int) (sum / 8) - 1);
		return (int) (sum / 8) - 1;
	}

	@Override
	protected void doEmbed(GIFImage image, Payload payload, EmbeddingProgress progress)
			throws SteganoEmbedException {

		List<Color> originalTable = image.getColorTable();
		List<Color> newTable = new ArrayList<>(originalTable);
		List<Color> randomTable = image.getSortedColorTable(DISTANCE);
		
		LOG.debug("Array: {}",Arrays.toString(originalTable.toArray()));
		LOG.debug("Array: {}",Arrays.toString(randomTable.toArray()));
		
		Collections.shuffle(randomTable, CryptoUtils.seededRandom(payload.getSteganoPassword()));

//		LOG.debug("Array: {}",Arrays.toString(randomTable.toArray()));
		
		LOG.debug("origtable size: {}",originalTable.size());
		LOG.debug("randomtable size: {}",randomTable.size());
		
		// Prepend 1 to the payload so that leading 0 bytes are not cut off
		PayloadEmbedder embedder = new PayloadEmbedder(payload, this.capacity());
		BigInteger numPayload = new BigInteger(ByteUtils.concat(new byte[] { 1 }, embedder.getPayloadBytes()));

		for (int i = 0; i < originalTable.size(); i++) {
			int pos = numPayload.mod(BigInteger.valueOf(i + 1)).intValue();
			numPayload = numPayload.divide(BigInteger.valueOf(i + 1));
			for (int j = i; j > pos; j--) {
				newTable.set(j, newTable.get(j - 1));
			}
			newTable.set(pos, randomTable.get(originalTable.size() - i - 1));
			progress.progressUpdate(i + 1, originalTable.size());
		}

		// Update the color table and pixels according to the new color table
		int[] newPixels = Arrays.stream(image.getPixels()) //
				.map(pixel -> newTable.indexOf(originalTable.get(pixel))).toArray();
		image.setPixels(newPixels);
		image.setColorTable(newTable);
	}

	@Override
	protected void doExtract(GIFImage image, Payload payload, EmbeddingProgress progress)
			throws SteganoExtractException {
		List<Color> table = image.getColorTable();
		List<Color> randomTable = image.getSortedColorTable(DISTANCE);
				
		
		LOG.debug("Array: {}",Arrays.toString(randomTable.toArray()));
		
		Collections.shuffle(randomTable, CryptoUtils.seededRandom(payload.getSteganoPassword()));
		
		
//		LOG.debug("Array: {}",Arrays.toString(randomTable.toArray()));
		
		
		Map<Color, Integer> positions = IntStream.range(0, table.size()).boxed()
				.collect(Collectors.toMap(i -> table.get(i), i -> i));

		BigInteger numPayload = BigInteger.ZERO;

		for (int i = 0; i < table.size() - 1; i++) {
			int pos = positions.get(randomTable.get(i));

			numPayload = numPayload.multiply(BigInteger.valueOf(table.size() - i));
			numPayload = numPayload.add(BigInteger.valueOf(pos));

			for (int j = i + 1; j < table.size(); j++) {
				Color color = randomTable.get(j);
				if (positions.get(color) > pos) {
					positions.put(color, positions.get(color) - 1);
				}
			}

			progress.progressUpdate(i + 2, table.size());
		}

		// We skip the 1st byte because it's the 1 we prepended during embedding
		int i = 1;
		byte[] payloadBytes = numPayload.toByteArray();
		PayloadExtractor extractor = new PayloadExtractor(payload);
		while (!extractor.finished() && i < payloadBytes.length) {
			extractor.processByte(payloadBytes[i++]);
		}

		// If the extractor still expects data at this point, we extracted
		// the wrong payload size due to wrong stego password
		if (!extractor.finished()) {
			throw new SteganoKeyException();
		}
	}
}
