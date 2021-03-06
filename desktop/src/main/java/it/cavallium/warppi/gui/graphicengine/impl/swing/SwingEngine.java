package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class SwingEngine implements GraphicEngine {

	private SwingWindow INSTANCE;
	public SwingRenderer r;
	public volatile BufferedImage g;
	public volatile boolean initialized;
	public Semaphore exitSemaphore;

	@Override
	public void setTitle(final String title) {
		INSTANCE.setTitle(title);
	}

	@Override
	public void setResizable(final boolean r) {
		INSTANCE.setResizable(r);
		if (!r)
			INSTANCE.setUndecorated(true);
	}

	@Override
	public void setDisplayMode(final int ww, final int wh) {
		INSTANCE.setSize(ww, wh);
		r.size = new int[] { ww, wh };
		SwingRenderer.canvas2d = new int[ww * wh];
		g = new BufferedImage(ww, wh, BufferedImage.TYPE_INT_RGB);
	}

	@Override
	public void create() {
		create(null);
	}

	@Override
	public void create(final Runnable onInitialized) {
		r = new SwingRenderer();
		g = new BufferedImage(r.size[0], r.size[1], BufferedImage.TYPE_INT_RGB);
		initialized = false;
		exitSemaphore = new Semaphore(0);
		INSTANCE = new SwingWindow(this);
		setResizable(Engine.getPlatform().getSettings().isDebugEnabled());
		setDisplayMode((int) (StaticVars.screenSize[0] / StaticVars.windowZoom.getLastValue()), (int) (StaticVars.screenSize[1] / StaticVars.windowZoom.getLastValue()));
		INSTANCE.setVisible(true);
		initialized = true;
		if (onInitialized != null)
			onInitialized.run();
	}

	@Override
	public Observable<Integer[]> onResize() {
		return INSTANCE.onResize();
	}

	@Override
	public int getWidth() {
		return INSTANCE.getWWidth() - StaticVars.screenPos[0];
	}

	@Override
	public int getHeight() {
		return INSTANCE.getWHeight() - StaticVars.screenPos[1];
	}

	@Override
	public void destroy() {
		initialized = false;
		exitSemaphore.release();
		INSTANCE.setVisible(false);
		INSTANCE.dispose();
	}

	@Override
	public void start(final RenderingLoop d) {
		INSTANCE.setRenderingLoop(d);
		final Thread th = new Thread(() -> {
			try {
				double extratime = 0;
				while (initialized) {
					final long start = System.nanoTime();
					repaint();
					final long end = System.nanoTime();
					final double delta = end - start;
					if (extratime + delta < 50 * 1000d * 1000d) {
						Thread.sleep((long) Math.floor(50d - (extratime + delta) / 1000d / 1000d));
						extratime = 0;
					} else
						extratime += delta - 50d;
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		});
		th.setName("CPU rendering thread");
		th.setDaemon(true);
		th.start();
	}

	@Deprecated()
	public void refresh() {
		if (Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen() == null || Engine.INSTANCE.getHardwareDevice().getDisplayManager().error != null && Engine.INSTANCE.getHardwareDevice().getDisplayManager().error.length() > 0 || Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen() == null || Engine.INSTANCE.getHardwareDevice().getDisplayManager().getScreen().mustBeRefreshed())
			INSTANCE.c.paintImmediately(0, 0, getWidth(), getHeight());
	}

	@Override
	public void repaint() {
		INSTANCE.c.repaint();
	}

	public abstract class Startable {
		public Startable() {
			force = false;
		}

		public Startable(final boolean force) {
			this.force = force;
		}

		public boolean force = false;

		public abstract void run();
	}

	@Override
	public int[] getSize() {
		return r.size;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public SwingRenderer getRenderer() {
		return r;
	}

	@Override
	public BinaryFont loadFont(final String fontName) throws IOException {
		return new SwingFont(fontName);
	}

	@Override
	public BinaryFont loadFont(final String path, final String fontName) throws IOException {
		return new SwingFont(path, fontName);
	}

	@Override
	public Skin loadSkin(final String file) throws IOException {
		return new SwingSkin(file);
	}

	@Override
	public void waitForExit() {
		try {
			exitSemaphore.acquire();
		} catch (final InterruptedException e) {}
	}

	@Override
	public boolean isSupported() {
		if (StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isCPUEngineForced() == false)
			return false;
		return (GraphicsEnvironment.isHeadless()) == false;
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}

	public void setAlphaChanged(final boolean val) {
		INSTANCE.setAlphaChanged(val);
	}

	public void setShiftChanged(final boolean val) {
		INSTANCE.setShiftChanged(val);
	}
}
