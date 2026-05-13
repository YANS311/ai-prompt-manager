# 数据目录

Oxford-IIIT Pet Dataset 会在首次运行 notebook 或 `scripts/run_pipeline.py` 时
自动下载到 `data/oxford-iiit-pet/` 子目录。

如自动下载较慢,请使用作业提供的网盘链接:

```
分享名称: OxfordPets
分享链接: https://kod.cuc.edu.cn/#s/EC0djoW2
提取密码: DDO30
```

下载后将以下两个压缩包放到 `data/oxford-iiit-pet/` 即可:

- `images.tar.gz`
- `annotations.tar.gz`

无需手动解压,torchvision 会自动完成。
