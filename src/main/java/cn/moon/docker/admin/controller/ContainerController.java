package cn.moon.docker.admin.controller;

import cn.moon.docker.admin.service.HostService;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.StrUtil;
import cn.moon.docker.admin.entity.Host;
import cn.moon.docker.admin.service.ContainerService;
import cn.moon.base.tool.DownloadTool;
import cn.moon.base.tool.JsonTool;
import cn.moon.base.AjaxResult;
import cn.moon.docker.core.DockerManager;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@Slf4j
@RequestMapping(value = "api/container")
public class ContainerController {

    @Resource
    HostService hostService;

    @Resource
    private DockerManager dockerManager;

    @Resource
    private ContainerService containerService;


    @RequestMapping("get")
    public AjaxResult get(@RequestParam String hostId, String containerId) throws Exception {
        Host host = hostService.findOne(hostId);
        DockerClient client = dockerManager.getClient(host);


        InspectContainerResponse response = client.inspectContainerCmd(containerId).exec();

        String json = JsonTool.toPrettyJsonQuietly(response);

        json = Objects.requireNonNull(json).replaceAll("\\\\\"","").replaceAll("\"","");


        return  AjaxResult.success("获取容器信息成功",json);

    }


    @RequestMapping("stats")
    public AjaxResult stats(@RequestParam String hostId, String containerId) throws Exception {
        Host host = hostService.findOne(hostId);
        DockerClient client = dockerManager.getClient(host);


        StatsCmd cmd = client.statsCmd(containerId);


        List<Statistics> list = new ArrayList<>();
        ResultCallback.Adapter<Statistics> callback = cmd.exec(new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {
                super.onNext(statistics);
                list.add(statistics);
                System.out.println(statistics);
            }
        });

        // 为了计算cpu使用率，需至少两个，以计算用时
        while (list.size() < 2){
            Thread.sleep(1000);
        }
        callback.close();
        cmd.close();
        client.close();


        // https://docs.docker.com/engine/api/v1.43/#tag/Container/operation/ContainerStats
        Statistics st = list.get(1);

        long used_memory = st.getMemoryStats().getUsage() - Objects.requireNonNull(st.getMemoryStats().getStats()).getCache();
        long available_memory = st.getMemoryStats().getLimit();

        Map<String,Object> map = new LinkedHashMap<>();
        map.put("MEM USAGE / LIMIT", DataSizeUtil.format(st.getMemoryStats().getUsage())  +" / " +DataSizeUtil.format(st.getMemoryStats().getLimit()) );
        map.put("MEM 使用率", (used_memory * 1D / available_memory) * 100.0 + "%");

        // cpu
        CpuStatsConfig cpuStats = st.getCpuStats();
        CpuStatsConfig preCpuStats = st.getPreCpuStats();

        long cpu_delta = Objects.requireNonNull(cpuStats.getCpuUsage()).getTotalUsage() - Objects.requireNonNull(preCpuStats.getCpuUsage()).getTotalUsage();
        long system_cpu_delta = cpuStats.getSystemCpuUsage() - preCpuStats.getSystemCpuUsage();

        long  number_cpus = cpuStats.getOnlineCpus();

        map.put("CPU 个数", number_cpus);
        map.put("CPU 使用率", (cpu_delta * 1D / system_cpu_delta) * number_cpus * 100.0 + "%");





