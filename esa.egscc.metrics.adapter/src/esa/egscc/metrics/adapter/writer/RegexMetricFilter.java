package esa.egscc.metrics.adapter.writer;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.egscc.metrics.adapter.util.Constants;

public class RegexMetricFilter implements BiPredicate<String, String> {

    private final Predicate<String> enabledMetrics;
    private final Predicate<String> disabledMetrics;

    public RegexMetricFilter(Set<String> enabledMetrics, Set<String> disabledMetrics) {
        if (!enabledMetrics.isEmpty()) {
            final Pattern enabledMetricsPattern = Pattern.compile(getRegexMetricsFilter(enabledMetrics));

            this.enabledMetrics = value -> matchesPattern(value, enabledMetricsPattern);
        } else {
            this.enabledMetrics = value -> true;
        }

        if (!disabledMetrics.isEmpty()) {
            final Pattern disabledMetricsPattern = Pattern.compile(getRegexMetricsFilter(disabledMetrics));

            this.disabledMetrics = value -> !matchesPattern(value, disabledMetricsPattern);
        } else {
            this.disabledMetrics = value -> true;
        }
    }

    @Override
    public boolean test(String metricName, String metricNameWithUnit) {
        return (enabledMetrics.test(metricName) || enabledMetrics.test(metricNameWithUnit))
                && disabledMetrics.test(metricName) && disabledMetrics.test(metricNameWithUnit);
    }

    private boolean matchesPattern(String metric, Pattern pattern) {
        Matcher matcher = pattern.matcher(metric);
        return matcher.matches();
    }

    /**
     * Returns the enabled / disabled metrics set in the metrics configuration file as a regular expression.
     * The regular expression returned by this method is afterwards used to filter the metrics map.
     *
     * @param searchStrings
     *                          set of enabled / disabled metrics
     * @return enabled / disabled metrics as regular expression<br>
     *         Examples:<br>
     *         <li>Filter <code>shgw_os_*, shgw_jvm_*</code> will be transformed into the regular expression
     *         <code>os_.*|jvm_.*</code></li>
     *         <li>Filter <code>os_*, jvm_*</code> will be transformed into the regular expression
     *         <code>os_.*|jvm_.*</code></li>
     */
    private String getRegexMetricsFilter(Set<String> searchStrings) {
        StringBuilder regexBuilder = new StringBuilder();
        for (String searchString : searchStrings) {
            if (searchString.startsWith(Constants.MANGER_NAME)) {
                searchString = searchString.replaceFirst(Constants.MANGER_NAME, "");
            }
            searchString = searchString.replaceAll("\\*", ".*");
            regexBuilder.append(searchString).append("|");
        }

        String regexMetricsFilter = regexBuilder.toString();
        if (regexMetricsFilter.endsWith("|")) {
            regexMetricsFilter = regexMetricsFilter.substring(0, regexMetricsFilter.length() - 1);
        }

        return regexMetricsFilter;
    }

}