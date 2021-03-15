package esa.egscc.metrics.adapter.config;

import java.util.Map;
import java.util.function.Supplier;

public interface MetadataLabelProvider extends Supplier<Map<String, String>> {

	/**
	 * Converts the given metadata map into the form
	 * <code>/key/value/key2/value2</code> that can be appended to an URL.
	 * 
	 * @param metaDataKeyValuePairs map of key-value pairs of metadata
	 * @return String representation in the form <code>/key/value</code> for each
	 *         pair
	 */
	String getAsUrlParameters();

	/**
	 * Update all registered metadata and attach the configured label tags.
	 */
	void updateMetadataTags();
}
