import json

class GCPHardwareDatabase:
    def __init__(self):
        print("[*] Conexión GCP Firebase inicializada (Modo Sovereign Local).")

    def fetch_headphone_profile(self, model_name):
        print(f"[*] GCP: Consultando curva de compensación para -> {model_name}")
        mock_db = {
            "audifonos_chinos_genericos": {"20Hz": "+4dB", "300Hz": "-2dB", "8kHz": "-6dB"},
            "earpods_clon": {"20Hz": "+6dB", "300Hz": "-1dB", "8kHz": "-4dB"}
        }
        profile = mock_db.get(model_name, {"default": "flat"})
        print(f"[+] GCP: Curva descargada exitosamente: {profile}")
        return profile
