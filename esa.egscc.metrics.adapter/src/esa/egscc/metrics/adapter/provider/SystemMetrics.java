package esa.egscc.metrics.adapter.provider;

import static esa.egscc.metrics.adapter.util.Constants.JVM_HEAP_AVAILABLE_METRIC;
import static esa.egscc.metrics.adapter.util.Constants.JVM_HEAP_FREE_METRIC;
import static esa.egscc.metrics.adapter.util.Constants.JVM_HEAP_USED_METRIC;
import static esa.egscc.metrics.adapter.util.Constants.JVM_STARTUP_COUNT;
import static esa.egscc.metrics.adapter.util.Constants.JVM_SYSTEMSTATE_STARTUP;
import static esa.egscc.metrics.adapter.util.Constants.JVM_SYSTEMSTATE_UPDATE;
import static esa.egscc.metrics.adapter.util.Constants.JVM_THREAD_COUNT_METRIC;
import static esa.egscc.metrics.adapter.util.Constants.JVM_UPTIME_SECONDS;
import static esa.egscc.metrics.adapter.util.Constants.OS_CPU_IDLE;
import static esa.egscc.metrics.adapter.util.Constants.OS_CPU_SYSTEM;
import static esa.egscc.metrics.adapter.util.Constants.OS_CPU_USER;
import static esa.egscc.metrics.adapter.util.Constants.OS_CPU_WAITHYPER;
import static esa.egscc.metrics.adapter.util.Constants.OS_CPU_WAITIO;
import static esa.egscc.metrics.adapter.util.Constants.OS_JVM_THREAD_COUNT;
import static esa.egscc.metrics.adapter.util.Constants.OS_JVM_VMDATA_METRIC;
import static esa.egscc.metrics.adapter.util.Constants.OS_JVM_VMSIZE_METRIC;
import static esa.egscc.metrics.adapter.util.Constants.OS_NET_ETH_BYTES_RECEIVED;
import static esa.egscc.metrics.adapter.util.Constants.OS_NET_ETH_BYTES_SENT;
import static esa.egscc.metrics.adapter.util.Constants.OS_NET_WIFI_BYTES_RECEIVED;
import static esa.egscc.metrics.adapter.util.Constants.OS_NET_WIFI_BYTES_SENT;
import static esa.egscc.metrics.adapter.util.Constants.OS_OPEN_FILE_DESCRIPTORS;
import static esa.egscc.metrics.adapter.util.Constants.OS_OPEN_NETWORK_CONNECTIONS;
import static esa.egscc.metrics.adapter.util.Constants.OS_UPTIME_SECONDS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.egscc.metrics.adapter.provider.helper.JmxHelper;
import esa.egscc.metrics.api.Gauge;
import esa.egscc.metrics.api.Metadata;
import esa.egscc.metrics.api.MetricRegistry;
import esa.egscc.metrics.api.MetricType;
import esa.egscc.metrics.api.MetricUnits;

/**
 * OS and JVM metrics.
 */
@Component(immediate = true, name = "SystemMetrics")
public final class SystemMetrics {
	private static final String FD_DIR = "/proc/self/fd";
	private static final String STATUS_FILE = "/proc/self/status";
	private static final String SOCKET_STATS_FILE = "/proc/net/sockstat";
	private static final String PROC_NET_DEV_FILE = "/proc/net/dev";
	private static final String PROC_STAT_FILE = "/proc/stat";

	@Reference
	private MetricRegistry metricRegistry;

//	@Reference
//	private MetadataLabelProvider metadataLabelProvider;

	private boolean jmxAvailable;
	private ProcessStatus jvmProcessStatus;

	private final Map<String, Long> cpuLoadValues = new HashMap<>();
	private final Map<String, Long> sentAndReceivedBytesValues = new HashMap<>();

	private ServiceRegistration<?> cpuLoadRetrieverReg;
	private ServiceRegistration<?> sentAndReceivedBytesRetrieverReg;
	private ServiceRegistration<?> jvmProcessStatusRetrieverReg;

	private final Logger logger = LoggerFactory.getLogger(SystemMetrics.class);

