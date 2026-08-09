from pydantic import BaseModel

class MediaCapabilities(BaseModel):
    canDownloadImage: bool = False
    canDownloadVideo: bool = False
    canDownloadAudio: bool = False
    canExtractAudio: bool = False
    canMuxAudioWithImage: bool = False
    canMuxAudioWithVideo: bool = False
    canDownloadCarousel: bool = False
    canSelectItems: bool = False
    hasMultipleItems: bool = False
