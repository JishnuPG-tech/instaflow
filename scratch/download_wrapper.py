import urllib.request
import os

url = "https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar"
os.makedirs("gradle/wrapper", exist_ok=True)
dest = "gradle/wrapper/gradle-wrapper.jar"

try:
    print(f"Downloading gradle-wrapper.jar from {url}...")
    urllib.request.urlretrieve(url, dest)
    print(f"Successfully saved gradle-wrapper.jar ({os.path.getsize(dest)} bytes)")
except Exception as e:
    print(f"Fallback download error: {e}")
    # Try raw git wrapper url
    fallback_url = "https://repo.gradle.org/gradle/libs-releases-local/org/gradle/gradle-wrapper/8.11.1/gradle-wrapper-8.11.1.jar"
    run_fallback = urllib.request.urlretrieve(fallback_url, dest)
    print(f"Saved via fallback ({os.path.getsize(dest)} bytes)")
