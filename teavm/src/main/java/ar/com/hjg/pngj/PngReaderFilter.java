package ar.com.hjg.pngj;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ar.com.hjg.pngj.chunks.PngChunk;

/**
 * This class allows to use a simple PNG reader as an input filter, wrapping a
 * ChunkSeqReaderPng in callback mode.
 *
 * In this sample implementation, all IDAT chunks are skipped and the rest are
 * stored. An example of use, that lets us
 * grab the Metadata and let the pixels go towards a BufferedImage:
 *
 *
 * <pre class="code">
 * PngReaderFilter reader = new PngReaderFilter(new FileInputStream(&quot;image.png&quot;));
 * BufferedImage image1 = ImageIO.read(reader);
 * reader.readUntilEndAndClose(); // in case ImageIO.read() does not read the traling chunks (it happens)
 * System.out.println(reader.getChunksList());
 * </pre>
 *
 */
public class PngReaderFilter extends FilterInputStream {

	private final ChunkSeqReaderPng chunkseq;

	public PngReaderFilter(final InputStream arg0) {
		super(arg0);
		chunkseq = createChunkSequenceReader();
	}

	protected ChunkSeqReaderPng createChunkSequenceReader() {
		return new ChunkSeqReaderPng(true) {
			@Override
			public boolean shouldSkipContent(final int len, final String id) {
				return super.shouldSkipContent(len, id) || id.equals("IDAT");
			}

			@Override
			protected boolean shouldCheckCrc(final int len, final String id) {
				return false;
			}

			@Override
			protected void postProcessChunk(final ChunkReader chunkR) {
				super.postProcessChunk(chunkR);
				// System.out.println("processed chunk " + chunkR.getChunkRaw().id);
			}
		};
	}

	@Override
	public void close() throws IOException {
		super.close();
		chunkseq.close();
	}

	@Override
	public int read() throws IOException {
		final int r = super.read();
		if (r > 0)
			chunkseq.feedAll(new byte[] { (byte) r }, 0, 1);
		return r;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		final int res = super.read(b, off, len);
		if (res > 0)
			chunkseq.feedAll(b, off, res);
		return res;
	}

	@Override
	public int read(final byte[] b) throws IOException {
		final int res = super.read(b);
		if (res > 0)
			chunkseq.feedAll(b, 0, res);
		return res;
	}

	public void readUntilEndAndClose() throws IOException {
		final BufferedStreamFeeder br = new BufferedStreamFeeder(in);
		while (!chunkseq.isDone() && br.hasMoreToFeed())
			br.feed(chunkseq);
		close();
	}

	public List<PngChunk> getChunksList() {
		return chunkseq.getChunks();
	}

	public ChunkSeqReaderPng getChunkseq() {
		return chunkseq;
	}

}