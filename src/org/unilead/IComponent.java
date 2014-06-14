package org.unilead;

public interface IComponent {

	public abstract void initialize();

	public abstract void destroy();

	public abstract void start();

	public abstract void stop();

	public abstract void pause() throws Exception;

	public abstract void resume() throws Exception;

}