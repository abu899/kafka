import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

import static org.apache.kafka.common.config.ConfigDef.*;

// config 를 정의할 때 지정하는 AbstractClass
// 아래 데이터에 대해 템플릿 형태로 config 를 받는다
public class SingleFileSinkConnectorConfig extends AbstractConfig {

    public static final String DIR_FILE_NAME = "file";
    private static final String DIR_FILE_NAME_DEFAULT_VALUE = "/tmp/kafka.txt";
    private static final String DIR_FILE_NAME_DOC = "저장할 디렉토리와 경로와 이름";

    public static final String TOPIC_NAME = "topic";

    public static ConfigDef CONFIG = new ConfigDef()
            .define(DIR_FILE_NAME, Type.STRING,
                    DIR_FILE_NAME_DEFAULT_VALUE,
                    Importance.HIGH, DIR_FILE_NAME_DOC);

    public SingleFileSinkConnectorConfig(Map<String, String> props) {
        super(CONFIG, props);
    }
}
