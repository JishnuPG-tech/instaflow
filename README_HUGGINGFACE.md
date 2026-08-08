---
title: InstaFlow Remote Processing Engine
emoji: ⚡
colorFrom: red
colorTo: purple
sdk: docker
app_port: 7860
pinned: false
---

# ⚡ InstaFlow v2 Remote Processing Engine (HuggingFace Docker Space)

High-performance, stateless FastAPI backend for Instagram Reels, Photo Posts, Carousels, and Media streaming.

## 🛠️ HuggingFace Spaces Deployment Instructions

1. Go to [HuggingFace Spaces](https://huggingface.co/new-space).
2. Set Space Name: `instaflow-backend`
3. Select SDK: **Docker** -> **Blank**
4. Copy the contents of this repository (or push via git) into your HuggingFace Space.
5. HuggingFace will automatically build the `Dockerfile` and start the server on port `7860`.

## 🌐 Public Space API Endpoints

- `GET https://your-space-name.hf.space/health`
- `POST https://your-space-name.hf.space/api/v1/analyze`
- `GET https://your-space-name.hf.space/api/v1/download?url=...`
