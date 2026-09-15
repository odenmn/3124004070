# 第一次个人编程作业：论文查重

学号：3124004070

本项目使用 Java 实现论文查重程序。程序从命令行接收原文、抄袭版论文和答案文件的绝对路径，计算重复率并将结果以两位小数写入答案文件。

## 运行接口

```text
java -jar main.jar [原文文件] [抄袭版论文文件] [答案文件]
```

程序只读取前两个参数指定的文件，只写入第三个参数指定的文件，不访问网络。

## 环境要求

- JDK 8 或更高版本
- Windows、Linux 或 macOS

项目带有 Maven Wrapper，不要求预先安装 Maven。

## 构建与质量检查

Windows：

```text
mvnw.cmd clean verify
```

Linux/macOS：

```text
./mvnw clean verify
```

构建产物位于 `target/main.jar`。`verify` 会依次执行 21 个 JUnit 测试、JaCoCo 覆盖率检查、Checkstyle 和 SpotBugs。质量门槛要求分支覆盖率不低于 80%。

## 样例运行

```text
java -jar target/main.jar C:\tests\orig.txt C:\tests\orig_add.txt C:\tests\ans.txt
```

答案文件内容示例：

```text
0.90
```

## 实现说明

程序对文本执行 Unicode NFKC 规范化，过滤标点与空白后提取字符二元组频次，并计算频次向量的余弦相似度。字符二元组不依赖外部分词词典，适合离线中文文本，同时对段落换序、局部增删和标点变化具有较好的稳定性。

## 验证结果

- Java 8 兼容字节码（major version 52）
- 21 个测试全部通过
- 行覆盖率 96.23%，分支覆盖率 85.71%
- Checkstyle 0 项违规
- SpotBugs 0 错误、0 警告
- 有效样例输出 `0.90`，单次冷启动约 160 ms
- JProfiler 基准吞吐量由 9.65 次/秒提升至 684.08 次/秒

## Release

执行完整构建后，将 `target/main.jar` 上传到 GitHub Releases，并确保发布附件名称为 `main.jar`。
