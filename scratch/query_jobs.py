import urllib.request
import json

url = "https://api.github.com/repos/JishnuPG-tech/instaflow/actions/runs/30783781852/jobs"
req = urllib.request.Request(url, headers={"User-Agent": "Python/3.13", "Accept": "application/vnd.github+json"})

try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode("utf-8"))
        jobs = data.get("jobs", [])
        print(f"Total Jobs in Run 30783781852: {len(jobs)}")
        for j in jobs:
            print(f"Job '{j['name']}': status={j['status']}, conclusion={j['conclusion']}")
except Exception as e:
    print(f"Error fetching jobs: {e}")
