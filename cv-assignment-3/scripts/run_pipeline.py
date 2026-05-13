"""命令行一键复现脚本(可选,主要工作流仍以 notebook 为准)。

用法示例:
    conda activate cv-pet-seg
    python scripts/run_pipeline.py --num-test 60 --image-size 128 --device cpu

将在 `outputs/` 下生成:
    - metrics_table.md         汇总指标表
    - vis_<method>_success_*.png / vis_<method>_failure_*.png  成功 / 失败样本可视化
"""
from __future__ import annotations

import argparse
import os
import sys
import time
import pathlib

import numpy as np
import torch
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

PROJECT_ROOT = pathlib.Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from src import data_utils, traditional, dl_models, metrics, visualize  # noqa: E402


def parse_args():
    p = argparse.ArgumentParser(description="Oxford-IIIT Pet 图像分割实验一键脚本")
    p.add_argument("--data-root", default=str(PROJECT_ROOT / "data"))
    p.add_argument("--output-dir", default=str(PROJECT_ROOT / "outputs"))
    p.add_argument("--image-size", type=int, default=128)
    p.add_argument("--num-test", type=int, default=60)
    p.add_argument(
        "--methods",
        nargs="+",
        default=["Otsu", "Canny+Morph", "MeanShift"],
        choices=list(traditional.METHOD_REGISTRY.keys()),
    )
    p.add_argument("--dl", default="fcn", choices=["fcn", "deeplabv3", "none"])
    p.add_argument("--device", default="cpu")
    p.add_argument("--seed", type=int, default=2026)
    return p.parse_args()


def main():
    args = parse_args()
    os.makedirs(args.output_dir, exist_ok=True)

    print("[1/5] 加载数据集 ...")
    test_ds = data_utils.load_oxford_pet_dataset(args.data_root, split="test", download=True)
    samples = data_utils.build_sample_pool(
        test_ds, image_size=args.image_size, max_samples=args.num_test, seed=args.seed
    )
    print(f"      使用 {len(samples)} 张测试图像 (size={args.image_size})")

    print("[2/5] 运行传统方法 ...")
    preds_by_method: dict = {}
    for m in args.methods:
        t0 = time.time()
        preds_by_method[m] = [traditional.run_traditional(m, s.image_rgb) for s in samples]
        print(f"      {m:<14s} done in {time.time() - t0:.1f}s")

    if args.dl != "none":
        print("[3/5] 运行 DL 推理 ...")
        if args.dl == "fcn":
            model = dl_models.load_fcn(device=args.device)
            dl_name = "FCN-ResNet50"
        else:
            model = dl_models.load_deeplabv3(device=args.device)
            dl_name = "DeepLabV3-ResNet50"
        batch, _ = data_utils.build_tensor_batch(samples)
        t0 = time.time()
        preds_by_method[dl_name] = dl_models.predict_masks(
            model, batch, device=args.device, target_size=args.image_size
        )
        print(f"      {dl_name} done in {time.time() - t0:.1f}s")

    print("[4/5] 计算指标 ...")
    metrics_by_method = {}
    for name, preds in preds_by_method.items():
        metrics_by_method[name] = [
            metrics.compute_all(p, s.gt_mask, s.filename)
            for p, s in zip(preds, samples)
        ]
    table_md = metrics.metrics_to_table(metrics_by_method)
    print("\n" + table_md + "\n")
    with open(os.path.join(args.output_dir, "metrics_table.md"), "w", encoding="utf-8") as f:
        f.write(table_md + "\n")

    print("[5/5] 输出可视化样本 ...")
    for method, ms in metrics_by_method.items():
        ious = np.array([m.iou for m in ms])
        order = np.argsort(ious)
        success = order[-3:][::-1].tolist()
        failure = order[:3].tolist()

        for tag, idx_list in [("success", success), ("failure", failure)]:
            for j, idx in enumerate(idx_list):
                s = samples[idx]
                fig = visualize.show_one_sample(
                    s.image_rgb,
                    s.gt_mask,
                    {method: preds_by_method[method][idx]},
                    title=f"{method} | {tag} #{j + 1} | IoU={ms[idx].iou:.3f} | {s.filename}",
                )
                safe = method.replace("+", "_").replace(" ", "_")
                out_path = os.path.join(args.output_dir, f"vis_{safe}_{tag}_{j + 1}.png")
                fig.savefig(out_path, dpi=120, bbox_inches="tight")
                plt.close(fig)
        print(f"      {method:<18s} -> 6 imgs saved")

    print(f"\n完成。结果保存在: {args.output_dir}")


if __name__ == "__main__":
    main()
