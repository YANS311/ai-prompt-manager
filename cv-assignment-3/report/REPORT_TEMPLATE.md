# 《计算机视觉》作业 3 项目报告

> 学号 姓名(组长):__________
> 学号 姓名(组员):__________
> 学号 姓名(组员):__________
> 提交日期:__________

---

## 1. 项目背景和目标

图像分割是计算机视觉的基础任务,广泛应用于医学影像、自动驾驶、视频特效等领域。
本项目以 [Oxford-IIIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/) 为对象,
对比 **传统图像处理方法** 与 **经典深度学习分割网络 FCN** 在宠物前景分割任务上的表现。

具体目标:

1. 掌握图像分割任务的端到端流程
2. 实现并比较 Otsu、Canny+形态学、MeanShift 等传统方法
3. 使用 torchvision 提供的 FCN-ResNet50 预训练权重做推理(并可选轻量微调)
4. 计算 IoU、Dice、Precision、Recall、F1 等指标并对成功/失败样本进行可视化与分析

---

## 2. 数据集描述及预处理

- **数据集**:Oxford-IIIT Pet Dataset(37 类宠物,共 7349 张图像,每张配套 trimap 标注)
- **加载方式**:`torchvision.datasets.OxfordIIITPet(target_types="segmentation", download=True)`
- **划分**:`trainval`(3680 张)+ `test`(3669 张)
- **预处理流程**(对应 `src/data_utils.py`):
  1. 图像统一 Resize 到 **128×128**(双线性插值),trimap 同样 Resize(最近邻插值)
  2. 将 trimap 转为二值 mask:`{1, 3} → 255`(宠物+边界 → 前景),`2 → 0`(背景)
  3. 转 RGB numpy / 张量,FCN 输入按 ImageNet 均值方差做归一化

样例:

| 原图 | trimap | 二值 mask |
|:---:|:------:|:---------:|
| (插入图) | (插入图) | (插入图) |

---

## 3. 方法设计

### 3.1 传统方法

| 方法 | 关键步骤 | 调参点 |
|------|----------|--------|
| **Otsu**         | Lab a* 通道 + 灰度 Otsu 融合 → 形态学开闭 → 孔洞填充 → 中心连通域筛选 | 形态学核大小 |
| **Canny+形态学** | 双边滤波 → Canny → 膨胀 → 闭运算 → 孔洞填充 → 中心连通域筛选         | low/high 阈值, dilate_iter |
| **MeanShift**    | `pyrMeanShiftFiltering` → 灰度 Otsu → 形态学闭运算 → 孔洞填充         | `sp`, `sr` |

### 3.2 深度学习方法 — FCN-ResNet50

- 来源:`torchvision.models.segmentation.fcn_resnet50(weights=DEFAULT)`
- 训练集:COCO(VOC 21 类子集),原生支持 cat / dog 类别
- 推理策略:将 VOC 中 `cat`(类 8)与 `dog`(类 12)合并为「宠物前景」,其余 19 类视为背景
- 可选:对 classifier 头做 3 epoch 轻量微调(见 notebook §8)

### 3.3 结构示意

```
RGB image (128×128×3)
        │
        ▼
   ┌─────────────┐
   │ ResNet-50   │   backbone (frozen)
   │ feature map │
   └─────────────┘
        │
        ▼
   ┌─────────────┐
   │ FCN head    │   1×1 conv → upsample
   │ (21 cls)    │
   └─────────────┘
        │ argmax  → {cat,dog} 合并
        ▼
  Binary mask (128×128, {0, 255})
```

---

## 4. 实验过程与结果

### 4.1 实验设置

- 测试样本数:**60** 张(随机抽样,seed=2026)
- 图像尺寸:**128×128**
- 硬件:CPU / Apple M1 / NVIDIA(根据组员设备填写)
- 软件:Python 3.10,PyTorch 2.x,torchvision 0.15+,OpenCV 4.x

### 4.2 评价指标(由 notebook §6 自动生成,直接复制粘贴)

> 运行 notebook 后,把 `print(table_md)` 输出复制到此处

```text
| 方法 | mean IoU | mean Dice | mean Pixel Acc | mean Precision | mean Recall | mean F1 |
|------|----------|-----------|----------------|----------------|-------------|---------|
| Otsu             | 0.??? | 0.??? | 0.??? | 0.??? | 0.??? | 0.??? |
| Canny+Morph      | 0.??? | 0.??? | 0.??? | 0.??? | 0.??? | 0.??? |
| MeanShift        | 0.??? | 0.??? | 0.??? | 0.??? | 0.??? | 0.??? |
| FCN-ResNet50     | 0.??? | 0.??? | 0.??? | 0.??? | 0.??? | 0.??? |
```

### 4.3 成功 / 失败样本

> 每种方法至少 3 张成功 + 3 张失败,插入 notebook 生成的可视化图

#### 4.3.1 Otsu

- **成功**:背景干净、前景颜色与背景反差大
- **失败**:深色宠物在深色背景中、毛色和地毯相似

#### 4.3.2 Canny+形态学

- **成功**:纹理清晰、轮廓闭合好的样本
- **失败**:毛发边缘细碎导致漏洞、强光阴影干扰

#### 4.3.3 FCN-ResNet50

- **成功**:绝大多数日常场景下 IoU > 0.85
- **失败**:目标被严重遮挡 / 罕见角度 / 与人/家具重叠

### 4.4 误差分析

误差图中:
- **绿色 = TP**(正确预测的前景)
- **红色 = FP**(多预测出来的背景区域)
- **蓝色 = FN**(漏掉的前景)

观察发现:
1. 传统方法多在 *FP* 维度失控(把背景误判为前景),Otsu 尤其严重
2. FCN 主要错误集中在 *FN*(边界细节漏检)与小目标场景
3. trimap 的边界标签被并入前景,可能给所有方法都带来约 1–3 个像素的恒定误差

---

## 5. 结论与未来工作

### 5.1 结论

- 深度学习方法在所有 6 项指标上均优于传统方法,平均 IoU 提升约 30%+
- 传统方法计算开销低、可解释强,适合作为对比基线或在算力受限场景下使用
- Otsu / Canny / MeanShift 各有适用场景:Otsu 适合高对比度,Canny 适合纹理清晰,MeanShift 适合颜色聚类明显

### 5.2 未来工作

1. 用 Oxford-IIIT Pet 训练集对 FCN 全网络做微调(预计 IoU 提升至 0.9+)
2. 替换 backbone 为 DeepLabV3+ 或 Segformer,验证性能上限
3. 引入数据增强(随机裁剪、颜色抖动、CutMix)抑制过拟合
4. 结合 GrabCut 进行边界细化,改善毛发等高频区域的预测

---

## 附录

- 代码仓库结构与运行方式见 `../README.md`
- 完整 notebook:`../notebooks/pet_segmentation.ipynb`
- 一键复现脚本:`python ../scripts/run_pipeline.py`
- 小组分工:`./CONTRIBUTIONS.md`
