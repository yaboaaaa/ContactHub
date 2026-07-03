#!/bin/bash
# ============================================================
# build-docker-image.sh
# 构建 Docker 镜像，支持两种标签：
#   1. <IMAGE_NAME>:latest
#   2. <IMAGE_NAME>:<GIT_SHORT_SHA>
# ============================================================
set -e

# ---------- 从环境变量或 GitHub Actions 获取参数 ----------
IMAGE_TAG="${GITHUB_SHA::8}"                    # Git 提交短哈希（如 a1b2c3d4）
DOCKER_REGISTRY="${DOCKER_REGISTRY:-registry.cn-hangzhou.aliyuncs.com}"
DOCKER_NAMESPACE="${DOCKER_NAMESPACE:-${ACR_NAMESPACE}}"
IMAGE_NAME="${IMAGE_NAME:-contacthub}"

echo "=========================================="
echo "  Building Docker Image..."
echo "  Registry:   ${DOCKER_REGISTRY}"
echo "  Namespace:  ${DOCKER_NAMESPACE}"
echo "  Image:      ${IMAGE_NAME}"
echo "  Tag:        ${IMAGE_TAG}"
echo "=========================================="

# 构建镜像（同时打两个标签）
docker build \
  -t "${IMAGE_NAME}:latest" \
  -t "${IMAGE_NAME}:${IMAGE_TAG}" \
  -f docker/Dockerfile \
  .

echo "=========================================="
echo "  Build completed successfully!"
echo "  Image: ${IMAGE_NAME}:${IMAGE_TAG}"
echo "=========================================="