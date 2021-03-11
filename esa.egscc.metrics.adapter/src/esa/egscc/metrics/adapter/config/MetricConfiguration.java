package esa.egscc.metrics.adapter.config;

import java.util.Set;

/**
 * The {@link MetricConfiguration} service is an gateway's internal application
 * access point to the QIVICON metric configuration functionality.
 * {@link MetricConfiguration} service allows to enable or disable the reporting
 * of metrics. For the reporting of the metrics the following configuration options
 * are supported:
 * <li>definition of a URL, to which the metrics will be pushed
 * <li>activation and deactivation of a local endpoint
 * <li>activation and deactivation of a certificate verification check
 * <li>definition of a time interval in seconds when to push the metrics to the cloud
 * <li>definition of a comma separated list of enabled metrics names including wildcard support
 * <li>definition of a comma separated list of disabled metric names including wildcard support
 * <br/>
 * <br/>
 * <p>
 * Access to this service requires the
 * {@code ServicePermission[MetricConfiguration, GET]} permission. It is intended
 * that only administrative bundles should be granted this permission to limit
 * access to the potentially intrusive methods provided by this service.
 */
public interface MetricConfiguration {

    /**
     * Checks if reporting of metrics is enabled or disabled.
     *
     * @return {@code true} if reporting of metrics is enabled, otherwise {@code false}
     */
    boolean isEnabled();

    /**
     * Checks if local endpoint is enabled or disabled.
     *
     * @return {@code true} if local endpoint is enabled, otherwise {@code false}
     */
    boolean isLocalEndPointEnabled();

    /**
     * Gets the URL of the push gateway.
     *
     * @return URL of the push gateway
     */
    String getPushGatewayURL();

    /**
     * Checks if verification of certificate should be skipped or not.
     *
     * @return {@code true} if verification of certificate should be skipped, otherwise {@code false}
     */
    boolean isSkipVerifyCertificate();

    /**
     * Gets time interval in seconds when to push metrics to the cloud.
     *
     * @return time interval in seconds when to push metrics to the cloud
     */
    int getPushToCloudTimeInterval();

    /**
     * Gets whitelist of metric names.
     *
     * @return set of enabled metric names if whitelist is defined, otherwise an empty set
     */
    Set<String> getEnabledMetrics();

    /**
     * Gets blacklist of metric names.
     *
     * @return set of disabled metric names if blacklist is defined, otherwise an empty set
     */
    Set<String> getDisabledMetrics();

    /**
     * The metadata that should be sent along with the metrics.
     *
     * @return set of metadata properties if defined, otherwise an empty set
     */
    Set<String> getMetadata();
}
