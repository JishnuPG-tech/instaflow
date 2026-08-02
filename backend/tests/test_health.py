from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert "ytDlpVersion" in data


def test_valid_instagram_url_resolve():
    response = client.post(
        "/api/resolve", json={"url": "https://www.instagram.com/reel/C123456789/"}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == "sample_shortcode"
    assert data["type"] == "reel"


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
