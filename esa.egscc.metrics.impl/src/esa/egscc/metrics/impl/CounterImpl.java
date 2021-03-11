package esa.egscc.metrics.impl;

import java.util.concurrent.atomic.LongAdder;

import org.eclipse.microprofile.metrics.Counter;

/**
 * An incrementing and decrementing counter metric.
 */
public class CounterImpl implements Counter {
	private final LongAdder count;

	public CounterImpl() {
		count = new LongAdder();
	}

	@Override
	public void inc() {
		inc(1);
	}

	@Override
	public void inc(final long n) {
		count.add(n);
	}

	@Override
	public void dec() {
		dec(1);
	}

	@Override
	public void dec(final long n) {
		count.add(-n);
	}

	
	@Override
	public long getCount() {
		return count.sum();
	}
}