	@Activate
	protected void activate(final BundleContext context) {
		logger.debug("Activating SystemMetrics.");

		try {
			jmxAvailable = Class.forName("java.lang.management.ManagementFactory") != null;
		} catch (ClassNotFoundException e) {
			jmxAvailable = false;
		}

		registerMetricListeners(context);

		final Metadata jvmTheadCountMd = new Metadata(JVM_THREAD_COUNT_METRIC, MetricType.GAUGE);
		final Metadata jvmHeapUsedMetricMd = new Metadata(JVM_HEAP_USED_METRIC, MetricType.GAUGE, MetricUnits.BYTES);
		final Metadata jvmHeapFreeMetricMd = new Metadata(JVM_HEAP_FREE_METRIC, MetricType.GAUGE, MetricUnits.BYTES);
		final Metadata jvmHeapAvailableMetricMd = new Metadata(JVM_HEAP_AVAILABLE_METRIC, MetricType.GAUGE,
				MetricUnits.BYTES);
		final Metadata osJvmVmsizeMetricMd = new Metadata(OS_JVM_VMSIZE_METRIC, MetricType.GAUGE,
				MetricUnits.KILOBYTES);
		final Metadata osJvmVmdataMetricMd = new Metadata(OS_JVM_VMDATA_METRIC, MetricType.GAUGE,
				MetricUnits.KILOBYTES);
		final Metadata osJvmThreadCountMd = new Metadata(OS_JVM_THREAD_COUNT, MetricType.GAUGE);
		final Metadata osOpenNetworkConnectionsMd = new Metadata(OS_OPEN_NETWORK_CONNECTIONS, MetricType.GAUGE);
		final Metadata osOpenFileDescriptorsMd = new Metadata(OS_OPEN_FILE_DESCRIPTORS, MetricType.GAUGE);
		final Metadata osNetEthBytesReceived = new Metadata(OS_NET_ETH_BYTES_RECEIVED, MetricType.GAUGE,
				MetricUnits.BYTES);
		final Metadata osNetEthBytesSent = new Metadata(OS_NET_ETH_BYTES_SENT, MetricType.GAUGE, MetricUnits.BYTES);
		final Metadata osNetWifiBytesReceived = new Metadata(OS_NET_WIFI_BYTES_RECEIVED, MetricType.GAUGE,
				MetricUnits.BYTES);
		final Metadata osNetWifiBytesSent = new Metadata(OS_NET_WIFI_BYTES_SENT, MetricType.GAUGE, MetricUnits.BYTES);
		final Metadata osCpuUser = new Metadata(OS_CPU_USER, MetricType.GAUGE, MetricUnits.PERCENT);
		final Metadata osCpuIdle = new Metadata(OS_CPU_IDLE, MetricType.GAUGE, MetricUnits.PERCENT);
		final Metadata osCpuSystem = new Metadata(OS_CPU_SYSTEM, MetricType.GAUGE, MetricUnits.PERCENT);
		final Metadata osCpuWaitio = new Metadata(OS_CPU_WAITIO, MetricType.GAUGE, MetricUnits.PERCENT);
		final Metadata osCpuWaithyper = new Metadata(OS_CPU_WAITHYPER, MetricType.GAUGE, MetricUnits.PERCENT);

		// JVM metrics
		metricRegistry.register(jvmTheadCountMd, (Gauge<Integer>) () -> Thread.getAllStackTraces().size());
		metricRegistry.register(jvmHeapUsedMetricMd, (Gauge<Long>) this::getUsedMemory);
		metricRegistry.register(jvmHeapFreeMetricMd, (Gauge<Long>) this::getFreeMemory);
		metricRegistry.register(jvmHeapAvailableMetricMd, (Gauge<Long>) this::getTotalMemory);

		// OS metrics
		metricRegistry.register(osJvmVmsizeMetricMd, (Gauge<Long>) () -> jvmProcessStatus.vmSize);
		metricRegistry.register(osJvmVmdataMetricMd, (Gauge<Long>) () -> jvmProcessStatus.vmData);
		metricRegistry.register(osJvmThreadCountMd, (Gauge<Integer>) () -> jvmProcessStatus.threadCount);
		metricRegistry.register(osOpenNetworkConnectionsMd, (Gauge<Integer>) this::getOpenNetworkConnectionsCount);
		metricRegistry.register(osOpenFileDescriptorsMd, (Gauge<Integer>) this::getOpenFileDescriptorsCount);
		metricRegistry.register(osNetEthBytesReceived, (Gauge<Long>) () -> {
			long netEthBytesReceived = sentAndReceivedBytesValues.get(OS_NET_ETH_BYTES_RECEIVED);
			return netEthBytesReceived >= 0 ? netEthBytesReceived : null;
		});

		metricRegistry.register(osNetEthBytesSent, (Gauge<Long>) () -> {
			long netEthBytesSent = sentAndReceivedBytesValues.get(OS_NET_ETH_BYTES_SENT);
			return netEthBytesSent >= 0 ? netEthBytesSent : null;
		});

		metricRegistry.register(osNetWifiBytesReceived, (Gauge<Long>) () -> {
			long netWifiBytesReceived = sentAndReceivedBytesValues.get(OS_NET_WIFI_BYTES_RECEIVED);
			return netWifiBytesReceived >= 0 ? netWifiBytesReceived : null;
		});

		metricRegistry.register(osNetWifiBytesSent, (Gauge<Long>) () -> {
			long netWifiBytesSent = sentAndReceivedBytesValues.get(OS_NET_WIFI_BYTES_SENT);
			return netWifiBytesSent >= 0 ? netWifiBytesSent : null;
		});

		metricRegistry.register(osCpuUser, (Gauge<Long>) () -> cpuLoadValues.get(OS_CPU_USER));
		metricRegistry.register(osCpuIdle, (Gauge<Long>) () -> cpuLoadValues.get(OS_CPU_IDLE));
		metricRegistry.register(osCpuSystem, (Gauge<Long>) () -> cpuLoadValues.get(OS_CPU_SYSTEM));
		metricRegistry.register(osCpuWaitio, (Gauge<Long>) () -> cpuLoadValues.get(OS_CPU_WAITIO));
		metricRegistry.register(osCpuWaithyper, (Gauge<Long>) () -> cpuLoadValues.get(OS_CPU_WAITHYPER));

		// Register metadata to attach label tags to
//		metadataLabelProvider.updateMetadataTags();
	}

