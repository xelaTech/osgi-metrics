package esa.egscc.metrics.adapter.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import esa.egscc.metrics.adapter.config.MetricConfiguration;

/**
 * Handles and provides the configuration values of the Metrics.
 */
@Component(name = "MetricConfiguration", configurationPid = "esa.egscc.metrics.config", configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true, enabled = true)
public class MetricConfigurationImpl implements MetricConfiguration {
//	private final Logger logger = LoggerFactory.getLogger(MetricConfigurationImpl.class);

	private volatile Map<String, Object> properties = new HashMap<>();

	private static final String PUSHGATEWAY_BASE_URI = "http://localhost:9091/metrics";

	private enum ConfigKey {
		ENABLED, LOCAL_ENDPOINT_ENABLED, PUSH_URL, SKIP_CERTIFICATE_CHECK, PUSH_TO_CLOUD_INTERVAL, ENABLED_METRICS,
		DISABLED_METRICS, METRICS_METADATA
	}

	@Activate
	protected void activate(final Map<String, Object> properties) {
//		logger.info("Activating MetricConfiguration.");
		System.out.println("Activating MetricConfiguration.");

		extractConfiguration(properties);
	}

	private void extractConfiguration(final Map<String, Object> properties) {
		this.properties = new HashMap<>(properties);
		determineEnvironment();
	}

	private void determineEnvironment() {
		// TODO: Implement real environment check here.
		final boolean isProd = false;
		this.properties.put(ConfigKey.ENABLED.name(), !isProd);
		this.properties.put(ConfigKey.LOCAL_ENDPOINT_ENABLED.name(), !isProd);
	}

	@Override
	public boolean isEnabled() {
		return getBooleanProperty(ConfigKey.ENABLED);
	}

	@Override
	public boolean isLocalEndPointEnabled() {
		return getBooleanProperty(ConfigKey.LOCAL_ENDPOINT_ENABLED);
	}

	@Override
	public String getPushGatewayURL() {
		String url = getStringProperty(ConfigKey.PUSH_URL);
		if (url.isEmpty()) {
			url = PUSHGATEWAY_BASE_URI;
		}
		return url;
	}

	@Override
	public boolean isSkipVerifyCertificate() {
		return getBooleanProperty(ConfigKey.SKIP_CERTIFICATE_CHECK);
	}

	@Override
	public int getPushToCloudTimeInterval() {
		return getIntegerProperty(ConfigKey.PUSH_TO_CLOUD_INTERVAL);
	}

	@Override
	public Set<String> getEnabledMetrics() {
		String metrics = getStringProperty(ConfigKey.ENABLED_METRICS);
		return parseCommaSeparated(metrics);
	}

	@Override
	public Set<String> getDisabledMetrics() {
		String metrics = getStringProperty(ConfigKey.DISABLED_METRICS);
		return parseCommaSeparated(metrics);
	}

	@Override
	public Set<String> getMetadata() {
		String metadata = getStringProperty(ConfigKey.METRICS_METADATA);
		return parseCommaSeparated(metadata);
	}

	private Set<String> parseCommaSeparated(String metrics) {
		if (metrics.isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> metricsSet = new HashSet<>();
		Arrays.stream(metrics.split(",")).map(String::trim).forEach(metricsSet::add);
		return metricsSet;
	}

	private boolean getBooleanProperty(ConfigKey key) {
		String propertyName = key.name();
		if (properties.get(propertyName) == null) {
			return false;
		} else if (properties.get(propertyName).toString().equals("true")) {
			return true;
		}
		return false;
	}

	private String getStringProperty(ConfigKey key) {
		try {
			return String.valueOf(properties.get(key.name()));
		} catch (Exception e) {
			return "";
		}
	}

	private Integer getIntegerProperty(ConfigKey key) {
		Object value = properties.get(key.name());
		try {
			return Integer.parseInt(value.toString());
		} catch (Exception e) {
			return null;
		}
	}
}
