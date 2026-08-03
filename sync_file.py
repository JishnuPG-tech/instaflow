import subprocess, base64

files = [
    (
        "docs/evidence/GATE_G_RC1_LOG.md",
        "/workspaces/Seal/docs/evidence/GATE_G_RC1_LOG.md",
    ),
    (
        "docs/evidence/GATE_G_CERTIFICATION.md",
        "/workspaces/Seal/docs/evidence/GATE_G_CERTIFICATION.md",
    ),
    (
        "docs/evidence/FINAL_CERTIFICATION.md",
        "/workspaces/Seal/docs/evidence/FINAL_CERTIFICATION.md",
    ),
    (
        "docs/PROJECT_STATUS.md",
        "/workspaces/Seal/docs/PROJECT_STATUS.md",
    ),
]

CODESPACE = "literate-space-zebra-x74gwwwqv54cppp9"

for local_path, remote_path in files:
    with open(local_path, "r", encoding="utf-8") as f:
        content = f.read()
    b64 = base64.b64encode(content.encode("utf-8")).decode("utf-8")
    cmd = [
        "gh", "codespace", "ssh", "-c", CODESPACE,
        "--", f"mkdir -p /workspaces/Seal/docs/evidence && echo '{b64}' | base64 -d > {remote_path}"
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"FAILED: {local_path}\n{result.stderr}")
    else:
        print(f"OK: {local_path}")

print("Final certification files synced.")
