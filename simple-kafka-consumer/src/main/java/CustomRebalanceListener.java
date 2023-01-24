import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class CustomRebalanceListener implements ConsumerRebalanceListener {

    private final static Logger logger = LoggerFactory.getLogger(CustomRebalanceListener.class);

    /**
     * 리밸런스가 시작되기 직전에 호출
     * 마지막으로 처리한 레코드를 기준으로 커밋하기 위해서는 리밸런스가 시작하기 직전에 커밋하면 됨
     * 따라서, 이 부분에 커밋을 구현하여 처리 가능
     */
    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        logger.warn("Partitions are revoked = {} ", partitions.toArray());
    }

    /**
     * 리밸런스가 끝난 뒤에 파티션이 할당 완료되면 호출
     */
    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        logger.warn("Partitions are assigned = {}", partitions.toArray());
    }
}
