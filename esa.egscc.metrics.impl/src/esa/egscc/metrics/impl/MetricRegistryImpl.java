package esa.egscc.metrics.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Timer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(name = "MetricRegistry", immediate = true, service = MetricRegistry.class)
public final class MetricRegistryImpl extends MetricRegistry {
	private static final String FILTER_NON_NULL = "Filter cannot be null";

	private ConcurrentMap<String, Metric> metrics;
	private ConcurrentMap<String, Metadata> metadata;

	@Activate
	protected void activate() {
		System.out.println("Activating MetricRegistry.");

		metrics = new ConcurrentHashMap<>();
		metadata = new ConcurrentHashMap<>();
	}

	@Override
	public <T extends Metric> T register(final String name, final T metric) {
		return register(name, metric, new Metadata(name, MetricType.from(metric.getClass())));
	}

	@Override
	public <T extends Metric> T register(final Metadata metadata, final T metric) {
		return register(metadata.getName(), metric, metadata);
	}

	@Override
	public <T extends Metric> T register(final String name, final T metric, final Metadata metadata) {
		requireNonNull(name, "Metric name cannot be null");
		requireNonNull(metric, "Metric instance cannot be null");
		requireNonNull(metadata, "Metric metadata cannot be null");

		System.out.println("Registering metric " + name);

		final Metric existing = metrics.putIfAbsent(name, metric);
		// Create copy of the metadata object so it can't be changed after its
		// registered
		final Metadata metadataCopy = new Metadata(metadata.getName(), metadata.getDisplayName(),
				metadata.getDescription(), metadata.getTypeRaw(), metadata.getUnit());
		for (final String tag : metadata.getTags().keySet()) {
			metadataCopy.getTags().put(tag, metadata.getTags().get(tag));
		}

		this.metadata.putIfAbsent(name, metadataCopy);

		if (existing != null) {
			System.out.println("Metric " + name + " exists already.");
		}

		return metric;
	}

	@Override
	public Counter counter(final String name) {
		requireNonNull(name, "Counter name cannot be null");
		return this.counter(new Metadata(name, MetricType.COUNTER));
	}

	@Override
	public Counter counter(final Metadata metadata) {
		requireNonNull(metadata, "Counter metadata cannot be null");
		return getOrAdd(metadata, MetricBuilder.COUNTERS);
	}

	@Override
	public Histogram histogram(final String name) {
		requireNonNull(name, "Histogram name cannot be null");
		return this.histogram(new Metadata(name, MetricType.HISTOGRAM));
	}

	@Override
	public Histogram histogram(final Metadata metadata) {
		requireNonNull(metadata, "Histogram metadata cannot be null");
		return getOrAdd(metadata, MetricBuilder.HISTOGRAMS);
	}

	@Override
	public Meter meter(final String name) {
		requireNonNull(name, "Meter name cannot be null");
		return this.meter(new Metadata(name, MetricType.METERED));
	}

	@Override
	public Meter meter(final Metadata metadata) {
		requireNonNull(metadata, "Meter metadata cannot be null");
		return getOrAdd(metadata, MetricBuilder.METERS);
	}

	@Override
	public Timer timer(final String name) {
		requireNonNull(name, "Timer name cannot be null");
		return timer(new Metadata(name, MetricType.TIMER));
	}

	@Override
	public Timer timer(final Metadata metadata) {
		requireNonNull(metadata, "Timer metadata cannot be null");
		return getOrAdd(metadata, MetricBuilder.TIMERS);
	}

	@Override
	public boolean remove(final String name) {
		requireNonNull(name, "Metric name cannot be null");
		final Metric metric = metrics.remove(name);
		metadata.remove(name);
		return metric != null;
	}