	@Deactivate
	protected void deactivate() {
		logger.debug("Deactivating SystemMetrics.");

		metricRegistry.remove(JVM_THREAD_COUNT_METRIC);
		metricRegistry.remove(JVM_HEAP_USED_METRIC);
		metricRegistry.remove(JVM_HEAP_FREE_METRIC);
		metricRegistry.remove(JVM_HEAP_AVAILABLE_METRIC);
		metricRegistry.remove(JVM_UPTIME_SECONDS);
		metricRegistry.remove(JVM_STARTUP_COUNT);
		metricRegistry.remove(JVM_SYSTEMSTATE_STARTUP);
		metricRegistry.remove(JVM_SYSTEMSTATE_UPDATE);

		metricRegistry.remove(OS_JVM_VMSIZE_METRIC);
		metricRegistry.remove(OS_JVM_VMDATA_METRIC);
		metricRegistry.remove(OS_JVM_THREAD_COUNT);
		metricRegistry.remove(OS_OPEN_NETWORK_CONNECTIONS);
		metricRegistry.remove(OS_OPEN_FILE_DESCRIPTORS);
		metricRegistry.remove(OS_UPTIME_SECONDS);
		metricRegistry.remove(OS_NET_ETH_BYTES_RECEIVED);
		metricRegistry.remove(OS_NET_ETH_BYTES_SENT);
		metricRegistry.remove(OS_NET_WIFI_BYTES_RECEIVED);
		metricRegistry.remove(OS_NET_WIFI_BYTES_SENT);
		metricRegistry.remove(OS_CPU_USER);
		metricRegistry.remove(OS_CPU_IDLE);
		metricRegistry.remove(OS_CPU_SYSTEM);
		metricRegistry.remove(OS_CPU_WAITIO);
		metricRegistry.remove(OS_CPU_WAITHYPER);

		unregisterMetricListeners();
	}

