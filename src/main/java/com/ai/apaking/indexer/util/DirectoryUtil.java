package com.ai.apaking.indexer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 类型说明: 目录操作工具类
 *
 * @author guh
 *         create time 2016/3/18 15:41
 */
public class DirectoryUtil {

    private static Logger log = LoggerFactory.getLogger(DirectoryUtil.class);


    private DirectoryUtil() {
    }

    /**
     * 对目录重命名
     *
     * @param fromDir 原始目录
     * @param toDir   重命名目录
     * @return is succeed
     */
    public static boolean renameDirectory(String fromDir, String toDir) {

        File from = new File(fromDir);

        if (!from.exists() || !from.isDirectory()) {
            log.info("Directory does not exist: " + fromDir);
            return false;
        }

        File to = new File(toDir);
        return from.renameTo(to);

    }


    /**
     * 删除目录
     *
     * @param dir 目录
     * @return 是否成功
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


    /**
     * 获得目录大小.
     *
     * @param file 目录
     * @return size of direction
     */
    public static long getDirSize(File file) {
        //判断文件是否存在
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                if (children != null) {
                    for (File f : children) {
                        size += getDirSize(f);
                    }
                }
                return size;
            } else {
                //如果是文件则直接返回其大小,以“兆”为单位
                long size = file.length();
                return size;
            }
        } else {
            return 0;
        }
    }

}
