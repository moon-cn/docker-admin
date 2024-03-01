package cn.moon.docker.core.log;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;


@RestController
@RequestMapping("api/log/{logId}")
public class LogController {


    @GetMapping
    public ResponseEntity<StreamingResponseBody> log(@PathVariable("logId") String logId) throws Exception {

        StreamingResponseBody responseBody = os -> {
            File file = LogConstants.getLogPath(logId);
            PrintWriter writer = new PrintWriter(os);
            writer.println("日志文件 " + file.getAbsolutePath());

            if (!file.exists()) {
                writer.write("日志文件已被清理" + file.getAbsolutePath());
                writer.flush();
                return;
            }


            RandomAccessFile accessFile = new RandomAccessFile(file, "r");
            String line;

            boolean alive= true;
            do {
                while ((line = accessFile.readLine()) != null) {
                    byte[] bytes = line.getBytes(StandardCharsets.ISO_8859_1);
                    String lineUTF8 = new String(bytes, StandardCharsets.UTF_8);

                    writer.println(lineUTF8);
                }
                writer.flush();

                try {
                    Thread.sleep(1000 * 5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                alive = System.currentTimeMillis() - file.lastModified() < 1000 * 60 * 5;
                writer.write(".");
            }while (alive);


            accessFile.close();
        };
        // 响应到客户端
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void streamLogs(@PathVariable("logId") String logId, HttpServletResponse response) throws IOException {
        // 设置 SSE 专用的响应头
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Type", "text/event-stream");
        response.setHeader("Connection", "keep-alive");
        response.setCharacterEncoding("UTF-8");


        // 设置日志文件路径
        File file = LogConstants.getLogPath(logId);

        if (!file.exists()) {
            sendSseMessage(response,"日志文件已被清理" + file.getAbsolutePath());
            return;
        }


        RandomAccessFile accessFile = new RandomAccessFile(file, "r");

        sendSseMessage(response, accessFile);

        // 创建 WatchService 监视日志文件的修改
        WatchService watchService = FileSystems.getDefault().newWatchService();

        // 注册日志文件所在目录到 WatchService
        Path logFileDir = file.toPath().getParent();
        logFileDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        // 在连接保持活跃状态期间，持续发送新的日志行
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 接收 WatchService 的事件
                WatchKey watchKey = watchService.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        //获取目录下新增的文件名
                        String fileName = event.context().toString();

                        System.out.println(fileName);

                        sendSseMessage(response, accessFile);
                    }
                }

                // 重置 WatchKey，以便可以接收后续的事件
                watchKey.reset();
            } catch (InterruptedException e) {
                break;
            }
        }
        accessFile.close();
    }

    private void sendSseMessage(HttpServletResponse response, RandomAccessFile accessFile) throws IOException {
        String line;
        while ((line = accessFile.readLine()) != null) {
            byte[] bytes = line.getBytes(StandardCharsets.ISO_8859_1);
            String lineUTF8 = new String(bytes, StandardCharsets.UTF_8);
            sendSseMessage(response, lineUTF8);
        }
    }


    private void sendSseMessage(HttpServletResponse response, String message) throws IOException {
        response.getWriter().write("data: " + message + "\n\n");
        response.flushBuffer();
    }

}
