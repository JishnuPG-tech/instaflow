import urllib.request
import json

url = "https://api.github.com/repos/JishnuPG-tech/instaflow/actions/runs?per_page=5"
req = urllib.request.Request(url, headers={"User-Agent": "Python/3.13", "Accept": "application/vnd.github+json"})

try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode("utf-8"))
        runs = data.get("workflow_runs", [])
        for r in runs[:3]:
            print(f"Run #{r['run_number']} (ID: {r['id']}): status={r['status']}, conclusion={r['conclusion']}, commit={r['head_sha'][:7]}")
except Exception as e:
    print(f"Error fetching runs: {e}")
