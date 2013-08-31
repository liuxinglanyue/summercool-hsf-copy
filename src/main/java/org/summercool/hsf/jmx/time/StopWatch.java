package org.summercool.hsf.jmx.time;

import java.util.concurrent.TimeUnit;

/**
 */
public class StopWatch {
	private static enum State {
		NEW, STOPPED, RUNNING
	};

	private State state = State.NEW;
	long nanoTimeStart;
	long nanoTimeStop;

	/**
	 * Create a new Stopwatch, and start it. You may restart the watch by calling {@code start()}
	 */
	public StopWatch() {
		start();
	}

	/**
	 * Start or restart stopwatch
	 */
	public synchronized void start() {
		state = State.RUNNING;
		nanoTimeStart = System.nanoTime();
	}

	/**
	 * Stop (or re-stop) stopwatch
	 * 
	 * @return
	 */
	public synchronized void stop() {
		if (state != State.STOPPED) {
			nanoTimeStop = System.nanoTime();
			state = State.STOPPED;
		}
	}

	/**
	 * @return the time, in milliseconds that has elapsed so far.<p> Calling this method is identical to calling:
	 *         {@code elapsedMillis(TimeUnit.MILLISECONDS);}
	 */
	public synchronized long elapsedMillis() {
		return elapsed(TimeUnit.MILLISECONDS);
	}

	/**
	 * @param unit
	 *        The TimeUnit that should be reported
	 * @return the time, in milliseconds that has elapsed so far.<p> Calling this method is identical to calling:
	 *         {@code elapsedMillis(TimeUnit.MILLISECONDS);}
	 */
	public synchronized long elapsed(TimeUnit unit) {
		final long elapsed;
		switch (state) {
		case RUNNING:
			elapsed = System.nanoTime() - nanoTimeStart;
			break;

		case STOPPED:
			elapsed = nanoTimeStop - nanoTimeStart;
			break;

		default:
			throw new IllegalStateException("State: " + state.toString());
		}
		if (unit == null) {
			throw new NullPointerException("unit");
		}

		return unit.convert(elapsed, TimeUnit.NANOSECONDS);
	}

	public boolean isRunning() {
		return state == State.RUNNING;
	}

	@Override
	public String toString() {
		return "StopWatch " + state + " @ " + elapsed(TimeUnit.MILLISECONDS) + "ms";
	}
}
