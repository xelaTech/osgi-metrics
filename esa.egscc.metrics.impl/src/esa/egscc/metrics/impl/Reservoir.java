package esa.egscc.metrics.impl;

import org.eclipse.microprofile.metrics.Sampling;

/**
 * A statistically representative reservoir of a data stream.
 */
public interface Reservoir extends Sampling {
	/**
	 * Returns the number of values recorded.
	 *
	 * @return the number of values recorded
	 */
	int size();

	/**
	 * Adds a new recorded value to the reservoir.
	 *
	 * @param value
	 *            a new recorded value
	 */
	void update(long value);
}
