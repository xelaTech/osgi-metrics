package esa.egscc.metrics.adapter.resource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import esa.egscc.metrics.adapter.exception.EmptyRegistryException;
import esa.egscc.metrics.adapter.exception.NoSuchRegistryException;
import esa.egscc.metrics.adapter.provider.MetricTriggerListener;
import esa.egscc.metrics.adapter.util.Constants;
import esa.egscc.metrics.adapter.writer.PrometheusMetricWriter;

@Component
@HttpWhiteboardServletPattern("/metrics/*")
public class PrometheusServlet extends HttpServlet implements Servlet {
	private static final long serialVersionUID = 1L;

	private final List<MetricTriggerListener> listeners = new CopyOnWriteArrayList<>();

	@Reference
	private MetricRegistry metricRegistry;

//	@Reference
//	private HttpService httpService;

//	@Activate
//	public void activate(BundleContext bundleContext) {
//		System.out.println("Activating Prometheus metrics servlet.");
//
//		try {
//			final DisableAuthenticationHttpContext context = new DisableAuthenticationHttpContext(
//					bundleContext.getBundle());
//			httpService.registerServlet(Constants.SERVLET_PATH, this, null, context);
//		} catch (ServletException | NamespaceException e) {
//			throw new RuntimeException("Prometheus Context Path cannot be registered");
//		}
//	}

//	@Deactivate
//	public void deactivate() {
//		System.out.println("Deactivating Prometheus metrics servlet.");
//
//		httpService.unregister(Constants.SERVLET_PATH);
//	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(Constants.CONTENT_TYPE_004);
		resp.setHeader(HttpHeader.CONTENT_ENCODING.asString(), "gzip");

		try (final GZIPOutputStream zippedOutputStream = new GZIPOutputStream(resp.getOutputStream());
				final Writer writer = new OutputStreamWriter(zippedOutputStream, StandardCharsets.UTF_8)) {

			listeners.forEach(MetricTriggerListener::trigger);
			final PrometheusMetricWriter metricWriter = new PrometheusMetricWriter(metricRegistry, writer, null);
			try {
				metricWriter.write();
			} catch (NoSuchRegistryException | EmptyRegistryException e) {
				throw new ServletException(e);
			}
		}
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	protected void addMetricTriggerListener(final MetricTriggerListener listener) {
		listeners.add(listener);
	}

	protected void removeMetricTriggerListener(final MetricTriggerListener listener) {
		listeners.remove(listener);
	}
}