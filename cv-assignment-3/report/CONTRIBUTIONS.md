# 小组分工说明 — 《计算机视觉》作业 3

> 本文件按作业要求,详细说明每位成员的工作及贡献。请各组员根据实际情况填写下表。

## 组员信息

| 序号 | 学号 | 姓名 | 角色 |
|------|------|------|------|
| 1    |      |      | 组长 |
| 2    |      |      | 组员 |
| 3    |      |      | 组员 |

---

## 任务拆解与负责人

| 任务模块 | 对应文件 / 章节 | 负责人 | 贡献占比 |
|----------|----------------|--------|----------|
| 项目结构搭建 / 环境配置 (conda, requirements)         | `environment.yml`, `requirements.txt`, `README.md` |        | %      |
| 数据加载与 trimap → 二值 mask 转换                     | `src/data_utils.py`, notebook §2-3                 |        | %      |
| 传统方法 1:Otsu 阈值分割                              | `src/traditional.py::segment_otsu`                 |        | %      |
| 传统方法 2:Canny 边缘 + 形态学                        | `src/traditional.py::segment_canny_morph`          |        | %      |
| 传统方法 3:MeanShift(可选)                          | `src/traditional.py::segment_meanshift`            |        | %      |
| 深度学习:FCN-ResNet50 预训练推理                     | `src/dl_models.py`, notebook §5                    |        | %      |
| 评价指标实现:IoU / Dice / Acc / Precision / Recall / F1 | `src/metrics.py`, notebook §6                     |        | %      |
| 可视化:成功/失败样本 + 误差图                        | `src/visualize.py`, notebook §7                    |        | %      |
| (可选)FCN 轻量微调                                  | notebook §8                                        |        | %      |
| 项目报告撰写                                         | `report/REPORT_TEMPLATE.md`                        |        | %      |
| 实验数据汇总 / 图表绘制                              | notebook §6 输出                                   |        | %      |
| 最终打包与提交                                       | —                                                  |        | %      |

> 合计:**100%**

---

## 具体工作描述(每人一段)

### 组员 1 — XXX(学号 XXXXXXXX)

负责 / 完成的工作:

- 例:搭建项目骨架,编写 conda `environment.yml` 与 README,验证 macOS 与 Windows 上的可复现性
- 例:实现 `src/data_utils.py`,完成 trimap → 二值 mask 转换、Resize 流程与张量批构造

### 组员 2 — XXX(学号 XXXXXXXX)

- 例:实现 Otsu 与 Canny+形态学两种传统方法,完成参数调优实验
- 例:在 60 张测试图上完成实验,负责报告 §4 实验结果撰写

### 组员 3 — XXX(学号 XXXXXXXX)

- 例:加载 FCN-ResNet50 预训练权重并实现推理流程,完成可选的 classifier 头微调
- 例:负责评价指标模块与可视化模块,产出报告 §4.3 中所有图

---

## 协作方式

- 代码版本管理:Git(分支策略:`main` 用于稳定版本,每个成员在 `feature/<姓名>-<模块>` 分支开发)
- 实验数据共享:notebook 输出 + `outputs/` 目录,统一保存到云盘
- 沟通:每周一次会议,日常用 IM 同步进度
