package cn.moon.docker.core.log;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;


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


}
