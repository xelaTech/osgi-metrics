package esa.egscc.metrics.adapter.util;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * This class extends a {@link HttpContext} to enable our servlets and resources
 * to run outside the authorization scope of the default {@link HttpService}
 * -Context. This is needed to be able to access the endpoints and the login
 * form without any prior user-credential check.
 *
 */
public final class DisableAuthenticationHttpContext implements HttpContext {

	private final Bundle bundle;

//	private final Logger logger = LoggerFactory.getLogger(DisableAuthenticationHttpContext.class);

	public DisableAuthenticationHttpContext(final Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public boolean handleSecurity(final HttpServletRequest request, final HttpServletResponse response)
	        throws IOException {
		return true;
	}

	@Override
	public URL getResource(final String name) {
		try {
			return bundle.getResource(name);
		} catch (final Exception ex) {
//			logger.error(String.format("Could not find artifact: %s", name), ex);
			return null;
		}
	}

	@Override
	public String getMimeType(final String name) {
		return null;
	}
}