	@Override
	public void removeMatching(final MetricFilter filter) {
		requireNonNull(filter, FILTER_NON_NULL);
		Iterator<Map.Entry<String, Metric>> iterator = metrics.entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<String, Metric> entry = iterator.next();
			if (filter.matches(entry.getKey(), entry.getValue())) {
				metadata.remove(entry.getKey());
				iterator.remove();
			}
		}
	}

	@Override
	public SortedSet<String> getNames() {
		return (SortedSet<String>) Collections.unmodifiableSortedSet((SortedSet<?>) metrics.keySet());
	}

	@Override
	public SortedMap<String, Gauge> getGauges() {
		return getGauges(MetricFilter.ALL);
	}

	@Override
	public SortedMap<String, Gauge> getGauges(final MetricFilter filter) {
		requireNonNull(filter, FILTER_NON_NULL);
		return getMetrics(Gauge.class, filter);
	}

	@Override
	public SortedMap<String, Counter> getCounters() {
		return getCounters(MetricFilter.ALL);
	}

	@Override
	public SortedMap<String, Counter> getCounters(final MetricFilter filter) {
		requireNonNull(filter, FILTER_NON_NULL);
		return getMetrics(Counter.class, filter);
	}

	@Override
	public SortedMap<String, Histogram> getHistograms() {
		return getHistograms(MetricFilter.ALL);
	}

	@Override
	public SortedMap<String, Histogram> getHistograms(final MetricFilter filter) {
		requireNonNull(filter, FILTER_NON_NULL);
		return getMetrics(Histogram.class, filter);
	}

	@Override
	public SortedMap<String, Meter> getMeters() {
		return getMeters(MetricFilter.ALL);
	}

	@Override
	public SortedMap<String, Meter> getMeters(final MetricFilter filter) {
		requireNonNull(filter, FILTER_NON_NULL);
		return getMetrics(Meter.class, filter);
	}

	@Override
	public SortedMap<String, Timer> getTimers() {
		return getTimers(MetricFilter.ALL);
	}

	@Override
	public SortedMap<String, Timer> getTimers(final MetricFilter filter) {
		requireNonNull(filter, FILTER_NON_NULL);
		return getMetrics(Timer.class, filter);
	}

	@SuppressWarnings("unchecked")
	private <T extends Metric> T getOrAdd(final Metadata metadata, final MetricBuilder<T> builder) {
		final Metric metric = metrics.get(metadata.getName());
		if (builder.isInstance(metric)) {
			return (T) metric;
		} else if (metric == null) {
			try {
				return register(metadata.getName(), builder.newMetric(), metadata);
			} catch (final IllegalArgumentException e) {
				final Metric added = metrics.get(metadata.getName());
				if (builder.isInstance(added)) {
					return (T) added;
				}
			}
		}
		throw new IllegalArgumentException(metadata.getName() + " is already used for a different type of metric");
	}

	@SuppressWarnings("unchecked")
	private <T extends Metric> SortedMap<String, T> getMetrics(final Class<T> clazz, final MetricFilter filter) {
		final TreeMap<String, T> timers = new TreeMap<>();
		for (final Map.Entry<String, Metric> entry : metrics.entrySet()) {
			if (clazz.isInstance(entry.getValue()) && filter.matches(entry.getKey(), entry.getValue())) {
				timers.put(entry.getKey(), (T) entry.getValue());
			}
		}

		return (SortedMap<String, T>) Collections.unmodifiableMap(timers);
	}

	@Override
	public Map<String, Metric> getMetrics() {
		return Collections.unmodifiableMap(metrics);
	}

	@Override
	public Map<String, Metadata> getMetadata() {
		return Collections.unmodifiableMap(metadata);
	}

	public Metadata getMetadata(final String name) {
		return metadata.get(name);
	}

	/**
	 * A quick and easy way of capturing the notion of default metrics.
	 */
	private interface MetricBuilder<T extends Metric> {
		MetricBuilder<Counter> COUNTERS = new MetricBuilder<Counter>() {
			@Override
			public Counter newMetric() {
				return new CounterImpl();
			}

			@Override
			public boolean isInstance(final Metric metric) {
				return Counter.class.isInstance(metric);
			}
		};

		MetricBuilder<Histogram> HISTOGRAMS = new MetricBuilder<Histogram>() {
			@Override
			public Histogram newMetric() {
				return new HistogramImpl(new ExponentiallyDecayingReservoir());
			}

			@Override
			public boolean isInstance(final Metric metric) {
				return Histogram.class.isInstance(metric);
			}
		};

		MetricBuilder<Meter> METERS = new MetricBuilder<Meter>() {
			@Override
			public Meter newMetric() {
				return new MeterImpl();
			}

			@Override
			public boolean isInstance(final Metric metric) {
				return Meter.class.isInstance(metric);
			}
		};

		MetricBuilder<Timer> TIMERS = new MetricBuilder<Timer>() {
			@Override
			public Timer newMetric() {
				return new TimerImpl();
			}

			@Override
			public boolean isInstance(final Metric metric) {
				return Timer.class.isInstance(metric);
			}
		};

		T newMetric();

		boolean isInstance(Metric metric);
	}

}
