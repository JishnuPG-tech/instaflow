# CONTINUOUS REGRESSION TEST DATASET — InstaFlow

- **Project**: InstaFlow (Specialization of JunkFood02/Seal)
- **Document Version**: `1.0.0`
- **Purpose**: Permanent, reproducible test dataset for validating Instagram media extraction across releases.

---

## 🧪 Standard Test Target Dataset

| Target ID | Media Category | URL / Pattern Format | Expected Extraction Behavior | Gate F Acceptance Criteria |
|:---|:---|:---|:---|:---|
| **TD-IMG-01** | Single Image Post | `https://www.instagram.com/p/{shortcode}/` | High-res JPEG/PNG payload | Single `.jpg` file in Gallery |
| **TD-VID-01** | Single Video Post | `https://www.instagram.com/p/{shortcode}/` | MP4 video + AAC audio combined | Single `.mp4` file with audio sync |
| **TD-REL-01** | Reel | `https://www.instagram.com/reel/{shortcode}/` | Vertical 9:16 MP4 video payload | `.mp4` saved with reel metadata |
| **TD-STO-01** | Active Story | `https://www.instagram.com/stories/{user}/{id}/` | Image/Video story payload | Single media file downloaded |
| **TD-STO-02** | Expired Story | `https://www.instagram.com/stories/{user}/{expired_id}/` | 404 / Expired media error | Graceful UI error alert, no crash |
| **TD-HGT-01** | Story Highlight | `https://www.instagram.com/stories/highlights/{id}/` | Multi-item story highlight sequence | Sequential highlight items enqueued |
| **TD-PFP-01** | Profile Picture | `https://www.instagram.com/{username}/` | HD profile avatar URL payload | High-res `.jpg` avatar saved |
| **TD-CAR-01** | Image Carousel | `https://www.instagram.com/p/{carousel_shortcode}/` | N image entries array | N `.jpg` files enqueued via `InstagramCarouselRouter` |
| **TD-CAR-02** | Video Carousel | `https://www.instagram.com/p/{carousel_shortcode}/` | N video entries array | N `.mp4` files enqueued |
| **TD-CAR-03** | Mixed Carousel | `https://www.instagram.com/p/{carousel_shortcode}/` | Interleaved Image + Video entries | N `.jpg`/`.mp4` files enqueued correctly |
| **TD-TXT-01** | Unicode / Emoji Caption | `https://www.instagram.com/p/{shortcode}/` | Captions containing non-ASCII / Emojis | Filename & history title render correctly |
| **TD-COK-01** | Cookie Required (Private) | `https://www.instagram.com/p/{private_shortcode}/` | Requires Instagram login session | Prompts for cookie import / uses stored cookies |
| **TD-ERR-01** | Broken / Invalid URL | `https://www.instagram.com/invalid_path_xyz` | Invalid URL error | Display "Invalid URL" toast/alert |
