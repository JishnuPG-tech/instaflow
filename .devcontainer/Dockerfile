FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Set non-interactive mode during build
ENV DEBIAN_FRONTEND=noninteractive

# Install JDK 17, Python 3.11, build tools, curl, unzip, git
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    python3.11 \
    python3.11-venv \
    python3-pip \
    curl \
    unzip \
    zip \
    git \
    build-essential \
    openssh-server \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Android SDK Command Line Tools
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -sS https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip

# Accept Android licenses and install platform-tools, build-tools, and android-34
RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true \
    && sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" > /dev/null 2>&1 || true

# Set up Python alias
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.11 1 \
    && update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 1
