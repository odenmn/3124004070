package com.odenmn.paperchecker;

/**
 * 论文查重程序的命令行入口。
 */
public final class Main {

    private Main() {
        // 工具类不需要创建实例。
    }

    /**
     * 启动查重程序，并通过非零退出码向调用方报告错误。
     *
     * @param args 原文文件、抄袭版文件和答案文件的绝对路径
     */
    public static void main(String[] args) {
        int exitCode = new PaperCheckerApplication().run(args, System.err);
        if (exitCode != PaperCheckerApplication.SUCCESS) {
            System.exit(exitCode);
        }
    }
}
