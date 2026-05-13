"""可视化工具:原图 / 真值 / 预测 / 误差图。"""
from __future__ import annotations

from typing import Dict, List

import matplotlib.pyplot as plt
import numpy as np


def error_map(pred: np.ndarray, gt: np.ndarray) -> np.ndarray:
    """生成 RGB 误差图:
        绿色 = True Positive (正确预测的前景)
        红色 = False Positive (多预测)
        蓝色 = False Negative (漏预测)
        黑色 = True Negative
    """
    p = pred > 0
    g = gt > 0
    h, w = p.shape
    out = np.zeros((h, w, 3), dtype=np.uint8)
    out[np.logical_and(p, g)] = [0, 200, 0]
    out[np.logical_and(p, ~g)] = [220, 30, 30]
    out[np.logical_and(~p, g)] = [30, 60, 220]
    return out


def show_one_sample(
    image_rgb: np.ndarray,
    gt: np.ndarray,
    preds_by_method: Dict[str, np.ndarray],
    title: str = "",
    figsize_per_col: float = 2.6,
):
    """在一行内展示:原图 | GT | (每种方法: pred + error)。"""
    methods = list(preds_by_method.keys())
    n_cols = 2 + 2 * len(methods)
    fig, axes = plt.subplots(1, n_cols, figsize=(figsize_per_col * n_cols, figsize_per_col))
    if n_cols == 1:
        axes = [axes]

    axes[0].imshow(image_rgb)
    axes[0].set_title("Image")
    axes[1].imshow(gt, cmap="gray", vmin=0, vmax=255)
    axes[1].set_title("GT mask")

    for i, m in enumerate(methods):
        col_pred = 2 + 2 * i
        col_err = col_pred + 1
        axes[col_pred].imshow(preds_by_method[m], cmap="gray", vmin=0, vmax=255)
        axes[col_pred].set_title(f"{m}")
        axes[col_err].imshow(error_map(preds_by_method[m], gt))
        axes[col_err].set_title(f"{m} err")

    for ax in axes:
        ax.axis("off")
    if title:
        fig.suptitle(title, fontsize=11)
    fig.tight_layout()
    return fig


def show_gallery(
    samples: list,
    preds_by_method: Dict[str, List[np.ndarray]],
    indices: List[int],
    section_title: str = "",
):
    """连续展示多个样本 (按 indices 顺序)。"""
    for idx in indices:
        s = samples[idx]
        method_preds = {m: preds_by_method[m][idx] for m in preds_by_method}
        show_one_sample(
            s.image_rgb,
            s.gt_mask,
            method_preds,
            title=f"{section_title} | idx={idx} | {s.filename}",
        )
        plt.show()
