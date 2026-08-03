import urllib.request
import json

url = "https://api.github.com/repos/JishnuPG-tech/instaflow/actions/runs/30755555704/jobs"
req = urllib.request.Request(url, headers={"User-Agent": "Python/3.13", "Accept": "application/vnd.github+json"})

try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode("utf-8"))
        for j in data.get("jobs", []):
            print(f"=== JOB: {j['name']} (Conclusion: {j['conclusion']}) ===")
            for s in j.get("steps", []):
                print(f"  Step '{s['name']}': status={s['status']}, conclusion={s['conclusion']}")
except Exception as e:
    print(f"Error: {e}")
