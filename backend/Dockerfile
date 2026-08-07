FROM python:3.12-slim

# Install system dependencies (FFmpeg, Curl, Ca-certificates)
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security compliance & HuggingFace Spaces suitability
RUN useradd -m -u 1000 instaflow

WORKDIR /home/instaflow/app

# Copy requirements and install Python packages
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . /home/instaflow/app/backend
COPY cookies.txt /home/instaflow/cookies.txt

# Environment configuration
ENV PYTHONPATH=/home/instaflow/app
ENV PORT=7860
ENV TEMP_DIR=/tmp/instaflow_downloads
ENV COOKIES_FILE=/home/instaflow/cookies.txt

# Set permissions for non-root user
RUN mkdir -p /tmp/instaflow_downloads && chown -R instaflow:instaflow /tmp/instaflow_downloads /home/instaflow

USER instaflow

EXPOSE 7860
EXPOSE 8000

CMD ["python", "-m", "uvicorn", "backend.app.main:app", "--host", "0.0.0.0", "--port", "7860"]
