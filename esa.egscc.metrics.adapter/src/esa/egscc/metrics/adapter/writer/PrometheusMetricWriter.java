package esa.egscc.metrics.adapter.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;

import esa.egscc.metrics.adapter.exception.EmptyRegistryException;
import esa.egscc.metrics.adapter.exception.NoSuchRegistryException;
import esa.egscc.metrics.adapter.util.Constants;
import esa.egscc.metrics.adapter.util.Helper;
import esa.egscc.metrics.adapter.util.PrometheusBuilder;
import esa.egscc.metrics.api.Counter;
import esa.egscc.metrics.api.Gauge;
import esa.egscc.metrics.api.Histogram;
import esa.egscc.metrics.api.Metadata;
import esa.egscc.metrics.api.Meter;
import esa.egscc.metrics.api.Metric;
import esa.egscc.metrics.api.MetricRegistry;
import esa.egscc.metrics.api.MetricUnits;
import esa.egscc.metrics.api.Timer;

public class PrometheusMetricWriter {

	private final Writer writer;
	private final MetricRegistry registry;
	private final BiPredicate<String, String> filter;

	public PrometheusMetricWriter(final MetricRegistry registry, final Writer writer,
			final BiPredicate<String, String> filter) {
		Objects.requireNonNull(registry, "MetricRegistry must not be null.");
		Objects.requireNonNull(writer, "Writer must not be null.");

		this.writer = writer;
		this.registry = registry;
		this.filter = filter;
	}

	public void write() throws NoSuchRegistryException, EmptyRegistryException, IOException {
		final StringBuilder builder = new StringBuilder();
		writeMetricsAsPrometheus(builder);
		serialize(builder);
	}

	private void writeMetricsAsPrometheus(final StringBuilder builder) throws EmptyRegistryException {
		writeMetricMapAsPrometheus(builder, Helper.getMetricsAsMap(registry), Helper.getMetricsMetadataAsMap(registry));
	}

	private void writeMetricMapAsPrometheus(final StringBuilder builder, final Map<String, Metric> metricMap,
			final Map<String, Metadata> metricMetadataMap) {
		for (final Entry<String, Metric> entry : metricMap.entrySet()) {
			final String metricNamePrometheus = Constants.MANGER_NAME + entry.getKey();
			final Metric metric = entry.getValue();
			final String entryName = entry.getKey();

			final Metadata metricMetaData = metricMetadataMap.get(entryName);

			// Giving description to the metric.
			String description = "";
			if (metricMetaData.getDescription() != null && metricMetaData.getDescription().trim().isEmpty() == false) {
				description = metricMetaData.getDescription();
			}

			final String tags = metricMetaData.getTagsAsString();

			// Appending unit to the metric name.
			String unit = MetricUnits.NONE;
			if (metricMetaData.getUnit() != null && metricMetaData.getUnit().trim().isEmpty() == false) {
				unit = metricMetaData.getUnit();
			}

			// Unit determination/translation.
			double conversionFactor = 0;
			String appendUnit = null;

			switch (unit) {
			case MetricUnits.NONE:
				conversionFactor = Double.NaN;
				appendUnit = null;
				break;
			case MetricUnits.NANOSECONDS:
				conversionFactor = Constants.NANOSECONDCONVERSION;
				appendUnit = Constants.APPENDEDSECONDS;
				break;
			case MetricUnits.MICROSECONDS:
				conversionFactor = Constants.MICROSECONDCONVERSION;
				appendUnit = Constants.APPENDEDSECONDS;
				break;
			case MetricUnits.SECONDS:
				conversionFactor = Constants.SECONDCONVERSION;
				appendUnit = Constants.APPENDEDSECONDS;
				break;
			case MetricUnits.MINUTES:
				conversionFactor = Constants.MINUTECONVERSION;
				appendUnit = Constants.APPENDEDSECONDS;
				break;
			case MetricUnits.HOURS:
				conversionFactor = Constants.HOURCONVERSION;
				appendUnit = Constants.APPENDEDSECONDS;
				break;
			case MetricUnits.DAYS:
				conversionFactor = Constants.DAYCONVERSION;
				appendUnit = Constants.APPENDEDSECONDS;
				break;
			case MetricUnits.PERCENT:
				conversionFactor = Double.NaN;
				appendUnit = Constants.APPENDEDPERCENT;
				break;
			case MetricUnits.BYTES:
				conversionFactor = Double.NaN;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.KILOBYTES:
				conversionFactor = Constants.KILOBYTECONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.MEGABYTES:
				conversionFactor = Constants.MEGABYTECONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.GIGABYTES:
				conversionFactor = Constants.GIGABYTECONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.KILOBITS:
				conversionFactor = Constants.KILOBITCONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.MEGABITS:
				conversionFactor = Constants.MEGABITCONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.GIGABITS:
				conversionFactor = Constants.GIGABITCONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.KIBIBITS:
				conversionFactor = Constants.KIBIBITCONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.MEBIBITS:

				conversionFactor = Constants.MEBIBITCONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.GIBIBITS:
				conversionFactor = Constants.GIBIBITCONVERSION;
				appendUnit = Constants.APPENDEDBYTES;
				break;
			case MetricUnits.MILLISECONDS:
				conversionFactor = Constants.MILLISECONDCONVERSION;
				appendUnit = Constants.APPENDEDSECONDS;
				break;
			default:
				conversionFactor = Double.NaN;
				appendUnit = "_" + unit;
			}

			if (filter != null && !filter.test(entryName, entryName + appendUnit)) {
				continue;
			}

			if (Counter.class.isInstance(metric)) {
				PrometheusBuilder.buildCounter(builder, metricNamePrometheus, (Counter) metric, description, tags);
			} else if (Gauge.class.isInstance(metric)) {
				PrometheusBuilder.buildGauge(builder, metricNamePrometheus, (Gauge<?>) metric, description,
						conversionFactor, tags, appendUnit);
			} else if (Timer.class.isInstance(metric)) {
				PrometheusBuilder.buildTimer(builder, metricNamePrometheus, (Timer) metric, description, tags);
			} else if (Histogram.class.isInstance(metric)) {
				PrometheusBuilder.buildHistogram(builder, metricNamePrometheus, (Histogram) metric, description,
						conversionFactor, tags, appendUnit);
			} else if (Meter.class.isInstance(metric)) {
				PrometheusBuilder.buildMeter(builder, metricNamePrometheus, (Meter) metric, description, tags);
			}
		}
	}

	private void serialize(final StringBuilder builder) throws IOException {
		try {
			writer.write(builder.toString());
		} finally {
			writer.close();
		}
	}
}
