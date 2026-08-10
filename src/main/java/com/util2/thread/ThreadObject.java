package com.util2.thread;

import com.util2.thread.NotCreateException;

public abstract class ThreadObject {

	abstract public void create() throws NotCreateException;
	abstract public void destroy();
	abstract public void waitThread();
	abstract public void notifyThread();

} // end class ThreadObject
