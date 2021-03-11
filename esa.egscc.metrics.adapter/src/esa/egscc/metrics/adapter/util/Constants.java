package esa.egscc.metrics.adapter.util;

public final class Constants {

    private Constants() {
        throw new IllegalAccessError("Cannot be instantiated");
    }

    // JVM Metrics
    public static final String JVM_THREAD_COUNT_METRIC = "jvm_thread_count";
    public static final String JVM_HEAP_AVAILABLE_METRIC = "jvm_memory_heap_available";
    public static final String JVM_HEAP_USED_METRIC = "jvm_memory_heap_used";
    public static final String JVM_HEAP_FREE_METRIC = "jvm_memory_heap_free";
    public static final String JVM_UPTIME_SECONDS = "jvm_uptime";
    public static final String JVM_STARTUP_COUNT = "jvm_startup_count";
    public static final String JVM_SYSTEMSTATE_STARTUP = "jvm_systemstate_startup";
    public static final String JVM_SYSTEMSTATE_UPDATE = "jvm_systemstate_update";

    // OS Metrics
    public static final String OS_JVM_VMSIZE_METRIC = "os_jvm_memory_vmsize";
    public static final String OS_JVM_VMDATA_METRIC = "os_jvm_memory_vmdata";
    public static final String OS_JVM_THREAD_COUNT = "os_jvm_thread_count";
    public static final String OS_OPEN_NETWORK_CONNECTIONS = "os_network_connections_open";
    public static final String OS_OPEN_FILE_DESCRIPTORS = "os_jvm_file_descriptors_open";
    public static final String OS_UPTIME_SECONDS = "os_uptime";
    public static final String OS_NET_ETH_BYTES_RECEIVED = "os_net_eth_received";
    public static final String OS_NET_ETH_BYTES_SENT = "os_net_eth_sent";
    public static final String OS_NET_WIFI_BYTES_RECEIVED = "os_net_wifi_received";
    public static final String OS_NET_WIFI_BYTES_SENT = "os_net_wifi_sent";

    // CPU Metrics
    public static final String OS_CPU_USER = "os_cpu_user";
    public static final String OS_CPU_IDLE = "os_cpu_idle";
    public static final String OS_CPU_SYSTEM = "os_cpu_system";
    public static final String OS_CPU_WAITIO = "os_cpu_waitio";
    public static final String OS_CPU_WAITHYPER = "os_cpu_waithyper";

    // Logging Metrics
    public static final String ERROR_COUNT_METRIC = "rt_logs_error_total";
    public static final String WARNING_COUNT_METRIC = "rt_logs_warning_total";
    public static final String INFO_COUNT_METRIC = "rt_logs_info_total";
    public static final String DEBUG_COUNT_METRIC = "rt_logs_debug_total";

    public static final String JOB_NAME = "egscc";
    public static final String MANGER_NAME = "egscc_";
    public static final String SERVLET_PATH = "/metrics";

    // HTTP Headers
    public static final String ACCEPT_HEADER = "Accept";
    public static final String ACCEPT_HEADER_JSON = "application/json";
    public static final String ACCEPT_HEADER_TEXT = "text/plain";
    public static final String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

    // Histogram, Meter, or Timer Constants
    public static final String QUANTILE = "quantile";
    public static final String COUNT = "count";
    public static final String MEAN_RATE = "meanRate";
    public static final String ONE_MINUTE_RATE = "oneMinRate";
    public static final String FIVE_MINUTE_RATE = "fiveMinRate";
    public static final String FIFTEEN_MINUTE_RATE = "fifteenMinRate";
    public static final String MAX = "max";
    public static final String MEAN = "mean";
    public static final String MIN = "min";
    public static final String STD_DEV = "stddev";
    public static final String MEDIAN = "p50";
    public static final String PERCENTILE_75TH = "p75";
    public static final String PERCENTILE_95TH = "p95";
    public static final String PERCENTILE_98TH = "p98";
    public static final String PERCENTILE_99TH = "p99";
    public static final String PERCENTILE_999TH = "p999";

    // Appended Units for prometheus
    public static final String APPENDEDSECONDS = "_seconds";
    public static final String APPENDEDBYTES = "_bytes";
    public static final String APPENDEDPERCENT = "_percent";

    // Conversion factors
    public static final double NANOSECONDCONVERSION = 0.000000001;
    public static final double MICROSECONDCONVERSION = 0.000001;
    public static final double MILLISECONDCONVERSION = 0.001;
    public static final double SECONDCONVERSION = 1;
    public static final double MINUTECONVERSION = 60;
    public static final double HOURCONVERSION = 3600;
    public static final double DAYCONVERSION = 86400;
    public static final double KILOBYTECONVERSION = 1024;
    public static final double MEGABYTECONVERSION = 1048576;
    public static final double GIGABYTECONVERSION = 1073741824;
    public static final double BITCONVERSION = 0.125;
    public static final double KILOBITCONVERSION = 125;
    public static final double MEGABITCONVERSION = 125000;
    public static final double GIGABITCONVERSION = 1.25e+8;
    public static final double KIBIBITCONVERSION = 128;
    public static final double MEBIBITCONVERSION = 131072;
    public static final double GIBIBITCONVERSION = 1.342e+8;
}
