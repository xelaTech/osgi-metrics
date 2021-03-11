package esa.egscc.metrics.adapter.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Timer;

import esa.egscc.metrics.adapter.exception.EmptyRegistryException;
import esa.egscc.metrics.adapter.exception.NoSuchMetricException;

public class Helper {

	private Helper() {
		throw new IllegalAccessError("Non-Instantiable");
	}

	public static Map<String, Metric> getMetricsAsMap(final MetricRegistry registry, final String metricName)
			throws NoSuchMetricException, EmptyRegistryException {
		final Map<String, Metric> metricMap = registry.getMetrics();
		final Map<String, Metric> returnMap = new HashMap<>();
		if (metricMap.isEmpty()) {
			throw new EmptyRegistryException();
		} else if (!metricMap.containsKey(metricName)) {
			throw new NoSuchMetricException();
		} else {
			returnMap.put(metricName, metricMap.get(metricName));
		}

		return returnMap;
	}

	public static Map<String, Metric> getMetricsAsMap(final MetricRegistry registry) throws EmptyRegistryException {
		final Map<String, Metric> metricMap = registry.getMetrics();
		if (metricMap.isEmpty()) {
			throw new EmptyRegistryException();
		}

		return metricMap;
	}

	public static Map<String, Metadata> getMetricsMetadataAsMap(final MetricRegistry registry)
			throws EmptyRegistryException {
		final Map<String, Metadata> metricMetadataMap = registry.getMetadata();
		if (metricMetadataMap.isEmpty()) {
			throw new EmptyRegistryException();
		}

		return metricMetadataMap;
	}

	public static Map<String, Metadata> getMetricsMetadataAsMap(final MetricRegistry registry, final String metric)
			throws EmptyRegistryException, NoSuchMetricException {
		final Map<String, Metadata> metricMetadataMap = registry.getMetadata();
		final Map<String, Metadata> returnMap = new HashMap<>();
		if (metricMetadataMap.isEmpty()) {
			throw new EmptyRegistryException();
		} else if (!metricMetadataMap.containsKey(metric)) {
			throw new NoSuchMetricException();
		} else {
			returnMap.put(metric, metricMetadataMap.get(metric));
		}
		return returnMap;
	}

	public static Map<String, Number> getTimerNumbers(final Timer timer) {
		final Map<String, Number> results = new HashMap<>();
		results.put(Constants.COUNT, timer.getCount());
		results.put(Constants.MEAN_RATE, timer.getMeanRate());
		results.put(Constants.ONE_MINUTE_RATE, timer.getOneMinuteRate());
		results.put(Constants.FIVE_MINUTE_RATE, timer.getFiveMinuteRate());
		results.put(Constants.FIFTEEN_MINUTE_RATE, timer.getFifteenMinuteRate());

		results.put(Constants.MAX, timer.getSnapshot().getMax());
		results.put(Constants.MEAN, timer.getSnapshot().getMean());
		results.put(Constants.MIN, timer.getSnapshot().getMin());

		results.put(Constants.STD_DEV, timer.getSnapshot().getStdDev());

		results.put(Constants.MEDIAN, timer.getSnapshot().getMedian());
		results.put(Constants.PERCENTILE_75TH, timer.getSnapshot().get75thPercentile());
		results.put(Constants.PERCENTILE_95TH, timer.getSnapshot().get95thPercentile());
		results.put(Constants.PERCENTILE_98TH, timer.getSnapshot().get98thPercentile());
		results.put(Constants.PERCENTILE_99TH, timer.getSnapshot().get99thPercentile());
		results.put(Constants.PERCENTILE_999TH, timer.getSnapshot().get999thPercentile());

		return results;
	}

	public static Map<String, Number> getHistogramNumbers(final Histogram histogram) {
		final Map<String, Number> results = new HashMap<>();
		results.put(Constants.COUNT, histogram.getCount());

		results.put(Constants.MAX, histogram.getSnapshot().getMax());
		results.put(Constants.MEAN, histogram.getSnapshot().getMean());
		results.put(Constants.MIN, histogram.getSnapshot().getMin());

		results.put(Constants.STD_DEV, histogram.getSnapshot().getStdDev());

		results.put(Constants.MEDIAN, histogram.getSnapshot().getMedian());
		results.put(Constants.PERCENTILE_75TH, histogram.getSnapshot().get75thPercentile());
		results.put(Constants.PERCENTILE_95TH, histogram.getSnapshot().get95thPercentile());
		results.put(Constants.PERCENTILE_98TH, histogram.getSnapshot().get98thPercentile());
		results.put(Constants.PERCENTILE_99TH, histogram.getSnapshot().get99thPercentile());
		results.put(Constants.PERCENTILE_999TH, histogram.getSnapshot().get999thPercentile());

		return results;
	}

	public static Map<String, Number> getMeterNumbers(final Meter meter) {
		final Map<String, Number> results = new HashMap<>();
		results.put(Constants.COUNT, meter.getCount());
		results.put(Constants.MEAN_RATE, meter.getMeanRate());
		results.put(Constants.ONE_MINUTE_RATE, meter.getOneMinuteRate());
		results.put(Constants.FIVE_MINUTE_RATE, meter.getFiveMinuteRate());
		results.put(Constants.FIFTEEN_MINUTE_RATE, meter.getFifteenMinuteRate());

		return results;
	}
}
