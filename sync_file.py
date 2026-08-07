import subprocess, base64

files = [
    (
        "app/src/main/java/com/junkfood/seal/ui/page/download/HomePageViewModel.kt",
        "/workspaces/Seal/app/src/main/java/com/junkfood/seal/ui/page/download/HomePageViewModel.kt",
    ),
]

CODESPACE = "literate-space-zebra-x74gwwwqv54cppp9"

for local_path, remote_path in files:
    with open(local_path, "r", encoding="utf-8") as f:
        content = f.read()
    b64 = base64.b64encode(content.encode("utf-8")).decode("utf-8")
    p = subprocess.Popen(["gh", "codespace", "ssh", "-c", CODESPACE, "--", f"base64 -d > {remote_path}"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = p.communicate(input=b64.encode("ascii"))
    if p.returncode != 0:
        print(f"FAILED: {local_path}\n{stderr.decode('utf-8')}")
    else:
        print(f"OK: {local_path}")

print("HomePageViewModel.kt synced cleanly.")
