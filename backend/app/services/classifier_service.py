import logging
from typing import Dict, Any, List
from backend.app.models.media import MediaType, MediaItemClassification, MediaClassificationResult

logger = logging.getLogger("ClassifierService")

class ClassifierService:
    @classmethod
    def classify(cls, metadata: Dict[str, Any]) -> MediaClassificationResult:
        shortcode = metadata.get("id", "media")
        title = metadata.get("title", "Instagram Content")
        author = metadata.get("uploader") or metadata.get("channel", "Instagram User")
        thumb = metadata.get("thumbnail") or metadata.get("url")

        entries = metadata.get("entries") or metadata.get("items") or []
        
        # Scenario 1: Multi-Item Carousel
        if len(entries) > 1:
            classified_items: List[MediaItemClassification] = []
            for idx, item in enumerate(entries, start=1):
                is_vid = bool(item.get("vcodec") and item.get("vcodec") != "none") or float(item.get("duration") or 0.0) > 0.0 or item.get("ext") == "mp4"
                item_type = MediaType.VIDEO if is_vid else MediaType.IMAGE
                classified_items.append(MediaItemClassification(
                    index=idx,
                    media_type=item_type,
                    url=item.get("url") or item.get("thumbnail"),
                    thumbnail=item.get("thumbnail") or item.get("url"),
                    duration=float(item.get("duration") or 0.0),
                    vcodec=item.get("vcodec"),
                    acodec=item.get("acodec"),
                    extra_info=item
                ))
            return MediaClassificationResult(
                shortcode=shortcode,
                primary_type=MediaType.CAROUSEL,
                title=title,
                author=author,
                thumbnail=thumb,
                items=classified_items,
                raw_metadata=metadata
            )

        # Scenario 2: Single Video / Reel
        is_video = metadata.get("is_video", False) or bool(metadata.get("vcodec") and metadata.get("vcodec") != "none") or float(metadata.get("duration") or 0.0) > 0.0 or metadata.get("ext") == "mp4"
        if is_video:
            item = MediaItemClassification(
                index=1,
                media_type=MediaType.VIDEO,
                url=metadata.get("url"),
                thumbnail=thumb,
                duration=float(metadata.get("duration") or 0.0),
                vcodec=metadata.get("vcodec", "h264"),
                acodec=metadata.get("acodec", "aac"),
                extra_info=metadata
            )
            return MediaClassificationResult(
                shortcode=shortcode,
                primary_type=MediaType.VIDEO,
                title=title,
                author=author,
                thumbnail=thumb,
                items=[item],
                raw_metadata=metadata
            )

        # Scenario 3: Single Static Image Post
        item = MediaItemClassification(
            index=1,
            media_type=MediaType.IMAGE,
            url=metadata.get("url") or thumb,
            thumbnail=thumb,
            duration=0.0,
            vcodec="none",
            acodec="none",
            extra_info=metadata
        )
        return MediaClassificationResult(
            shortcode=shortcode,
            primary_type=MediaType.IMAGE,
            title=title,
            author=author,
            thumbnail=thumb,
            items=[item],
            raw_metadata=metadata
        )
