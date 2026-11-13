"""Confere se o ambiente tem o que o gerador da Marca v2 precisa (Pillow e numpy)."""
import importlib

for mod in ("PIL", "numpy"):
    try:
        m = importlib.import_module(mod)
        print(mod, "ok", getattr(m, "__version__", "?"))
    except ImportError as e:
        print(mod, "FALTA:", e)
