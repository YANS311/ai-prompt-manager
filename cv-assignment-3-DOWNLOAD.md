# 下载并在本地运行《计算机视觉》作业 3

本仓库中的 `cv-assignment-3/` 是一个可独立运行的工程,
已经打包成 `dist/cv-assignment-3.zip` 方便下载到本地。

---

## 方式 A:直接下载 zip(推荐,最快)

### 1. 下载 zip

在浏览器中访问下方链接并点击 **Download / Raw**:

> 在 GitHub 网页里:
> 进入分支 `cursor/cv-assignment-pet-segmentation-1e3a`
> → 打开 `dist/cv-assignment-3.zip`
> → 点击右上角 **Download raw file**

或者用命令行:

```bash
# 在你想保存的目录执行(把 <user>/<repo> 换成本仓库的 owner/name)
curl -L -o cv-assignment-3.zip \
  "https://raw.githubusercontent.com/<user>/<repo>/cursor/cv-assignment-pet-segmentation-1e3a/dist/cv-assignment-3.zip"
```

### 2. 解压

- **macOS**:双击 `cv-assignment-3.zip`,或终端 `unzip cv-assignment-3.zip`
- **Windows**:右键 → "全部解压"

得到目录 `cv-assignment-3/`,里面就是完整工程。

### 3. 创建 conda 环境并运行

```bash
cd cv-assignment-3
conda env create -f environment.yml
conda activate cv-pet-seg
```

### 4. 在 VS Code 中打开

1. 用 VS Code 打开 `cv-assignment-3` 文件夹
2. 安装插件(若尚未安装):**Python**、**Jupyter**(Microsoft 官方)
3. 打开 `notebooks/pet_segmentation.ipynb`
4. **右上角 Select Kernel → Python Environments → 选 `cv-pet-seg`**
5. 按 `Shift+Enter` 逐个 cell 执行即可

---

## 方式 B:`git clone`(适合熟悉 git 的同学)

```bash
git clone -b cursor/cv-assignment-pet-segmentation-1e3a \
  https://github.com/<user>/<repo>.git
cd <repo>/cv-assignment-3
conda env create -f environment.yml
conda activate cv-pet-seg
# 之后步骤同上
```

---

## 方式 C:GitHub 网页打包整个分支

打开仓库 → 切到分支 `cursor/cv-assignment-pet-segmentation-1e3a`
→ 绿色 **Code** 按钮 → **Download ZIP**
→ 解压后进入 `cv-assignment-3/` 子目录,后续与方式 A 第 3 步开始一致。

---

## 数据集说明

首次运行 notebook 时,`torchvision` 会自动下载 Oxford-IIIT Pet Dataset (~800MB) 到
`cv-assignment-3/data/oxford-iiit-pet/`。

下载较慢的话,可使用作业提供的网盘:

```
分享名称: OxfordPets
分享链接: https://kod.cuc.edu.cn/#s/EC0djoW2
提取密码: DDO30
```

把 `images.tar.gz` 与 `annotations.tar.gz` 放进 `cv-assignment-3/data/oxford-iiit-pet/` 后再运行即可。

---

## 常见问题

| 现象 | 解决办法 |
|------|----------|
| `conda env create` 找不到 pytorch | 已在 `environment.yml` 中加入 `conda-forge`,如仍失败,退回 pip:`pip install -r requirements.txt` |
| VS Code 看不到 `cv-pet-seg` kernel | 激活环境后执行 `python -m ipykernel install --user --name cv-pet-seg`,重启 VS Code |
| `OxfordIIITPet` AttributeError | 升级 `torchvision >= 0.15` |
| 网络下载慢 | 用方式 A 的 zip,或用上面的 CUC 网盘手动准备数据集 |
