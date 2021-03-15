package esa.egscc.metrics.adapter.provider;

import static java.util.Objects.requireNonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jetty.util.URIUtil;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.egscc.metrics.adapter.config.MetadataLabelProvider;
import esa.egscc.metrics.adapter.config.MetricConfiguration;
import esa.egscc.metrics.adapter.util.Constants;

/**
 * Lazily provides read-only label metadata (key-value pairs) for gateway
 * metrics in different formats. Implements a
 * {@link java.util.function.Supplier} of {@code Map<String,String>} so that it
 * is possible to also directly obtain the key-value pairs.
 */
@Component(name = "MetadataLabelProvider", immediate = true)
public class MetadataLabelProviderImpl implements MetadataLabelProvider {
	// NOTE: Do not use hyphens or other special characters in the keys, as push
	// then will not work.
	private static final String JOB_KEY = "job";
	private static final String JAVA_VERSION_KEY = "java_version";

	private MetricRegistry metricRegistry;
	private MetricConfiguration metricConfiguration;

	private Map<String, String> metadataKeyValuePairs;

	private final Logger logger = LoggerFactory.getLogger(MetadataLabelProviderImpl.class);

	@Activate
	protected void activate() {
		logger.debug("Activating MetadataLabelProviderImpl.");

		final Set<String> configuredMetadataKeys = metricConfiguration.getMetadata();
		fillMetadataMap(configuredMetadataKeys);
	}

	@Deactivate
	protected void deactivate() {
		logger.debug("Deactivating MetadataLabelProviderImpl.");
		
		metadataKeyValuePairs = null;
	}

	private void fillMetadataMap(final Set<String> metadataKeys) {
		// Order seems important for the push gateway URL
		final Map<String, String> metadataKeyValueMap = new LinkedHashMap<>();

		// Prometheus job is always present and cannot be configured away.
		metadataKeyValueMap.put(JOB_KEY, Constants.JOB_NAME);

		// java version - Note that since Java9, it returns e.g. "9" instead of "1.9"
		if (metadataKeys.contains(JAVA_VERSION_KEY)) {
			metadataKeyValueMap.put(JAVA_VERSION_KEY, System.getProperty("java.version"));
		}

		this.metadataKeyValuePairs = Collections.unmodifiableMap(metadataKeyValueMap);
	}

	@Override
	public Map<String, String> get() {
		return metadataKeyValuePairs;
	}

	@Override
	public String getAsUrlParameters() {
		requireNonNull(metadataKeyValuePairs, "meta data key-value map must not be null!");
		final StringBuilder metadataBuilder = new StringBuilder();
		final Consumer<? super Map.Entry<String, String>> metadataTagConsumer = entry -> metadataBuilder
				.append(URIUtil.SLASH).append(entry.getKey()).append(URIUtil.SLASH).append(urlEncode(entry.getValue()));
		metadataKeyValuePairs.entrySet().stream().forEachOrdered(metadataTagConsumer);
		return metadataBuilder.toString();
	}

	@Override
	public void updateMetadataTags() {
		requireNonNull(metricRegistry, "metricRegistry must not be null!");
		metricRegistry.getMetadata().values().forEach(md -> md.setTags(new HashMap<>(get())));
	}

	/**
	 * OSGi Service Component Modification Callback
	 */
	@Modified
	protected void modified(final Map<String, Object> properties) {
		final Set<String> configuredMetadataKeys = metricConfiguration.getMetadata();
		fillMetadataMap(configuredMetadataKeys);
		updateMetadataTags();
	}

	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			// should never happen
			return null;
		}
	}

	@Reference
	protected void setMetricRegistry(final MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;
	}

	protected void unsetMetricRegistry(final MetricRegistry metricRegistry) {
		this.metricRegistry = null;
	}

	@Reference
	protected void setMetricConfiguration(final MetricConfiguration metricConfiguration) {
		this.metricConfiguration = metricConfiguration;
	}

	protected void unsetMetricConfiguration(final MetricConfiguration metricConfiguration) {
		this.metricConfiguration = null;
	}
}
