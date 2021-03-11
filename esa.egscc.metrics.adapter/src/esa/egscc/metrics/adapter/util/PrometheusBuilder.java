package esa.egscc.metrics.adapter.util;

import java.text.DecimalFormat;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Counting;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metered;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Sampling;
import org.eclipse.microprofile.metrics.Timer;

public class PrometheusBuilder {

	private static DecimalFormat numberFormat = new DecimalFormat( "0.###########");

    private PrometheusBuilder() {
		throw new IllegalAccessError("Non-Instantiable");
	}

	public static void buildGauge(final StringBuilder builder, final String name, final Gauge<?> gauge,
	        final String description, final Double conversionFactor, final String tags, final String appendUnit) {
		// Skip non number values
		Number gaugeValNumber = null;
		Object gaugeValue = null;
		try {
			gaugeValue = gauge.getValue();
		} catch (final RuntimeException e) {
			// The forwarding gauge is likely unloaded. A warning has already been emitted
			return;
		}
		if (!Number.class.isInstance(gaugeValue)) {
			return;
		}
		gaugeValNumber = (Number) gaugeValue;
		if (!Double.isNaN(conversionFactor)) {
	        gaugeValNumber = gaugeValNumber.doubleValue() * conversionFactor;
		}
		getPromTypeLine(builder, name, "gauge", appendUnit);
		getPromHelpLine(builder, name, description, appendUnit);
		getPromValueLine(builder, name, gaugeValNumber, tags, appendUnit);
	}

	public static void buildCounter(final StringBuilder builder, final String name, final Counter counter,
	        final String description, final String tags) {
		getPromTypeLine(builder, name, "counter");
		getPromHelpLine(builder, name, description);
		getPromValueLine(builder, name, counter.getCount(), tags);
	}

	public static void buildTimer(final StringBuilder builder, final String name, final Timer timer,
	        final String description, final String tags) {
		buildMetered(builder, name, timer, description, tags);
		final double conversionFactor = Constants.NANOSECONDCONVERSION;
		// Build Histogram
		buildSampling(builder, name, timer, description, conversionFactor, tags, Constants.APPENDEDSECONDS);
	}

	public static void buildHistogram(final StringBuilder builder, final String name, final Histogram histogram,
	        final String description, final Double conversionFactor, final String tags, final String appendUnit) {
		// Build Histogram
		buildSampling(builder, name, histogram, description, conversionFactor, tags, appendUnit);
	}

	public static void buildMeter(final StringBuilder builder, final String name, final Meter meter,
	        final String description, final String tags) {
		buildCounting(builder, name, meter, description, tags);
		buildMetered(builder, name, meter, description, tags);
	}

	private static void buildSampling(final StringBuilder builder, final String name, final Sampling sampling,
	        final String description, final Double conversionFactor, final String tags, final String appendUnit) {

		double meanVal = sampling.getSnapshot().getMean();
		double maxVal = sampling.getSnapshot().getMax();
		double minVal = sampling.getSnapshot().getMin();
		double stdDevVal = sampling.getSnapshot().getStdDev();
		double medianVal = sampling.getSnapshot().getMedian();
		double percentile75th = sampling.getSnapshot().get75thPercentile();
		double percentile95th = sampling.getSnapshot().get95thPercentile();
		double percentile98th = sampling.getSnapshot().get98thPercentile();
		double percentile99th = sampling.getSnapshot().get99thPercentile();
		double percentile999th = sampling.getSnapshot().get999thPercentile();

		if (!Double.isNaN(conversionFactor)) {
			meanVal = sampling.getSnapshot().getMean() * conversionFactor;
			maxVal = sampling.getSnapshot().getMax() * conversionFactor;
			minVal = sampling.getSnapshot().getMin() * conversionFactor;
			stdDevVal = sampling.getSnapshot().getStdDev() * conversionFactor;
			medianVal = sampling.getSnapshot().getMedian() * conversionFactor;
			percentile75th = sampling.getSnapshot().get75thPercentile() * conversionFactor;
			percentile95th = sampling.getSnapshot().get95thPercentile() * conversionFactor;
			percentile98th = sampling.getSnapshot().get98thPercentile() * conversionFactor;
			percentile99th = sampling.getSnapshot().get99thPercentile() * conversionFactor;
			percentile999th = sampling.getSnapshot().get999thPercentile() * conversionFactor;
		}

		String lineName = name + "_mean";
		getPromTypeLine(builder, lineName, "gauge", appendUnit);
		getPromValueLine(builder, lineName, meanVal, tags, appendUnit);
		lineName = name + "_max";
		getPromTypeLine(builder, lineName, "gauge", appendUnit);
		getPromValueLine(builder, lineName, maxVal, tags, appendUnit);
		lineName = name + "_min";
		getPromTypeLine(builder, lineName, "gauge", appendUnit);
		getPromValueLine(builder, lineName, minVal, tags, appendUnit);
		lineName = name + "_stddev";
		getPromTypeLine(builder, lineName, "gauge", appendUnit);
		getPromValueLine(builder, lineName, stdDevVal, tags, appendUnit);

		getPromTypeLine(builder, name, "summary", appendUnit);
		getPromHelpLine(builder, name, description, appendUnit);
		if (Counting.class.isInstance(sampling)) {
			getPromValueLine(builder, name, ((Counting) sampling).getCount(), tags,
			        appendUnit == null ? "_count" : appendUnit + "_count");
		}
		getPromValueLine(builder, name, medianVal, tags, new Tag(Constants.QUANTILE, "0.5"), appendUnit);
		getPromValueLine(builder, name, percentile75th, tags, new Tag(Constants.QUANTILE, "0.75"), appendUnit);
		getPromValueLine(builder, name, percentile95th, tags, new Tag(Constants.QUANTILE, "0.95"), appendUnit);
		getPromValueLine(builder, name, percentile98th, tags, new Tag(Constants.QUANTILE, "0.98"), appendUnit);
		getPromValueLine(builder, name, percentile99th, tags, new Tag(Constants.QUANTILE, "0.99"), appendUnit);
		getPromValueLine(builder, name, percentile999th, tags, new Tag(Constants.QUANTILE, "0.999"), appendUnit);
	}

