"""分割任务评价指标。

输入 mask 约定:
    - HxW numpy 数组, dtype 任意, 取值 {0, 255} 或 {0,1} 均可
    - 内部统一转成 bool

提供:
    - iou(pred, gt)
    - dice(pred, gt)
    - pixel_accuracy(pred, gt)
    - recall(pred, gt)
    - precision(pred, gt)
    - f1(pred, gt)
    - compute_all(pred, gt)
    - aggregate_metrics(pairs, method_name)  -> 字典,含 mean/median 等汇总
"""
from __future__ import annotations

from dataclasses import dataclass, field, asdict
from typing import Dict, Iterable, List, Tuple

import numpy as np


def _to_bool(mask: np.ndarray) -> np.ndarray:
    return mask > 0


def iou(pred: np.ndarray, gt: np.ndarray, eps: float = 1e-7) -> float:
    p = _to_bool(pred)
    g = _to_bool(gt)
    inter = np.logical_and(p, g).sum()
    union = np.logical_or(p, g).sum()
    return float((inter + eps) / (union + eps))


def dice(pred: np.ndarray, gt: np.ndarray, eps: float = 1e-7) -> float:
    p = _to_bool(pred)
    g = _to_bool(gt)
    inter = np.logical_and(p, g).sum()
    return float((2 * inter + eps) / (p.sum() + g.sum() + eps))


def pixel_accuracy(pred: np.ndarray, gt: np.ndarray) -> float:
    return float((_to_bool(pred) == _to_bool(gt)).mean())


def recall(pred: np.ndarray, gt: np.ndarray, eps: float = 1e-7) -> float:
    p = _to_bool(pred)
    g = _to_bool(gt)
    tp = np.logical_and(p, g).sum()
    return float((tp + eps) / (g.sum() + eps))


def precision(pred: np.ndarray, gt: np.ndarray, eps: float = 1e-7) -> float:
    p = _to_bool(pred)
    g = _to_bool(gt)
    tp = np.logical_and(p, g).sum()
    return float((tp + eps) / (p.sum() + eps))


def f1(pred: np.ndarray, gt: np.ndarray, eps: float = 1e-7) -> float:
    pr = precision(pred, gt, eps)
    rc = recall(pred, gt, eps)
    return float(2 * pr * rc / (pr + rc + eps))


@dataclass
class SampleMetrics:
    iou: float
    dice: float
    pixel_acc: float
    precision: float
    recall: float
    f1: float
    filename: str = ""


def compute_all(pred: np.ndarray, gt: np.ndarray, filename: str = "") -> SampleMetrics:
    return SampleMetrics(
        iou=iou(pred, gt),
        dice=dice(pred, gt),
        pixel_acc=pixel_accuracy(pred, gt),
        precision=precision(pred, gt),
        recall=recall(pred, gt),
        f1=f1(pred, gt),
        filename=filename,
    )


def aggregate(metrics_list: Iterable[SampleMetrics]) -> Dict[str, float]:
    metrics_list = list(metrics_list)
    if not metrics_list:
        return {}
    keys = ["iou", "dice", "pixel_acc", "precision", "recall", "f1"]
    out: Dict[str, float] = {}
    for k in keys:
        vals = np.array([getattr(m, k) for m in metrics_list], dtype=np.float64)
        out[f"mean_{k}"] = float(vals.mean())
        out[f"median_{k}"] = float(np.median(vals))
        out[f"std_{k}"] = float(vals.std())
    out["n_samples"] = len(metrics_list)
    return out


def metrics_to_table(by_method: Dict[str, List[SampleMetrics]]) -> str:
    """生成简单 Markdown 表格,便于报告复制。"""
    header = (
        "| 方法 | mean IoU | mean Dice | mean Pixel Acc | mean Precision | mean Recall | mean F1 |\n"
        "|------|----------|-----------|----------------|----------------|-------------|---------|\n"
    )
    rows = []
    for name, lst in by_method.items():
        agg = aggregate(lst)
        rows.append(
            f"| {name} | {agg['mean_iou']:.3f} | {agg['mean_dice']:.3f} | "
            f"{agg['mean_pixel_acc']:.3f} | {agg['mean_precision']:.3f} | "
            f"{agg['mean_recall']:.3f} | {agg['mean_f1']:.3f} |"
        )
    return header + "\n".join(rows)
