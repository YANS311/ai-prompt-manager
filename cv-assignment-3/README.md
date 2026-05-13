# 《计算机视觉》作业 3 — 基于 Oxford-IIIT Pet Dataset 的图像分割实验

> **传统图像处理方法 + 经典深度学习分割网络 FCN**
> 学生组队(人数不多于 3 人/队)

本项目使用 **Python + Jupyter Notebook + VS Code** 完成作业要求,
完整代码跨平台运行(已在 **macOS Apple Silicon / Intel** 与 **Windows 10/11** 上验证),
全部依赖通过 **Conda** 一键安装。

---

## 目录结构

```
cv-assignment-3/
├── environment.yml           # Conda 环境定义 (macOS / Windows / Linux 通用)
├── requirements.txt          # pip 备用安装方式
├── README.md                 # 本说明文件
├── src/                      # 可复用的 Python 模块
│   ├── __init__.py
│   ├── data_utils.py         # OxfordIIITPet 加载, trimap → 二值 mask, 张量批构造
│   ├── traditional.py        # Otsu / Canny+形态学 / MeanShift 三种传统方法
│   ├── dl_models.py          # FCN-ResNet50 / DeepLabV3 预训练推理
│   ├── metrics.py            # IoU / Dice / Pixel-Acc / Precision / Recall / F1
│   └── visualize.py          # 原图/真值/预测/误差图可视化
├── notebooks/
│   └── pet_segmentation.ipynb   # 主实验 notebook (按顺序执行即可)
├── scripts/
│   └── run_pipeline.py       # 命令行一键跑全流程(可选)
├── report/
│   ├── REPORT_TEMPLATE.md    # 项目报告大纲模板
│   └── CONTRIBUTIONS.md      # 小组分工说明模板
└── data/                     # 数据集存放目录 (首次运行自动下载)
```

---

## 1. 环境准备 (macOS / Windows / Linux 通用)

> 推荐使用 **Miniconda** 或 **Anaconda**。如未安装,请先到
> <https://docs.conda.io/en/latest/miniconda.html> 下载对应平台版本。

### 1.1 创建并激活环境

```bash
# 进入项目根目录
cd cv-assignment-3

# 一键创建 conda 环境 (名字: cv-pet-seg)
conda env create -f environment.yml

# 激活环境
conda activate cv-pet-seg
```

> Windows 用户请使用 **Anaconda Prompt** 或 **PowerShell**;
> 上述命令在 macOS / Windows / Linux 上完全一致。

### 1.2 在 VS Code 中打开

1. 安装 VS Code 插件:**Python**、**Jupyter**(均为 Microsoft 官方)
2. 启动 VS Code → `File → Open Folder…` → 选择 `cv-assignment-3/`
3. 打开 `notebooks/pet_segmentation.ipynb`
4. **右上角 `Select Kernel` → `Python Environments` → 选择 `cv-pet-seg`**
5. 按顺序 `Shift+Enter` 执行每个 cell 即可

### 1.3 (备选) 不用 Conda

```bash
python -m venv .venv
# Windows:  .venv\Scripts\activate
# macOS:    source .venv/bin/activate
pip install -r requirements.txt
```

---

## 2. 数据集说明

本实验使用 [Oxford-IIIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/)。
首次运行 notebook 时 `torchvision.datasets.OxfordIIITPet(..., download=True)` 会自动下载约 **800MB** 的数据到 `data/oxford-iiit-pet/`。

### 国内网盘备用 (作业提供)

```
分享名称: OxfordPets
分享链接: https://kod.cuc.edu.cn/#s/EC0djoW2
提取密码: DDO30
```

下载后将 `images.tar.gz` 与 `annotations.tar.gz` 放入

```
cv-assignment-3/data/oxford-iiit-pet/
```

再次运行 notebook 时,torchvision 会跳过下载、直接解压。

### Trimap → 二值 Mask

| trimap 值 | 含义       | 二值 mask |
|-----------|------------|-----------|
| 1         | 宠物前景   | 255       |
| 2         | 背景       | 0         |
| 3         | 边界/不确定 | 255       |

转换在 `src/data_utils.py::trimap_to_binary_mask` 中实现。

---

## 3. 一键运行 (notebook)

```bash
conda activate cv-pet-seg
cd cv-assignment-3
jupyter lab notebooks/pet_segmentation.ipynb   # 或在 VS Code 中直接打开
```

Notebook 顺序执行后将输出:

- 数据预览图
- 三种传统方法的预测 mask
- FCN-ResNet50 预测 mask
- IoU / Dice / Accuracy / Precision / Recall / F1 指标表 + 条形图
- 每种方法各 3 个成功样本 + 3 个失败样本的可视化(原图 / 真值 / 预测 / 误差图)

---

## 4. 一键运行 (命令行,可选)

```bash
conda activate cv-pet-seg
cd cv-assignment-3
python scripts/run_pipeline.py --num-test 60 --image-size 128 --device cpu
```

输出包括 Markdown 格式的指标表、`outputs/` 下的可视化 PNG 图。

---

## 5. 评分对照表

| 评分项     | 对应文件 / 位置                                           |
|------------|-----------------------------------------------------------|
| 数据预处理 | `src/data_utils.py` + notebook §2-3                       |
| 传统方法 ≥2 | `src/traditional.py`(Otsu / Canny+Morph / MeanShift)    |
| DL 方法 ≥1 | `src/dl_models.py`(FCN-ResNet50)                        |
| IoU / Dice | `src/metrics.py` + notebook §6                            |
| 成功/失败可视化 | `src/visualize.py` + notebook §7                     |
| 报告       | `report/REPORT_TEMPLATE.md`                              |
| 分工说明   | `report/CONTRIBUTIONS.md`                                |

---

## 6. 常见问题 (FAQ)

**Q1. 在 macOS Apple Silicon 上 `conda env create` 报找不到 pytorch?**
A:  `environment.yml` 已经把 `conda-forge` 设为 fallback channel,会自动选择适配 arm64 的 osx 版本。
    如仍失败,可改用 `pip install -r requirements.txt`。

**Q2. Windows 下载数据集卡住?**
A:  请使用上面提供的 CUC 网盘链接手动下载并放入 `data/oxford-iiit-pet/`,再重新运行 notebook。

**Q3. 报 `torchvision.datasets.OxfordIIITPet` AttributeError?**
A:  请确保 `torchvision >= 0.15`,旧版本没有该数据集。

**Q4. VS Code 找不到 kernel `cv-pet-seg`?**
A:  在已激活该环境的终端中执行 `python -m ipykernel install --user --name cv-pet-seg`,
    然后重启 VS Code 并重新 Select Kernel。

---

## 7. 提交清单

按作业要求,最终提交 3 个文件:

1. **项目代码 zip**:`《计算机视觉》作业3(学号 姓名).zip`(打包 `cv-assignment-3/` 即可)
2. **项目报告 pdf**:基于 `report/REPORT_TEMPLATE.md` 渲染
3. **分工说明 pdf**:基于 `report/CONTRIBUTIONS.md` 渲染
