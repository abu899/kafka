import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.List;
import java.util.Map;

public class CustomSourceTask extends SourceTask {

    // Task 의 버저닝
    @Override
    public String version() {
        return null;
    }

    // 커넥터에서 가져온 프로퍼티를 가지고 초기화를 진행
    @Override
    public void start(Map<String, String> props) {
    }

    // 데이터를 처리하기위해 호출되는 메서드
    // List<SourceRecord> 로 리턴하게 되면 send() 와 같이 토픽으로 데이터를 전달
    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        return null;
    }

    @Override
    public void stop() {

    }
}
