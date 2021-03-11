package esa.egscc.metrics.adapter.provider;

/**
 * The {@link MetricTriggerListener} provides a listener for metric values which should be updated every time an update
 * action (e.g. browser refresh) has been triggered.
 *
 */

@FunctionalInterface
public interface MetricTriggerListener {

    /**
     * Triggers the update of a metric value.
     */
    void trigger();

}
