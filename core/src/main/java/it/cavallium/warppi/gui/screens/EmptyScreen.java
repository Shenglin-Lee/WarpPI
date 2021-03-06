package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.gui.HistoryBehavior;

public class EmptyScreen extends Screen {

	public float endLoading;

	public EmptyScreen() {
		super();
		historyBehavior = HistoryBehavior.DONT_KEEP_IN_HISTORY;
	}

	@Override
	public void created() throws InterruptedException {
		endLoading = 0;
	}

	@Override
	public void initialized() throws InterruptedException {}

	@Override
	public void graphicInitialized() throws InterruptedException {}

	@Override
	public void render() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeRender(final float dt) {

	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	@Override
	public String getSessionTitle() {
		return "empty";
	}

}
