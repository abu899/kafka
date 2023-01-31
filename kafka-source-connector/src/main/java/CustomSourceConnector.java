import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.List;
import java.util.Map;

public class CustomSourceConnector extends SourceConnector {

    // 프로퍼티를 가져와서 initialize
    @Override
    public void start(Map<String, String> props) {
    }

    // 실행하고자 하는 소스 테스크의 클래스 이름
    // 기본적으로 한개의 커넥터당 한개의 테스크
    @Override
    public Class<? extends Task> taskClass() {
        return CustomSourceTask.class;
//        return null;
    }

    // Task 마다 다른 옵션을 주고 싶다면
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        return null;
    }

    // 리소스 해제가 필요하다면 이곳에서 진행
    @Override
    public void stop() {
    }

    @Override
    public ConfigDef config() {
        return null;
    }

    @Override
    public String version() {
        return null;
    }
}
