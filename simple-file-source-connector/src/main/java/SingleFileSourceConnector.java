import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleFileSourceConnector extends SourceConnector {

    private Map<String, String> configProps;

    // REST API 로 템플릿으로 파이프라인을 만든다면 prop 으로 데이터가 들어옴(file, topic)
    @Override
    public void start(Map<String, String> props) {
        this.configProps = props;
        try {
            new SingleFileSourceConnectorConfig(props);
        } catch (ConfigException e) {
            throw new ConnectException(e.getMessage(), e);
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return SingleFileSourceTask.class;
    }

    // 우리가 만들 테스크에 따라서, 테스크에 어떤 config 를 넣을지 지정
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String,String>> taskConfigs = new ArrayList<>();
        Map<String, String> taskProps = new HashMap<>();
        taskProps.putAll(configProps);
        for (int i = 0; i < maxTasks ; i++) {
            taskConfigs.add(taskProps);
        }
        return taskConfigs;
    }

    @Override
    public void stop() {
    }

    @Override
    public ConfigDef config() {
        return SingleFileSourceConnectorConfig.CONFIG;
    }

    @Override
    public String version() {
        return "1.0";
    }
}
