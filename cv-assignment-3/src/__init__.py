"""计算机视觉作业 3 \u2014 Oxford-IIIT Pet Dataset 图像分割实验工具包。

子模块在按需 import 时才会加载其依赖项 (避免单独使用 metrics 时强制依赖 PIL/cv2/torch)。
使用示例:
    from src import data_utils, traditional, dl_models, metrics, visualize
"""

__all__ = ["data_utils", "traditional", "dl_models", "metrics", "visualize"]
