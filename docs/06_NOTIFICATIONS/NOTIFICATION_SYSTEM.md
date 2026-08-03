# Notification System — Progress, Channels & Actions

## 1. Notification Utility (`NotificationUtil.kt`)

[`NotificationUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/NotificationUtil.kt) encapsulates all Android notification channel creation, progress bar updates, and completion/error alerts.

---

## 2. Notification Channels

| Channel ID | Channel Name | Importance | Description |
| :--- | :--- | :--- | :--- |
| `download_channel` | Download Notifications | `IMPORTANCE_LOW` | Displays active progress bar, speed (MB/s), ETA, and Cancel action button without sound/vibration interrupt. |
| `update_channel` | App Updates | `IMPORTANCE_DEFAULT` | Displays new version release available alerts. |

---

## 3. Broadcast Receiver (`NotificationActionReceiver.kt`)

[`NotificationActionReceiver.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/NotificationActionReceiver.kt) processes notification pending intent clicks:
- `ACTION_CANCEL`: Immediately terminates active `yt-dlp` process execution.
- `ACTION_PAUSE`: Pauses active download task.
- `ACTION_RETRY`: Restarts failed task.
