"""传统图像处理分割方法。

本模块实现了:
    1) Otsu 阈值分割 (在灰度 / a* / 显著性图上)
    2) Canny 边缘检测 + 形态学闭运算 + 孔洞填充
    3) MeanShift 聚类区域分割 (可选,作为第三个方法备选)

每个方法的输入都是 HxWx3 uint8 RGB 图像,
输出都是 HxW uint8 二值 mask,取值 {0, 255}。
"""
from __future__ import annotations

import cv2
import numpy as np
from scipy import ndimage as ndi


# ---------------------------------------------------------------
# 工具:中心连通域筛选
# ---------------------------------------------------------------
def _keep_center_components(mask: np.ndarray, min_area_ratio: float = 0.01) -> np.ndarray:
    """保留靠近图像中心、且面积占比 > min_area_ratio 的前景连通域。

    宠物图像中,被摄主体一般位于画面中心区域,该启发能有效抑制边缘噪点。
    """
    h, w = mask.shape
    binary = (mask > 0).astype(np.uint8)
    num, labels, stats, centroids = cv2.connectedComponentsWithStats(binary, connectivity=8)
    if num <= 1:
        return mask

    out = np.zeros_like(mask)
    cx, cy = w / 2.0, h / 2.0
    diag = np.sqrt(h * h + w * w)
    total = h * w

    for i in range(1, num):
        area = stats[i, cv2.CC_STAT_AREA]
        if area / total < min_area_ratio:
            continue
        # 距离图像中心越近权重越大;若组件触及到中心十字附近则保留
        dist = np.linalg.norm(centroids[i] - np.array([cx, cy])) / diag
        if dist < 0.45 or area / total > 0.05:
            out[labels == i] = 255
    if out.max() == 0:
        # 全部被过滤,则保留最大的那一个,避免输出全黑
        largest = 1 + np.argmax(stats[1:, cv2.CC_STAT_AREA])
        out[labels == largest] = 255
    return out


def _fill_holes(mask: np.ndarray) -> np.ndarray:
    return (ndi.binary_fill_holes(mask > 0).astype(np.uint8)) * 255


# ---------------------------------------------------------------
# 方法 1: Otsu 阈值分割
# ---------------------------------------------------------------
def segment_otsu(image_rgb: np.ndarray) -> np.ndarray:
    """基于 Lab 色彩空间 a* 通道 + 灰度通道融合的 Otsu 阈值分割。

    步骤:
        1) RGB -> Lab,取 a* 通道 (对粉/红色生物体响应较强)
        2) Otsu 自适应阈值
        3) 结合灰度 Otsu 进行融合,提升鲁棒性
        4) 形态学开闭运算 + 孔洞填充 + 中心连通域筛选
    """
    if image_rgb.ndim != 3:
        raise ValueError("Otsu 输入需为 RGB 图像")

    lab = cv2.cvtColor(image_rgb, cv2.COLOR_RGB2LAB)
    a_channel = lab[..., 1]
    gray = cv2.cvtColor(image_rgb, cv2.COLOR_RGB2GRAY)

    _, m1 = cv2.threshold(a_channel, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    _, m2 = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

    # 取两者并集,提高召回
    mask = cv2.bitwise_or(m1, m2)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel, iterations=1)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel, iterations=2)
    mask = _fill_holes(mask)
    mask = _keep_center_components(mask)
    return mask.astype(np.uint8)


# ---------------------------------------------------------------
# 方法 2: Canny 边缘 + 形态学
# ---------------------------------------------------------------
def segment_canny_morph(
    image_rgb: np.ndarray,
    low: int = 50,
    high: int = 150,
    dilate_iter: int = 2,
    close_iter: int = 3,
) -> np.ndarray:
    """Canny 边缘 -> 膨胀闭合 -> 孔洞填充 -> 中心连通域过滤。"""
    gray = cv2.cvtColor(image_rgb, cv2.COLOR_RGB2GRAY)
    gray = cv2.bilateralFilter(gray, d=5, sigmaColor=50, sigmaSpace=50)
    edges = cv2.Canny(gray, low, high)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3))
    edges = cv2.dilate(edges, kernel, iterations=dilate_iter)
    closed = cv2.morphologyEx(edges, cv2.MORPH_CLOSE, kernel, iterations=close_iter)
    filled = _fill_holes(closed)

    # 二次开运算去除细碎噪点
    opened = cv2.morphologyEx(filled, cv2.MORPH_OPEN, kernel, iterations=1)
    return _keep_center_components(opened).astype(np.uint8)


# ---------------------------------------------------------------
# 方法 3: MeanShift 区域分割 (备选)
# ---------------------------------------------------------------
def segment_meanshift(
    image_rgb: np.ndarray,
    spatial_radius: int = 15,
    color_radius: int = 25,
    max_pyramid_level: int = 1,
) -> np.ndarray:
    """OpenCV MeanShift 滤波 + Otsu 后处理。

    pyrMeanShiftFiltering 会把像素聚类到颜色 + 空间一致的区域,
    随后再做 Otsu 二值化,得到前景 mask。
    """
    shifted = cv2.pyrMeanShiftFiltering(
        image_rgb, sp=spatial_radius, sr=color_radius, maxLevel=max_pyramid_level
    )
    gray = cv2.cvtColor(shifted, cv2.COLOR_RGB2GRAY)
    _, mask = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel, iterations=2)
    mask = _fill_holes(mask)
    mask = _keep_center_components(mask)
    return mask.astype(np.uint8)


METHOD_REGISTRY = {
    "Otsu": segment_otsu,
    "Canny+Morph": segment_canny_morph,
    "MeanShift": segment_meanshift,
}


def run_traditional(method_name: str, image_rgb: np.ndarray) -> np.ndarray:
    if method_name not in METHOD_REGISTRY:
        raise KeyError(f"未注册的传统方法: {method_name}; 可选: {list(METHOD_REGISTRY)}")
    return METHOD_REGISTRY[method_name](image_rgb)
