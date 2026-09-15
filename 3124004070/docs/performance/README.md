# 性能分析记录

## 分析环境

- JProfiler 15.0.3，CPU Sampling，采样间隔 5 ms
- Oracle JDK 21.0.10，64 位 Windows 11
- 输入：`orig.txt` 与 `orig_0.8_add.txt`
- 每个版本预热等待 5 秒，随后持续执行真实相似度计算 25 秒
- JProfiler 对其中连续 15 秒进行 CPU 采样

## 首版瓶颈

首版的 `baselineDotProduct` 使用双层循环比较两个特征表中的键。若两个文本分别有 `U` 和 `V` 个不同的 N-gram，点积计算复杂度为 `O(U × V)`。长文本中该方法构成主要性能瓶颈。

首版基准：25.090 秒完成 242 次计算，吞吐量为 9.65 次/秒。

对应快照：`jprofiler-baseline.jps`。

## 优化方法与结果

优化后只遍历较小的特征表，并使用 `HashMap.get` 在另一张表中查找相同 N-gram。点积阶段的平均时间复杂度降为 `O(min(U, V))`，空间复杂度保持不变。

优化版基准：25.001 秒完成 17103 次计算，吞吐量为 684.08 次/秒。与首版相比，吞吐量约提升 70.89 倍；两版对样例输出均为 `0.90`。

对应快照：`jprofiler-optimized.jps`。

## 博客截图操作

1. 使用 JProfiler 打开 `jprofiler-baseline.jps`。
2. 进入 CPU Views，选择 Hot Spots，按 Time 排序。
3. 截图时保留 JProfiler 窗口、`baselineDotProduct` 方法和耗时占比。
4. 再打开 `jprofiler-optimized.jps`，以相同视图截取优化后热点。
5. 将截图分别保存为 `baseline-hotspots.png` 和 `optimized-hotspots.png`，放入本目录。
