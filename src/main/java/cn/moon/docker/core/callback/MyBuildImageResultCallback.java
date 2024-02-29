package cn.moon.docker.core.callback;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.BuildResponseItem;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class MyBuildImageResultCallback extends ResultCallbackTemplate<MyBuildImageResultCallback, BuildResponseItem> {

    private String imageId;

    private String error;
    private final String logFileId;

    LogBuffer logBuffer = new LogBuffer(log);


    public MyBuildImageResultCallback(String logFileId) {
        this.logFileId = logFileId;
    }

    @Override
    public void onNext(BuildResponseItem item) {
        MDC.put("logFileId", logFileId);
        if (item.isBuildSuccessIndicated()) {
            this.imageId = item.getImageId();
        } else if (item.isErrorIndicated()) {
            log.info("异常 {}", item.getError());
            this.error = item.getError();
        }
        logBuffer.tryLog(item);
    }


    /**
     * Awaits the image id from the response stream.
     *
     * @throws DockerClientException if the build fails.
     */
    public String awaitImageId() {
        try {
            awaitCompletion();
        } catch (InterruptedException e) {
            throw new DockerClientException("", e);
        }

        MDC.remove("logFileId");
        return getImageId();
    }

    private String getImageId() {
        if (imageId != null) {
            return imageId;
        }

        if (error == null) {
            throw new DockerClientException("Could not build image");
        }

        throw new DockerClientException("Could not build image: " + error);
    }
}