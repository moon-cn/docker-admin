package cn.moon.docker.core.callback;

import com.github.dockerjava.api.model.ResponseItem;
import org.slf4j.Logger;


public class LogBuffer {


    public static final String TAB = "    ";
    Logger log;


    public LogBuffer(Logger log) {
        this.log = log;
    }

    private final StringBuffer buffer = new StringBuffer();




    public void tryLog(ResponseItem item) {
        // 打印比较好的日志
        String stream = item.getStream();
        if (stream != null) {
            buffer.append(stream);

            if (stream.endsWith("\n")) {
                String info = buffer.toString();
                String[] lines = info.split("\\n|\\r");
                for (String line : lines) {
                    if (line.startsWith("Step ")) {
                        log.info(line);
                    } else {
                        log.info("{}{}",TAB,line);
                    }
                }
                buffer.setLength(0);
            }

        } else if (item.getStatus() != null) {
            log.info("{}{} {}",TAB, item.getStatus(), item.getProgress());
        } else {
            log.info("{}{}", TAB,item);
        }

    }

}
