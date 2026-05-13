"""数据加载与预处理工具。

主要功能:
1. 加载 torchvision.datasets.OxfordIIITPet 数据集 (trimap 标注)。
2. 将 trimap 标注转换为二值前景 mask:
       trimap 中 1 = 前景宠物, 2 = 背景, 3 = 边界
       本作业按要求将"宠物主体 + 边界"视为前景 (255),其余为背景 (0)。
3. 提供统一的 Resize / to-numpy 工具,保证传统方法与深度学习方法使用相同的输入尺寸。
"""
from __future__ import annotations

import os
from dataclasses import dataclass
from typing import List, Tuple

import numpy as np
from PIL import Image

try:
    import torchvision
    from torchvision.datasets import OxfordIIITPet
    from torchvision import transforms
except Exception as e:  # pragma: no cover - 仅在 torchvision 未安装时触发
    raise ImportError(
        "需要 torchvision >= 0.15,请先运行 `conda env create -f environment.yml`"
    ) from e


# Oxford-IIIT Pet Dataset trimap 含义:
#   1 -> 前景 (pet)
#   2 -> 背景 (background)
#   3 -> 边界/不确定 (boundary)
# 任务要求: 前景 + 边界 -> 255, 背景 -> 0
TRIMAP_FOREGROUND = 1
TRIMAP_BACKGROUND = 2
TRIMAP_BOUNDARY = 3


@dataclass
class Sample:
    """一条已经预处理好的数据。

    属性:
        image_rgb:  HxWx3 uint8, RGB 通道,数值范围 0~255
        gt_mask:    HxW   uint8, 真实二值 mask, 0 / 255
        filename:   原文件名,便于可视化时溯源
    """

    image_rgb: np.ndarray
    gt_mask: np.ndarray
    filename: str


def trimap_to_binary_mask(trimap: np.ndarray) -> np.ndarray:
    """trimap (HxW) -> 二值 mask (HxW), dtype=uint8, 取值 0 / 255。"""
    if trimap.ndim == 3:
        trimap = trimap[..., 0]
    mask = np.where(
        (trimap == TRIMAP_FOREGROUND) | (trimap == TRIMAP_BOUNDARY), 255, 0
    ).astype(np.uint8)
    return mask


def _pil_to_numpy_image(img: Image.Image, size: int) -> np.ndarray:
    img = img.convert("RGB").resize((size, size), Image.BILINEAR)
    return np.asarray(img, dtype=np.uint8)


def _pil_to_numpy_trimap(trimap: Image.Image, size: int) -> np.ndarray:
    # trimap 必须使用最近邻插值,避免引入新的标签值
    trimap = trimap.resize((size, size), Image.NEAREST)
    arr = np.asarray(trimap)
    # torchvision 的 OxfordIIITPet 可能返回 mode='L' (单通道整数图),取值 {1,2,3}
    if arr.ndim == 3:
        arr = arr[..., 0]
    return arr.astype(np.uint8)


def load_oxford_pet_dataset(
    root: str,
    split: str = "trainval",
    download: bool = True,
) -> OxfordIIITPet:
    """加载 Oxford-IIIT Pet Dataset (segmentation)。

    参数:
        root: 数据存放根目录 (相对/绝对路径均可)
        split: 'trainval' 或 'test'
        download: 是否自动下载 (首次运行需要 True)
    返回:
        torchvision OxfordIIITPet 数据集对象,target 为 trimap PIL.Image。
    """
    os.makedirs(root, exist_ok=True)
    ds = OxfordIIITPet(
        root=root,
        split=split,
        target_types="segmentation",
        download=download,
    )
    return ds


def build_sample_pool(
    dataset: OxfordIIITPet,
    image_size: int = 128,
    max_samples: int | None = None,
    seed: int = 42,
) -> List[Sample]:
    """从数据集中预处理出统一尺寸的 Sample 列表,方便后续多种方法重复使用。"""
    n = len(dataset) if max_samples is None else min(max_samples, len(dataset))
    rng = np.random.default_rng(seed)
    indices = rng.permutation(len(dataset))[:n]

    samples: List[Sample] = []
    for idx in indices:
        img_pil, trimap_pil = dataset[int(idx)]
        img = _pil_to_numpy_image(img_pil, image_size)
        trimap = _pil_to_numpy_trimap(trimap_pil, image_size)
        mask = trimap_to_binary_mask(trimap)
        fname = os.path.basename(dataset._images[int(idx)])
        samples.append(Sample(image_rgb=img, gt_mask=mask, filename=fname))
    return samples


def build_tensor_batch(samples: List[Sample]) -> Tuple["torch.Tensor", List[np.ndarray]]:
    """将 Sample 列表转成 torch 推理用的 NCHW 浮点张量 (ImageNet 归一化)。

    返回 (input_tensor, gt_mask_list)。
    """
    import torch

    mean = np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std = np.array([0.229, 0.224, 0.225], dtype=np.float32)

    imgs = np.stack([s.image_rgb.astype(np.float32) / 255.0 for s in samples], axis=0)
    imgs = (imgs - mean) / std  # NHWC
    imgs = np.transpose(imgs, (0, 3, 1, 2)).copy()  # NCHW
    tensor = torch.from_numpy(imgs).float()
    gts = [s.gt_mask for s in samples]
    return tensor, gts
