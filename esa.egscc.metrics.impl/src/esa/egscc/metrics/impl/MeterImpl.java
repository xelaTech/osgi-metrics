package esa.egscc.metrics.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import esa.egscc.metrics.api.Meter;

/**
 * A meter metric which measures mean throughput and one-, five-, and
 * fifteen-minute exponentially-weighted moving average throughputs.
 *
 * @see EWMA
 */
public class MeterImpl implements Meter {
	private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

	private final EWMA m1Rate = EWMA.oneMinuteEWMA();
	private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
	private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

	private final LongAdder count = new LongAdder();
	private final long startTime;
	private final AtomicLong lastTick;
	private final Clock clock;

	/**
	 * Creates a new {@link MeterImpl}.
	 */
	public MeterImpl() {
		this(Clock.defaultClock());
	}

	/**
	 * Creates a new {@link MeterImpl}.
	 *
	 * @param clock
	 *            the clock to use for the meter ticks
	 */
	public MeterImpl(final Clock clock) {
		this.clock = clock;
		startTime = this.clock.getTick();
		lastTick = new AtomicLong(startTime);
	}

	/**
	 * Mark the occurrence of an event.
	 */
	@Override
	public void mark() {
		mark(1);
	}

	/**
	 * Mark the occurrence of a given number of events.
	 *
	 * @param n
	 *            the number of events
	 */
	@Override
	public void mark(final long n) {
		tickIfNecessary();
		count.add(n);
		m1Rate.update(n);
		m5Rate.update(n);
		m15Rate.update(n);
	}

	private void tickIfNecessary() {
		final long oldTick = lastTick.get();
		final long newTick = clock.getTick();
		final long age = newTick - oldTick;
		if (age > TICK_INTERVAL) {
			final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
			if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
				final long requiredTicks = age / TICK_INTERVAL;
				for (long i = 0; i < requiredTicks; i++) {
					m1Rate.tick();
					m5Rate.tick();
					m15Rate.tick();
				}
			}
		}
	}

	@Override
	public long getCount() {
		return count.sum();
	}

	@Override
	public double getFifteenMinuteRate() {
		tickIfNecessary();
		return m15Rate.getRate(TimeUnit.SECONDS);
	}

	@Override
	public double getFiveMinuteRate() {
		tickIfNecessary();
		return m5Rate.getRate(TimeUnit.SECONDS);
	}

	@Override
	public double getMeanRate() {
		if (getCount() == 0) {
			return 0.0;
		} else {
			final double elapsed = clock.getTick() - startTime;
			return getCount() / elapsed * TimeUnit.SECONDS.toNanos(1);
		}
	}

	@Override
	public double getOneMinuteRate() {
		tickIfNecessary();
		return m1Rate.getRate(TimeUnit.SECONDS);
	}
}