        return  AjaxResult.success("获取容器状态统计成功", map);

    }


    @RequestMapping("log/{hostId}/{containerId}")
    public void logByHost(@PathVariable String hostId, @PathVariable String containerId, HttpServletResponse response) throws Exception {
        DockerClient client = containerService.responseLog(hostId);


        PrintWriter out = response.getWriter();
        client.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .withTail(500)
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        String msg = new String(item.getPayload(), StandardCharsets.ISO_8859_1);
                        out.write(msg);
                        out.flush();
                    }
                }).awaitCompletion();

        System.out.println("日志结束");
    }

    @RequestMapping("downloadLog")
    public void downloadLog(String hostId, String containerId, HttpServletResponse response) throws Exception {
        Host host = hostService.findOne(hostId);

        DockerClient client = dockerManager.getClient(host);


        DownloadTool.setDownloadParam(containerId + ".log", null, response);

        OutputStream out = response.getOutputStream();
        client.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(true)
                .withFollowStream(false)
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        byte[] payload = item.getPayload();
                        try {
                            out.write(payload);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).awaitCompletion();

        out.close();
        client.close();


    }

    @RequestMapping("downloadFile")
    public void downloadFile(String hostId, String containerId, String file, HttpServletResponse response) throws Exception {
        log.info("进入下载文件");
        Host host = hostService.findOne(hostId);

        DockerClient client = dockerManager.getClient(host);

        InputStream is = client.copyArchiveFromContainerCmd(containerId, file).exec();


        DownloadTool.setDownloadParam(FilenameUtils.getName(file) + ".tar", null, response);

        IoUtil.copy(is, response.getOutputStream());
        is.close();
        client.close();
        log.info("文件下载结束");
    }


    @RequestMapping("remove")
    public AjaxResult removeContainer(String hostId, String containerId) throws IOException {
        Host host = hostService.findOne(hostId);
        DockerClient client = dockerManager.getClient(host);

        client.removeContainerCmd(containerId)
                .exec();

        client.close();
        return AjaxResult.success("删除容器成功");

    }

    @RequestMapping("stop")
    public AjaxResult stop(String hostId, String containerId) throws IOException {
        Host host = hostService.findOne(hostId);
        DockerClient client = dockerManager.getClient(host);
        client.stopContainerCmd(containerId)
                .exec();
        client.close();
        return AjaxResult.success("停止容器成功");
    }

    @RequestMapping("start")
    public AjaxResult start(String hostId, String containerId) throws IOException {
        Host host = hostService.findOne(hostId);

        DockerClient client = dockerManager.getClient(host);

        client.startContainerCmd(containerId)
                .exec();
        client.close();
        return AjaxResult.success("启动容器成功");
    }

    @RequestMapping("status")
    public AjaxResult status(String hostId, String appName) {
        Host host = hostService.findOne(hostId);


        DockerClient client = dockerManager.getClient(host);
        Map<String, String> appLabelFilter = dockerManager.getAppLabelFilter(appName);

        try {

            List<Container> list = client.listContainersCmd().withLabelFilter(appLabelFilter).withShowAll(true).exec();
            client.close();
            if (list.isEmpty()) {
                return AjaxResult.success("未知");
            }

            Container container = list.get(0);

            return AjaxResult.success(container.getStatus());
        } catch (Exception e) {

            return AjaxResult.success("未知");
        }

    }




    @RequestMapping("file")
    public AjaxResult file(String hostId, String containerId, @RequestParam(defaultValue = "/", required = false) String path) throws Exception {
        Host host = hostService.findOne(hostId);

        DockerClient client = dockerManager.getClient(host);

        ExecCreateCmdResponse response = client.execCreateCmd(containerId)
                .withCmd("sh", "-c", "ls -lt  " + path).withAttachStdout(true).exec();
        String execId = response.getId();


        List<FileVo> dirs = new ArrayList<>();
        List<FileVo> files = new ArrayList<>();


        System.out.println("路径 {}" + path);

        StringBuilder sb = new StringBuilder();
        client.execStartCmd(execId).exec(new ResultCallbackTemplate<ExecStartResultCallback, Frame>() {
            @Override
            public void onNext(Frame frame) {
                String str = new String(frame.getPayload());
                System.out.println(str);
                if (frame.getStreamType() != StreamType.STDOUT) {
                    return;
                }

                sb.append(new String(frame.getPayload()));
            }
        }).awaitCompletion();
        client.close();

        List<String> rs = StrUtil.splitTrim(sb.toString(), "\n");
        if (rs.size() > 0) {
            rs.remove(0);
        }


        for (String line : rs) {
            System.out.println(line);

            String[] parts = line.split("\\s+");


            String name = parts[parts.length - 1];
            String fullPath = path + (path.endsWith("/") ? "" : "/") + name;


            FileVo item = new FileVo();
            item.setDir(parts[0].startsWith("d"));
            item.setTitle(name);
            item.setKey(fullPath.replaceAll("/", "_"));
            item.setPath(fullPath);

            item.size = Long.parseLong(parts[4]);
            item.sizeFmt = DataSizeUtil.format(item.size);

            String time = parts[5] + " " + parts[6] + " "+ parts[7];



            item.time = time;




            boolean dir = item.isDir();
            if (dir) {
                dirs.add(item);
            } else {
                files.add(item);
            }
        }
        Map<String, List<FileVo>> data = new HashMap<>();
        data.put("dirs", dirs);
        data.put("files", files);

        return AjaxResult.success("获取文件列表成功", data);
    }

    @RequestMapping("cmd")
    public AjaxResult cmd(String hostId, String containerId,String cmd) throws Exception {
        Host host = hostService.findOne(hostId);

        DockerClient client = dockerManager.getClient(host);

        ExecCreateCmdResponse response = client.execCreateCmd(containerId)
                .withCmd("sh", "-c", cmd).withAttachStdout(true).exec();
        String execId = response.getId();





        System.out.println("执行命令 {}" + cmd);

        StringBuilder sb = new StringBuilder();
        client.execStartCmd(execId).exec(new ResultCallbackTemplate<ExecStartResultCallback, Frame>() {
            @Override
            public void onNext(Frame frame) {
                String str = new String(frame.getPayload());
                System.out.println(str);
                sb.append(new String(frame.getPayload()));
            }
        }).awaitCompletion();


        client.close();

        return AjaxResult.success("执行命令成功" + cmd, sb.toString());
    }


    @Data
    public static class FileVo {
        boolean dir;
        String key;
        String title;

        String path;
        String time;

        long size;
        String sizeFmt;
    }

}
