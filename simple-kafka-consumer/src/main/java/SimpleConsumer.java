import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

public class SimpleConsumer {
    /**
     * 정상 종료되지 않은 컨슈머는 세션 타임아웃이 발생할 떄까지 컨슈머 그룹에 좀비프로세스로 남게됨
     * 명시적으로 종료를 알리지 않으면, 리밸런싱이 늦어짐
     * wakeup 를 통해 안전하게 종료 후, poll 메서드가 호출되면 WakeupException 예외가 발생함
     * WakeupException 을 받아 자원을 해제
     */
    static class ShutdownThread extends Thread {
        public void run() {
            logger.info("Shutdown hook");
            consumer.wakeup();
        }
    }

    private static KafkaConsumer<String, String> consumer;
    private final static Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";
    private final static String GROUP_ID = "test-group";
    private final static int PARTITION_NUMBER = 0;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

//        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); // 기본값
//        configs.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 60000); // 기본값
//        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 수동 커밋시 설정

        consumer = new KafkaConsumer<>(configs);
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        // 구독 + 리밸런싱 리스너 컨슈머
//        consumer.subscribe(Arrays.asList(TOPIC_NAME), new CustomRebalanceListener());

        // 파티션 지정 컨슈머 ( Group id 는 필수가 아님)
//        consumer.assign(Collections.singleton(new TopicPartition(TOPIC_NAME, PARTITION_NUMBER)));

        try {
//          simpleConsume();
            syncCommit();
//          syncCommitBasedRecord();
//          asyncCommit();
//            asyncCommitCallBack();
        } catch (WakeupException e) {
            logger.warn("Wakeup consumer");
        } finally {
            logger.warn("Consumer closed");
            consumer.close();
        }
    }

    // 자동 커밋
    private static void simpleConsume() {
        // 무한 루프를 돌면 poll 을 통해 데이터를 가져온다
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                logger.info("record = {} ", record);
            }
        }
    }

    /**
     * 수동 커밋 - 동기 오프셋
     * 가장 마지막 레코드의 오프셋을 기준으로 커밋
     * poll 메서드로 받은 모든 레코드의 처리 후 commitSync 를 호출해야 함
     */
    private static void syncCommit() {
        // 무한 루프를 돌면 poll 을 통해 데이터를 가져온다
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                logger.info("record = {} ", record);
            }
            consumer.commitSync(); // 반드시 커밋을 해줘야 리밸런싱을 피할 수 있다
        }
    }

    /**
     * 수동 커밋 - 동기 오프셋(레코드단위)
     * 레코드 단위로 커밋을 진행
     * 커밋은 컨슈머와 브로커의 통신이기에 커밋을 자주하게 되면 브로커에 부담이 가중되고 컨슈머 처리 능력도 떨어짐
     * 일반적으론 활용하지 않음 - 데이터 처리량이 적을 때 (초당 100개 이하)
     */
    private static void syncCommitBasedRecord() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();

            for (ConsumerRecord<String, String> record : records) {
                logger.info("record = {} ", record);
                currentOffset.put(
                        new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset() + 1, null)
                );
                consumer.commitSync(currentOffset);
            }
        }
    }

    /**
     * 수동 커밋 - 비동기 오프셋 커밋
     * 동기 오프셋의 경우 커밋이 완료되었음을 브로커로부터 응답기 까지 데이터처리가 일시적으로 중단
     * 더 많은 데이터를 처리하기 위해선 비동기 오프셋을 활용
     */
    private static void asyncCommit() {
        // 무한 루프를 돌면 poll 을 통해 데이터를 가져온다
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                logger.info("record = {} ", record);
            }
            consumer.commitAsync();
        }
    }

    /**
     * 수동 커밋 - 비동기 오프셋 콜백
     * 커밋이 정상적으로 되지 않을 수 있기 때문에, 완료되었는지에 대해 콜백이 가능
     * 커밋 실패시 대응책을 마련해야함
     */
    private static void asyncCommitCallBack() {
        // 무한 루프를 돌면 poll 을 통해 데이터를 가져온다
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                logger.info("record = {} ", record);
            }
            consumer.commitAsync(
                    new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
                            if (null != e) {
                                logger.error("Commit failed, offset = {}", offsets, e);
                            } else {
                                logger.debug("commit success");
                            }
                        }
                    }
            );
        }
    }
}
