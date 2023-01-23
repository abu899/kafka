import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class SimpleProducer {
    private final static Logger logger = LoggerFactory.getLogger(SimpleProducer.class);
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        Properties configs = new Properties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Custom Partitioner
//        configs.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class);

        KafkaProducer<String , String> producer = new KafkaProducer<>(configs);

//        messageWithoutKey(producer);
//        messageWithKey(producer);
//        messageWithPartitionNo(producer);
        printRecordMetaData(producer);

        producer.flush();
        producer.close(); // Accumulator 에 저장된 모든 데이터를 카프카 클러스토 전송 후 종료
    }

    private static void messageWithoutKey(KafkaProducer<String , String> producer) {
        String message = "test message in Java3";
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, message);
        logger.info("record = {} ", record);

        producer.send(record);
    }

    private static void messageWithKey(KafkaProducer<String , String> producer) {
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "pangyo", "pangyo");
        ProducerRecord<String, String> record2 = new ProducerRecord<>(TOPIC_NAME, "busan", "busan");

        producer.send(record);
        producer.send(record2);
    }

    private static void messageWithPartitionNo(KafkaProducer<String , String> producer) {
        int partitionNo = 0;
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, partitionNo,
                "pankyo", "pankyo");
        producer.send(record);
    }

    private static void printRecordMetaData(KafkaProducer<String , String> producer)  {
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "pangyo", "pangyo");

        RecordMetadata metadata = null;
        try {
            metadata = producer.send(record).get();
            logger.info("metadata = {} ", metadata);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
