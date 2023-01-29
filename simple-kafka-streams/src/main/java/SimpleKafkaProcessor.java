import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import processorapi.FilterProcessor;

import java.util.Properties;

public class SimpleKafkaProcessor {
    private static String APPLICATION_NAME = "streams-processor-application";
    private static String BOOTSTRAP_SERVER = "localhost:9092";
    private static String STREAM_LOG = "stream_log";
    private static String STREAM_LOG_FILTER = "stream_log_filter";

    static void run() {
        Properties configs = new Properties();
        configs.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        configs.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        configs.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        configs.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        Topology topology = new Topology();
        topology.addSource("Source", STREAM_LOG)
                .addProcessor("Process",
                        ()-> new FilterProcessor(),
                        "Source")
                .addSink("Sink", STREAM_LOG_FILTER, "Process");

        KafkaStreams streams = new KafkaStreams(topology, configs);
        streams.start();
    }
}
