package esa.egscc.metrics.adapter.provider.helper;

import java.lang.management.ManagementFactory;

public class JmxHelper {
	private static final String MBEANNAME_CODE_CACHE = "Code Cache";
	private static final String MBEANNAME_EDEN_SPACE = "Eden Space";
	private static final String MBEANNAME_METASPACE = "Metaspace";
	private static final String MBEANNAME_SURVIVOR_SPACE = "Survivor Space";
	private static final String MBEANNAME_OLD_SPACE = "Tenured Gen";

	private JmxHelper() {
		throw new IllegalAccessError("Cannot be instantiated");
	}

	public static long getUsedMemory() {
		return ManagementFactory.getMemoryPoolMXBeans().stream()
				.filter(memoryBean -> fileterMemoryBean(memoryBean.getName()))
				.mapToLong(memoryBean -> memoryBean.getUsage().getUsed()).sum();
	}

	public static long getTotalMemory() {
		return ManagementFactory.getMemoryPoolMXBeans().stream()
				.filter(memoryBean -> fileterMemoryBean(memoryBean.getName()))
				.mapToLong(memoryBean -> memoryBean.getUsage().getCommitted()).sum();

	}

	private static boolean fileterMemoryBean(String memoryBeanName) {
		return memoryBeanName.contains(MBEANNAME_OLD_SPACE) || memoryBeanName.contains(MBEANNAME_EDEN_SPACE)
				|| memoryBeanName.contains(MBEANNAME_SURVIVOR_SPACE) || memoryBeanName.contains(MBEANNAME_CODE_CACHE)
				|| memoryBeanName.contains(MBEANNAME_METASPACE);
	}

}
