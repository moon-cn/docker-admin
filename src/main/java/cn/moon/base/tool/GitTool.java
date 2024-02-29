package cn.moon.base.tool;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

@Slf4j
public class GitTool {

    @AllArgsConstructor
    @Data
    public static class CloneResult {
        File dir;
        String codeMessage;
    }

    public static CloneResult clone(String url, String user, String password, String value) throws GitAPIException {

        String dirName = url.substring(url.lastIndexOf("/") + 1);
        dirName = dirName.replace(".git", "");

        File workDir = new File("/tmp/" + dirName + "/" + DateUtil.date().toString("yyyyMMddHHmmss"));

        long start = System.currentTimeMillis();

        // 是提交ID，就是指某一次具体的提交，
        boolean isCommitRef = value.length() == 40;

        log.info("工作目录为 {}", workDir.getAbsolutePath());
        log.info("获取代码 git clone {}", url);

        if (workDir.exists()) {
            workDir.delete();
        }


        CloneCommand cloneCommand = Git.cloneRepository()
                .setCloneSubmodules(true)
                .setURI(url)
                .setDirectory(workDir);

        // support public project by anon
        if (user != null && password != null) {
            UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(user, password);
            cloneCommand.setCredentialsProvider(provider);
        }

        if (!isCommitRef) {
            cloneCommand.setBranch(value);
        }
        Git git = cloneCommand.call();

        if (isCommitRef) {
            git.reset().setRef(value).setMode(ResetCommand.ResetType.HARD).call();
        }

        LogCommand logcmd = git.log();
        Iterable<RevCommit> logResult = logcmd.call();
        RevCommit next = logResult.iterator().next();

        String submitMessage = next.getFullMessage();
        log.info("代码日志：{}", submitMessage);


        git.close();
        log.info("代码获取完毕, 共 {} M", FileUtils.sizeOfDirectory(workDir) / 1024 / 1024);

        log.info("耗时：{} 秒", (System.currentTimeMillis() - start) / 1000);


        return new CloneResult(workDir, submitMessage);
    }

}