	private int getOpenNetworkConnectionsCount() {
		String firstLine = "";

		try (FileReader fileReader = new FileReader(new File(SOCKET_STATS_FILE));
				BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			firstLine = bufferedReader.readLine(); // e.g. sockets: used 144
			int lastSpace = firstLine.lastIndexOf(' ') + 1;
			final String openSocketsStr = firstLine.substring(lastSpace, firstLine.length()).trim();

			return Integer.parseInt(openSocketsStr);
		} catch (NumberFormatException | IOException e) {
			// logger.warn("Cannot parse sockets in {}, line: {}", SOCKET_STATS_FILE,
			// firstLine);
		}

		return 0;
	}

	private int getOpenFileDescriptorsCount() {
		File fileDescriptorDirectory = new File(FD_DIR);
		if (fileDescriptorDirectory.exists() && fileDescriptorDirectory.isDirectory()) {
			return fileDescriptorDirectory.list().length;
		} else {
			// logger.error("{} does not exist or is not a directory.", FD_DIR);
		}

		return 0;
	}

	private long getTotalMemory() {
		if (jmxAvailable) {
			return JmxHelper.getTotalMemory();
		} else {
			return Runtime.getRuntime().totalMemory();
		}
	}

	private long getUsedMemory() {
		if (jmxAvailable) {
			return JmxHelper.getUsedMemory();
		} else {
			return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		}
	}

	private long getFreeMemory() {
		if (jmxAvailable) {
			return JmxHelper.getTotalMemory() - JmxHelper.getUsedMemory();
		} else {
			return Runtime.getRuntime().freeMemory();
		}
	}

	private void registerMetricListeners(final BundleContext context) {
		cpuLoadRetrieverReg = context.registerService(MetricTriggerListener.class.getName(), new CpuLoadRetriever(),
				null);
		sentAndReceivedBytesRetrieverReg = context.registerService(MetricTriggerListener.class.getName(),
				new SentAndReceivedBytesRetriever(), null);
		jvmProcessStatusRetrieverReg = context.registerService(MetricTriggerListener.class.getName(),
				new JvmProcessStatusRetriever(), null);
	}

	private void unregisterMetricListeners() {
		cpuLoadRetrieverReg.unregister();
		sentAndReceivedBytesRetrieverReg.unregister();
		jvmProcessStatusRetrieverReg.unregister();
		cpuLoadRetrieverReg = null;
		sentAndReceivedBytesRetrieverReg = null;
		jvmProcessStatusRetrieverReg = null;
	}

	private final class CpuLoadRetriever implements MetricTriggerListener {

		private final Pattern cpuLoad = Pattern.compile("^cpu((\\s+[0-9]+){7,})");

		private CpuLoadRetriever() {
			retrieveCpuLoad();
		}

		@Override
		public void trigger() {
			retrieveCpuLoad();
		}

