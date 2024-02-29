package cn.moon.base.tool;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.URLUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 文件下载工具类
 */
public class DownloadTool {



    public static void download(String fileName, byte[] fileBytes, HttpServletResponse response) {
        try {

            setDownloadParam(fileName, "" + fileBytes.length, response);
            IoUtil.write(response.getOutputStream(), true, fileBytes);
        } catch (IOException e) {
            throw new IllegalStateException("下载文件错误");
        }
    }

    public static void setDownloadParam(String fileName, String contentLength, HttpServletResponse response) {
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLUtil.encode(fileName) + "\"");
        if(contentLength != null){
            response.addHeader("Content-Length", contentLength);
        }

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/octet-stream;charset=UTF-8");
    }


}
