import socket
import urllib3.util.connection as urllib3_cn

# Force IPv4 socket resolution globally to eliminate IPv6 datacenter socket hangs on Hugging Face Space
_old_getaddrinfo = socket.getaddrinfo
def _ipv4_getaddrinfo(*args, **kwargs):
    res = _old_getaddrinfo(*args, **kwargs)
    ipv4 = [r for r in res if r[0] == socket.AF_INET]
    return ipv4 if ipv4 else res
socket.getaddrinfo = _ipv4_getaddrinfo
urllib3_cn.allowed_gai_family = lambda: socket.AF_INET

import uuid
import logging
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from backend.app.core.config import settings
from backend.app.core.logging import request_id_ctx_var, logger
from backend.app.api.health import router as health_router
from backend.app.api.gateway import router as gateway_router
from backend.app.api.video import router as video_router
from backend.app.api.audio import router as audio_router
from backend.app.api.post import router as post_router

# InstaFlow Modular Service Engine v2.1.0
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    docs_url="/docs",
    redoc_url="/redoc"
)

# Enable CORS Middleware for full client accessibility
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Middleware for correlation Request IDs
@app.middleware("http")
async def request_id_middleware(request: Request, call_next):
    req_id = request.headers.get("X-Request-ID", str(uuid.uuid4()))
    token = request_id_ctx_var.set(req_id)
    response = await call_next(request)
    response.headers["X-Request-ID"] = req_id
    request_id_ctx_var.reset(token)
    return response

# Register API Routers
app.include_router(health_router, tags=["Health"])
app.include_router(gateway_router, prefix=settings.API_V1_PREFIX, tags=["Gateway"])
app.include_router(video_router, prefix=settings.API_V1_PREFIX, tags=["Video Engine"])
app.include_router(audio_router, prefix=settings.API_V1_PREFIX, tags=["Audio Engine"])
app.include_router(post_router, prefix=settings.API_V1_PREFIX, tags=["Post Engine"])