	private static void buildCounting(final StringBuilder builder, final String name, final Counting counting,
	        final String description, final String tags) {
		final String lineName = name + "_total";
		getPromTypeLine(builder, lineName, "counter");
		getPromHelpLine(builder, lineName, description);
		getPromValueLine(builder, lineName, counting.getCount(), tags);
	}

	private static void buildMetered(final StringBuilder builder, final String name, final Metered metered,
	        final String description, final String tags) {
		String lineName = name + "_rate_" + MetricUnits.PER_SECOND.toString();
		getPromTypeLine(builder, lineName, "gauge");
		getPromValueLine(builder, lineName, metered.getMeanRate(), tags);

		lineName = name + "_one_min_rate_" + MetricUnits.PER_SECOND.toString();
		getPromTypeLine(builder, lineName, "gauge");
		getPromValueLine(builder, lineName, metered.getOneMinuteRate(), tags);

		lineName = name + "_five_min_rate_" + MetricUnits.PER_SECOND.toString();
		getPromTypeLine(builder, lineName, "gauge");
		getPromValueLine(builder, lineName, metered.getFiveMinuteRate(), tags);

		lineName = name + "_fifteen_min_rate_" + MetricUnits.PER_SECOND.toString();
		getPromTypeLine(builder, lineName, "gauge");
		getPromValueLine(builder, lineName, metered.getFifteenMinuteRate(), tags);
	}

	private static void getPromValueLine(final StringBuilder builder, final String name, final Number value,
	        final String tags) {
		getPromValueLine(builder, name, value, tags, null);
	}

	private static void getPromValueLine(final StringBuilder builder, final String name, final Number value,
	        String tags, final Tag quantile, final String appendUnit) {

		if (tags == null || tags.isEmpty()) {
			tags = quantile.getKey() + "=\"" + quantile.getValue() + "\"";
		} else {
			tags = tags + "," + quantile.getKey() + "=\"" + quantile.getValue() + "\"";
		}
		getPromValueLine(builder, name, value, tags, appendUnit);
	}

	private static void getPromValueLine(final StringBuilder builder, final String name, final Number value,
	        final String tags, final String appendUnit) {

		final String metricName = getPrometheusMetricName(name);

		builder.append(metricName);

		if (appendUnit != null) {
			builder.append(appendUnit);
		}

		if (tags != null && tags.length() > 0) {
			builder.append("{").append(tags).append("}");
		}

		builder.append(" ").append(numberFormat.format(value)).append('\n');
	}

	private static void getPromHelpLine(final StringBuilder builder, final String name, final String description) {
		getPromHelpLine(builder, name, description, null);
	}

	private static void getPromHelpLine(final StringBuilder builder, final String name, final String description,
	        final String appendUnit) {
		final String metricName = getPrometheusMetricName(name);
		if (description != null && !description.isEmpty()) {
			builder.append("# HELP ").append(metricName);

			if (appendUnit != null) {
				builder.append(appendUnit);
			}
			builder.append(" ").append(description).append("\n");
		}
	}

	private static void getPromTypeLine(final StringBuilder builder, final String name, final String type) {
		getPromTypeLine(builder, name, type, null);
	}

	private static void getPromTypeLine(final StringBuilder builder, final String name, final String type,
	        final String appendUnit) {

		final String metricName = getPrometheusMetricName(name);
		builder.append("# TYPE ").append(metricName);
		if (appendUnit != null) {
			builder.append(appendUnit);
		}
		builder.append(" ").append(type).append("\n");
	}

	/*
	 * Create the Prometheus metric name by sanitizing some characters
	 */
	private static String getPrometheusMetricName(final String name) {
		String out = name.replaceAll("(?<!^|:)(\\p{Upper})(?=\\p{Lower})", "_$1");
		out = out.replaceAll("(?<=\\p{Lower})(\\p{Upper})", "_$1").toLowerCase();
		out = out.replaceAll("[-_.\\s]+", "_");
		out = out.replaceAll("^_*(.*?)_*$", "$1");

		return out;
	}
}
