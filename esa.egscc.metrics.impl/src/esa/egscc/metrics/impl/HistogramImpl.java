package esa.egscc.metrics.impl;

import java.util.concurrent.atomic.LongAdder;

import esa.egscc.metrics.api.Histogram;
import esa.egscc.metrics.api.Snapshot;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately
 *      computing running variance</a>
 */
public class HistogramImpl implements Histogram {
	private final Reservoir reservoir;
	private final LongAdder count;

	/**
	 * Creates a new {@link HistogramImpl} with the given reservoir.
	 *
	 * @param reservoir
	 *            the reservoir to create a histogram from
	 */
	public HistogramImpl(final Reservoir reservoir) {
		this.reservoir = reservoir;
		count = new LongAdder();
	}

	/**
	 * Adds a recorded value.
	 *
	 * @param value
	 *            the length of the value
	 */
	@Override
	public void update(final int value) {
		update((long) value);
	}

	/**
	 * Adds a recorded value.
	 *
	 * @param value
	 *            the length of the value
	 */
	@Override
	public void update(final long value) {
		count.increment();
		reservoir.update(value);
	}

	/**
	 * Returns the number of values recorded.
	 *
	 * @return the number of values recorded
	 */
	@Override
	public long getCount() {
		return count.sum();
	}

	@Override
	public Snapshot getSnapshot() {
		return reservoir.getSnapshot();
	}
}
