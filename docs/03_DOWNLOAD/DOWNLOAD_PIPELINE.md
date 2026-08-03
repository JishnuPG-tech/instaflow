# Download Pipeline & Progress Callback Lifecycle

## 1. Pipeline Execution Flow

```mermaid
sequenceDiagram
    participant UI as DownloadDialogViewModel
    participant Engine as DownloaderV2Impl
    participant JNI as YoutubeDL JNI
    participant Process as yt-dlp Process
    participant DB as Room DB

    UI->>Engine: executeDownloadTask(task)
    Engine->>Engine: Prepare Request & Output Directory
    Engine->>JNI: YoutubeDL.getInstance().execute(request, callback)
    JNI->>Process: Spawn Process (stdout pipe)
    loop Progress Callback
        Process-->>JNI: Output line "[download]  45.2% of 10.5MiB at 2.4MiB/s ETA 00:03"
        JNI-->>Engine: callback(progress, eta, line)
        Engine-->>UI: TaskState.Running(progress=45.2f, speed="2.4MiB/s")
    end
    Process-->>JNI: Process exit code 0
    JNI-->>Engine: Execution Success
    Engine->>DB: Insert DownloadedVideoInfo record
    Engine-->>UI: TaskState.Completed
```

---

## 2. Progress Parsing Regex & Callbacks

`youtubedl-android` parses stdout lines matching:
`\[download\]\s+(\d+\.\d+)%\s+of\s+([~\d\.\w]+)\s+at\s+([\d\.\w/]+)\s+ETA\s+([\d:]+)`

- **Percentage**: Converted to floating point (`0.0f` to `100.0f`).
- **Download Speed**: E.g., `2.4MiB/s`.
- **ETA**: Estimated time remaining in `MM:SS`.
- **Total Bytes**: Extracted file size estimate.
