"""深度学习分割模型 (基于 torchvision 预训练权重)。

本实验默认使用 FCN-ResNet50 在 COCO_VOC 上的预训练权重进行推理:
    - VOC 类别中 "cat" 索引 = 8, "dog" 索引 = 12
    - 我们将 "cat" + "dog" 通道合并视为宠物前景,其余视为背景
    - 输出 HxW uint8 二值 mask (0 / 255)

同时提供 DeepLabV3-ResNet50 备选,接口完全一致。
"""
from __future__ import annotations

from typing import List

import numpy as np
import torch
import torch.nn.functional as F
from torchvision import models

# Pascal VOC 21 类索引,只有 cat / dog 是宠物
VOC_PET_CLASSES = {
    "cat": 8,
    "dog": 12,
}


def load_fcn(device: str = "cpu") -> torch.nn.Module:
    """加载 FCN-ResNet50 预训练权重 (VOC 21 类)。"""
    weights = models.segmentation.FCN_ResNet50_Weights.DEFAULT
    model = models.segmentation.fcn_resnet50(weights=weights)
    model.eval().to(device)
    return model


def load_deeplabv3(device: str = "cpu") -> torch.nn.Module:
    weights = models.segmentation.DeepLabV3_ResNet50_Weights.DEFAULT
    model = models.segmentation.deeplabv3_resnet50(weights=weights)
    model.eval().to(device)
    return model


@torch.inference_mode()
def predict_masks(
    model: torch.nn.Module,
    batch_tensor: torch.Tensor,
    device: str = "cpu",
    target_size: int | None = None,
) -> List[np.ndarray]:
    """对 NCHW 张量批量推理,返回每张图的 HxW uint8 二值 mask 列表。

    target_size: 若指定则把输出 resize 回原始尺寸 (默认就是输入尺寸)。
    """
    batch_tensor = batch_tensor.to(device)
    logits = model(batch_tensor)["out"]  # (N, 21, h, w)
    if target_size is not None and logits.shape[-1] != target_size:
        logits = F.interpolate(
            logits, size=(target_size, target_size), mode="bilinear", align_corners=False
        )

    cat_id = VOC_PET_CLASSES["cat"]
    dog_id = VOC_PET_CLASSES["dog"]
    pred_class = logits.argmax(dim=1)  # (N, h, w)
    pet_mask = ((pred_class == cat_id) | (pred_class == dog_id)).cpu().numpy()
    return [(m.astype(np.uint8) * 255) for m in pet_mask]
