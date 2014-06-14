package org.unilead;

import org.unilead.openrtb.common.api.Bid;

public class AdComponent implements IComponent {
	/* (non-Javadoc)
	 * @see org.unilead.IComponent#initialize()
	 */
	@Override
	public void initialize() {
		postTask = new PostTask();
		//postTask.initialize();
	}

	/**
	 * @see org.unilead.IComponent#destroy()
	 */
	@Override
	public void destroy() {
		postTask = null;
	}

	/* (non-Javadoc)
	 * @see org.unilead.IComponent#start()
	 */
	@Override
	public void start() {

	}

	/* (non-Javadoc)
	 * @see org.unilead.IComponent#stop()
	 */
	@Override
	public void stop() {
	}

	/* (non-Javadoc)
	 * @see org.unilead.IComponent#pause()
	 */
	@Override
	public void pause() throws Exception {
		throw new Exception("Not implemented.");
	}

	/* (non-Javadoc)
	 * @see org.unilead.IComponent#resume()
	 */
	@Override
	public void resume() throws Exception {
		throw new Exception("Not implemented.");
	}
	/**
	 * Do real work - Post BidRequest To Server 
	 */
	public void postBidRequest(){
		
		postTask.execute();
	}
	private PostTask postTask = null;
	private Bid bid;
}
