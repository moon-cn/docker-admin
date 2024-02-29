package cn.moon.docker.core.callback;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.PushResponseItem;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class MyPushImageCallback extends ResultCallbackTemplate<MyPushImageCallback, PushResponseItem> {
    private final String logFileId;
    private String error;

    LogBuffer logBuffer = new LogBuffer(log);

    public MyPushImageCallback(String logFileId) {
        this.logFileId =logFileId;
    }

    @Override
    public void onNext(PushResponseItem item) {MDC.put("logFileId", logFileId);
        if (item.isErrorIndicated()) {
            this.error = item.getError();
        }
        logBuffer.tryLog(item);
    }

    public String getError() {
        return error;
    }
}