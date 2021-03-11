package esa.egscc.metrics.adapter.push;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.OutputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import esa.egscc.metrics.adapter.config.MetadataLabelProvider;
import esa.egscc.metrics.adapter.config.MetricConfiguration;
import esa.egscc.metrics.adapter.exception.EmptyRegistryException;
import esa.egscc.metrics.adapter.exception.NoSuchRegistryException;
import esa.egscc.metrics.adapter.provider.MetricTriggerListener;
import esa.egscc.metrics.adapter.util.Constants;
import esa.egscc.metrics.adapter.writer.PrometheusMetricWriter;
import esa.egscc.metrics.adapter.writer.RegexMetricFilter;

@Component(name = "PushgatewaySender", immediate = true)
public class PushgatewaySender {
	private static final String PUSHGATEWAY_USERNAME = "admin";
	private static final String PUSHGATEWAY_PASSWORD = "admin";
//	private static final String PUSHGATEWAY_PASSWORD = "eyJrIjoiYVduTkhSa0drRXJyNUNXQlRUaWoyRXYxR1d4cVdMdngiLCJuIjoiZWdzY2NfbWV0cmljcyIsImlkIjoxfQ==";
	private static final String THREAD_NAME = "Prometheus Push EGSCC Sender";

	private final List<MetricTriggerListener> listeners = new CopyOnWriteArrayList<>();
	private MetricRegistry metricRegistry;
	private MetadataLabelProvider metadataLabelProvider;
	private MetricConfiguration metricConfiguration;

	private ScheduledExecutorService executor;

	private String pushgatewayUri;
	private BiPredicate<String, String> metricFilter;

	@Activate
	public void activate() {
		executor = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, THREAD_NAME));

		StringBuilder uriBuilder = new StringBuilder(metricConfiguration.getPushGatewayURL());
		uriBuilder.append(metadataLabelProvider.getAsUrlParameters());

		pushgatewayUri = uriBuilder.toString();
		int pushInterval = metricConfiguration.getPushToCloudTimeInterval();
		metricFilter = new RegexMetricFilter(metricConfiguration.getEnabledMetrics(),
				metricConfiguration.getDisabledMetrics());

		executor.scheduleAtFixedRate(this::conditionalSend, 0, pushInterval, TimeUnit.SECONDS);
	}

	@Deactivate
	public void deactivate() {
		shutdownExecutor();
	}

	private void conditionalSend() {
		try {
			if (metricConfiguration.isEnabled()) {
				send();
			}
		} catch (Throwable e) {
			throw e;
		}
	}

	private void send() {
		// TODO: Create a pool of HTTP clients and reuse them, instead of creating each
		// time a new one.
		final HttpClient httpClient = new HttpClient();
		final Request request = httpClient.newRequest(pushgatewayUri).method(HttpMethod.PUT);
		final Authentication.Result auth = new BasicAuthentication.BasicResult(request.getURI(), PUSHGATEWAY_USERNAME,
				PUSHGATEWAY_PASSWORD);
		auth.apply(request);

		try (final OutputStreamContentProvider contentProvider = new OutputStreamContentProvider();
				final OutputStream out = contentProvider.getOutputStream()) {

			request.content(contentProvider, Constants.CONTENT_TYPE_004);
			final Writer streamWriter = new OutputStreamWriter(out, UTF_8);

			listeners.forEach(MetricTriggerListener::trigger);
			final PrometheusMetricWriter metricWriter = new PrometheusMetricWriter(metricRegistry, streamWriter,
					metricFilter);

			request.send(
					result -> System.out.println("Sent metrics, response status: " + result.getResponse().getStatus()));

			metricWriter.write();
		} catch (IOException | NoSuchRegistryException | EmptyRegistryException e) {
			// logger.error("Could not send metrics.", e);
		}
	}

	private void shutdownExecutor() {
		try {
			executor.shutdown();
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	@Reference
	protected void setMetricConfiguration(final MetricConfiguration metricConf) {
		this.metricConfiguration = metricConf;
	}

	protected void unsetMetricConfiguration(final MetricConfiguration metricConf) {
		this.metricConfiguration = null;
	}

	@Reference
	protected void setMetricRegistry(final MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;
	}

	protected void unsetMetricRegistry(final MetricRegistry metricRegistry) {
		this.metricRegistry = null;
	}

	@Reference
	protected void setMetadataLabelProvider(final MetadataLabelProvider metadataLabelProvider) {
		this.metadataLabelProvider = metadataLabelProvider;
	}

	protected void unsetMetadataLabelProvider(final MetadataLabelProvider metadataLabelProvider) {
		this.metadataLabelProvider = null;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	protected void addMetricTriggerListener(final MetricTriggerListener listener) {
		listeners.add(listener);
	}

	protected void removeMetricTriggerListener(final MetricTriggerListener listener) {
		listeners.remove(listener);
	}

}
