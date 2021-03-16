package esa.egscc.metrics.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import esa.egscc.metrics.api.Histogram;
import esa.egscc.metrics.api.Meter;
import esa.egscc.metrics.api.Snapshot;
import esa.egscc.metrics.api.Timer;

/**
 * A timer metric which aggregates timing durations and provides duration
 * statistics, plus throughput statistics via {@link Meter}.
 */
public class TimerImpl implements Timer {
	/**
	 * A timing context.
	 *
	 * @see TimerImpl#time()
	 */
	public static class Context implements Timer.Context {
		private final TimerImpl timer;
		private final Clock clock;
		private final long startTime;

		private Context(final TimerImpl timer, final Clock clock) {
			this.timer = timer;
			this.clock = clock;
			startTime = clock.getTick();
		}

		/**
		 * Updates the timer with the difference between current and start time. Call to
		 * this method will not reset the start time. Multiple calls result in multiple
		 * updates.
		 *
		 * @return the elapsed time in nanoseconds
		 */
		@Override
		public long stop() {
			final long elapsed = clock.getTick() - startTime;
			timer.update(elapsed, TimeUnit.NANOSECONDS);
			return elapsed;
		}

		/** Equivalent to calling {@link #stop()}. */
		@Override
		public void close() {
			stop();
		}
	}

	private final Meter meter;
	private final Histogram histogram;
	private final Clock clock;

	/**
	 * Creates a new {@link TimerImpl} using an
	 * {@link ExponentiallyDecayingReservoir} and the default {@link Clock}.
	 */
	public TimerImpl() {
		this(new ExponentiallyDecayingReservoir());
	}

	/**
	 * Creates a new {@link TimerImpl} that uses the given {@link Reservoir}.
	 *
	 * @param reservoir
	 *            the {@link Reservoir} implementation the timer should use
	 */
	public TimerImpl(final Reservoir reservoir) {
		this(reservoir, Clock.defaultClock());
	}

	/**
	 * Creates a new {@link TimerImpl} that uses the given {@link Reservoir} and
	 * {@link Clock}.
	 *
	 * @param reservoir
	 *            the {@link Reservoir} implementation the timer should use
	 * @param clock
	 *            the {@link Clock} implementation the timer should use
	 */
	TimerImpl(final Reservoir reservoir, final Clock clock) {
		meter = new MeterImpl(clock);
		this.clock = clock;
		histogram = new HistogramImpl(reservoir);
	}

	/**
	 * Adds a recorded duration.
	 *
	 * @param duration
	 *            the length of the duration
	 * @param unit
	 *            the scale unit of {@code duration}
	 */
	@Override
	public void update(final long duration, final TimeUnit unit) {
		update(unit.toNanos(duration));
	}

	/**
	 * Times and records the duration of event.
	 *
	 * @param event
	 *            a {@link Callable} whose {@link Callable#call()} method implements
	 *            a process whose duration should be timed
	 * @param <T>
	 *            the type of the value returned by {@code event}
	 * @return the value returned by {@code event}
	 * @throws Exception
	 *             if {@code event} throws an {@link Exception}
	 */
	@Override
	public <T> T time(final Callable<T> event) throws Exception {
		final long startTime = clock.getTick();
		try {
			return event.call();
		} finally {
			update(clock.getTick() - startTime);
		}
	}

	/**
	 * Times and records the duration of event.
	 *
	 * @param event
	 *            a {@link Runnable} whose {@link Runnable#run()} method implements
	 *            a process whose duration should be timed
	 */
	@Override
	public void time(final Runnable event) {
		final long startTime = clock.getTick();
		try {
			event.run();
		} finally {
			update(clock.getTick() - startTime);
		}
	}

	/**
	 * Returns a new {@link Context}.
	 *
	 * @return a new {@link Context}
	 * @see Context
	 */
	@Override
	public Context time() {
		return new Context(this, clock);
	}

	@Override
	public long getCount() {
		return histogram.getCount();
	}

	@Override
	public double getFifteenMinuteRate() {
		return meter.getFifteenMinuteRate();
	}

	@Override
	public double getFiveMinuteRate() {
		return meter.getFiveMinuteRate();
	}

	@Override
	public double getMeanRate() {
		return meter.getMeanRate();
	}

	@Override
	public double getOneMinuteRate() {
		return meter.getOneMinuteRate();
	}

	@Override
	public Snapshot getSnapshot() {
		return histogram.getSnapshot();
	}

	private void update(final long duration) {
		if (duration >= 0) {
			histogram.update(duration);
			meter.mark();
		}
	}
}
