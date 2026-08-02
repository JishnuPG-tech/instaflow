from unittest.mock import patch
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert "ytDlpVersion" in data


@patch("main.extract_media_info")
def test_valid_reel_extraction(mock_extract):
    mock_extract.return_value = {
        "id": "C123456789",
        "type": "reel",
        "author": "test_creator",
        "authorDisplayName": "Test Creator",
        "authorAvatarUrl": None,
        "thumbnailUrl": "https://instagram.com/thumb.jpg",
        "caption": "Cool reel test caption",
        "uploadedAt": "1700000000",
        "durationSeconds": 15.5,
        "formats": [
            {
                "formatId": "1080p",
                "label": "1080p HD",
                "ext": "mp4",
                "vcodec": "avc1.640028",
                "acodec": "mp4a.40.2",
                "height": 1080,
                "width": 1080,
                "tbr": 2500.0,
                "filesizeBytes": 15000000,
            }
        ],
        "items": None,
    }

    response = client.post(
        "/api/resolve", json={"url": "https://www.instagram.com/reel/C123456789/"}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == "C123456789"
    assert data["type"] == "reel"
    assert data["formats"][0]["formatId"] == "1080p"


@patch("main.extract_media_info")
def test_valid_carousel_extraction(mock_extract):
    mock_extract.return_value = {
        "id": "Carousel123",
        "type": "carousel",
        "author": "photo_creator",
        "authorDisplayName": "Photo Creator",
        "authorAvatarUrl": None,
        "thumbnailUrl": "https://instagram.com/thumb_c.jpg",
        "caption": "Multi slide carousel",
        "uploadedAt": "1700000500",
        "durationSeconds": None,
        "formats": None,
        "items": [
            {
                "index": 0,
                "type": "photo",
                "thumbnailUrl": "https://instagram.com/slide0.jpg",
                "formats": [
                    {
                        "formatId": "original",
                        "label": "Original Quality",
                        "ext": "jpg",
                        "height": 1080,
                        "width": 1080,
                        "filesizeBytes": 2000000,
                    }
                ],
            },
            {
                "index": 1,
                "type": "video",
                "thumbnailUrl": "https://instagram.com/slide1_thumb.jpg",
                "formats": [
                    {
                        "formatId": "1080p",
                        "label": "1080p HD",
                        "ext": "mp4",
                        "height": 1080,
                        "width": 1080,
                        "filesizeBytes": 10000000,
                    }
                ],
            },
        ],
    }

    response = client.post(
        "/api/resolve", json={"url": "https://www.instagram.com/p/Carousel123/"}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == "Carousel123"
    assert data["type"] == "carousel"
    assert len(data["items"]) == 2
    assert data["items"][0]["type"] == "photo"
    assert data["items"][1]["type"] == "video"


def test_ssrf_and_invalid_urls():
    # Localhost SSRF probe
    response = client.post("/api/resolve", json={"url": "http://127.0.0.1:8000/secret"})
    assert response.status_code == 400

    # Non-Instagram domain
    response = client.post("/api/resolve", json={"url": "https://example.com/test"})
    assert response.status_code == 400

    # Internal IP range (AWS metadata)
    response = client.post(
        "/api/resolve", json={"url": "http://169.254.169.254/latest/meta-data/"}
    )
    assert response.status_code == 400

    # Embedded credentials
    response = client.post(
        "/api/resolve", json={"url": "https://admin:pass@instagram.com/p/123/"}
    )
    assert response.status_code == 400
