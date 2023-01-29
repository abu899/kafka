package processorapi;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

public class FilterProcessor implements Processor<String, String> { // Processor<Key, Value>
    private ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void process(String key, String value) {
        if (value.length() > 5) {
            context.forward(key, value); // 다음 프로세서로 넘김
        }
        context.commit(); // 레코드가 처리되었음을 지정
    }

    @Override
    public void close() {

    }
}
