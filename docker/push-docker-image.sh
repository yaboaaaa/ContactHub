#!/bin/bash
# ============================================================
# push-docker-image.sh
# 将本地构建的 Docker 镜像推送到阿里云容器镜像服务 (ACR)
# ============================================================
set -e

# ---------- 从环境变量或 GitHub Actions 获取参数 ----------
IMAGE_TAG="${GITHUB_SHA::8}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-registry.cn-hangzhou.aliyuncs.com}"
DOCKER_NAMESPACE="${DOCKER_NAMESPACE:-${ACR_NAMESPACE}}"
IMAGE_NAME="${IMAGE_NAME:-contacthub}"

# 完整镜像地址
FULL_IMAGE_NAME="${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${IMAGE_NAME}"

echo "=========================================="
echo "  Pushing Docker Image to ACR..."
echo "  Full Image: ${FULL_IMAGE_NAME}"
echo "  Tags:       latest, ${IMAGE_TAG}"
echo "=========================================="

# 为远程仓库重新标记本地镜像
docker tag "${IMAGE_NAME}:latest"    "${FULL_IMAGE_NAME}:latest"
docker tag "${IMAGE_NAME}:${IMAGE_TAG}" "${FULL_IMAGE_NAME}:${IMAGE_TAG}"

# 推送镜像
docker push "${FULL_IMAGE_NAME}:latest"
docker push "${FULL_IMAGE_NAME}:${IMAGE_TAG}"

echo "=========================================="
echo "  Push completed successfully!"
echo "  ${FULL_IMAGE_NAME}:latest"
echo "  ${FULL_IMAGE_NAME}:${IMAGE_TAG}"
echo "=========================================="