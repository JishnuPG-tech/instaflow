import sys, types
from pathlib import Path

_file_path = Path(__file__).resolve()
_app_dir = _file_path.parent
_root_dir = _app_dir.parent

for p in [str(_root_dir), str(_app_dir)]:
    if p not in sys.path:
        sys.path.insert(0, p)

try:
    import app
    if 'backend' not in sys.modules:
        _bmod = types.ModuleType('backend')
        _bmod.app = app
        sys.modules['backend'] = _bmod
    sys.modules['backend.app'] = app
except Exception:
    pass