		private void retrieveCpuLoad() {
			try (FileReader fileReader = new FileReader(new File(PROC_STAT_FILE));
					BufferedReader reader = new BufferedReader(fileReader)) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					Matcher matcher = cpuLoad.matcher(line);
					if (matcher.matches() && matcher.groupCount() >= 2) {
						String groupContent = matcher.group(1).trim();
						String[] cpuValues = groupContent.split("\\s+");
						long cpuSum = 0;
						for (String cpuValue : cpuValues) {
							cpuSum = cpuSum + Long.valueOf(cpuValue);
						}

						if (cpuSum > 0) {
							cpuLoadValues.put(OS_CPU_USER, (long) (Double.valueOf(cpuValues[0]) / cpuSum * 100));
							cpuLoadValues.put(OS_CPU_SYSTEM, (long) (Double.valueOf(cpuValues[2]) / cpuSum * 100));
							cpuLoadValues.put(OS_CPU_IDLE, (long) (Double.valueOf(cpuValues[3]) / cpuSum * 100));
							cpuLoadValues.put(OS_CPU_WAITIO, (long) (Double.valueOf(cpuValues[4]) / cpuSum * 100));
							cpuLoadValues.put(OS_CPU_WAITHYPER, (long) (Double.valueOf(cpuValues[7]) / cpuSum * 100));
						}

						break;
					}
				}
			} catch (IOException e) {
				// logger.error(String.format("Error reading %s file: %s", PROC_STAT_FILE,
				// e.getMessage()));
			}
		}
	}

	private final class SentAndReceivedBytesRetriever implements MetricTriggerListener {

		private final Pattern netEthBytes = Pattern.compile("^\\s*eth0:((\\s+[0-9]+){16})");
		private final Pattern netWifiBytes = Pattern.compile("^\\s*wl0:((\\s+[0-9]+){16})");

		private SentAndReceivedBytesRetriever() {
			retrieveSentAndReceivedBytes();
		}

		@Override
		public void trigger() {
			retrieveSentAndReceivedBytes();
		}

		private void retrieveSentAndReceivedBytes() {
			long ethReceivedBytes = -1;
			long ethSentBytes = -1;
			long wifiReceivedBytes = -1;
			long wifiSentBytes = -1;

			try (FileReader fileReader = new FileReader(new File(PROC_NET_DEV_FILE));
					BufferedReader reader = new BufferedReader(fileReader)) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					Matcher ethMatcher = netEthBytes.matcher(line);
					Matcher wifiMatcher = netWifiBytes.matcher(line);
					if (ethMatcher.matches() && ethMatcher.groupCount() >= 2) {
						String ethGroupContent = ethMatcher.group(1).trim();
						ethReceivedBytes = Long.valueOf(ethGroupContent.split("\\s+")[0]);
						ethSentBytes = Long.valueOf(ethGroupContent.split("\\s+")[8]);
					}
					if (wifiMatcher.matches() && wifiMatcher.groupCount() >= 2) {
						String wifiGroupContent = wifiMatcher.group(1).trim();
						wifiReceivedBytes = Long.valueOf(wifiGroupContent.split("\\s+")[0]);
						wifiSentBytes = Long.valueOf(wifiGroupContent.split("\\s+")[8]);
					}
				}
			} catch (IOException e) {
				// logger.error(String.format("Error reading %s file: %s", PROC_STAT_FILE,
				// e.getMessage()));
			}

			sentAndReceivedBytesValues.put(OS_NET_ETH_BYTES_RECEIVED, ethReceivedBytes);
			sentAndReceivedBytesValues.put(OS_NET_ETH_BYTES_SENT, ethSentBytes);
			sentAndReceivedBytesValues.put(OS_NET_WIFI_BYTES_RECEIVED, wifiReceivedBytes);
			sentAndReceivedBytesValues.put(OS_NET_WIFI_BYTES_SENT, wifiSentBytes);
		}
	}

	private final class JvmProcessStatusRetriever implements MetricTriggerListener {

		private final Pattern memoryLinePattern = Pattern.compile("(.*):\\s+([0-9]+)(\\s+kB)?");

		@Override
		public void trigger() {
			final ProcessStatus result = new ProcessStatus();

			try (FileReader fileReader = new FileReader(new File(STATUS_FILE));
					BufferedReader reader = new BufferedReader(fileReader)) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					Matcher matcher = memoryLinePattern.matcher(line);
					if (matcher.matches() && matcher.groupCount() >= 2) {
						result.setPropertyByKey(matcher.group(1), matcher.group(2));
					}
				}
			} catch (IOException e) {
				// logger.error(String.format("Error reading status file: %s", e.getMessage()));
			}

			jvmProcessStatus = result;
		}
	}

	/**
	 * Represents data read from the <code>/proc/self/status</code> file.
	 */
	private static final class ProcessStatus {
		long vmSize;
		long vmData;
		int threadCount;

		void setPropertyByKey(String key, String value) {
			switch (key) {
			case "VmSize":
				this.vmSize = Long.parseLong(value);
				break;
			case "VmData":
				this.vmData = Long.parseLong(value);
				break;
			case "Threads":
				this.threadCount = Integer.parseInt(value);
				break;
			}
		}
	}
}
