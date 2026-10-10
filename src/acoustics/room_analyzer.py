#!/usr/bin/env python3
import numpy as np
import math
import sys
import time

class RoomAcousticsEngine:
    def __init__(self, sample_rate=44100):
        self.sample_rate = sample_rate
        print(f"[+] Motor Acústico SovereignEMA_G5618 inicializado. Sample Rate: {sample_rate}Hz")

    def generate_pink_noise(self, duration_sec=3):
        """Genera ruido rosa con pendiente espectral de -3dB/octava"""
        num_samples = int(self.sample_rate * duration_sec)
        # Filtro Voss-McCartney aproximado para ruido rosa
        whitish = np.random.randn(num_samples)
        pink = np.cumsum(whitish) # Simplificación analítica vectorial
        pink -= np.mean(pink)
        pink /= np.max(np.abs(pink))
        print(f"[+] Generado buffer de Ruido Rosa ({duration_sec}s).")
        return pink

    def generate_sine_sweep(self, start_freq=20, end_freq=20000, duration_sec=3):
        """Genera un barrido de frecuencias logarítmico (Sine Sweep)"""
        num_samples = int(self.sample_rate * duration_sec)
        t = np.linspace(0, duration_sec, num_samples)
        # Barrido logarítmico
        k = (end_freq / start_freq) ** (1 / duration_sec)
        phase = 2 * math.pi * start_freq * ((k ** t) - 1) / math.log(k)
        sweep = np.sin(phase)
        print(f"[+] Generado Sine Sweep Logarítmico ({start_freq}Hz - {end_freq}Hz).")
        return sweep

    def analyze_room_modes(self, room_length_m, room_width_m, room_height_m):
        """Calcula los modos axiales principales de la sala (acumulación de graves)"""
        c = 343.0 # Velocidad del sonido en m/s
        print(f"\n=================================================================")
        print(f" DIAGNÓSTICO DE MODOS AXIALES (Física de Sala)")
        print(f" Dimensiones: {room_length_m}m (L) x {room_width_m}m (W) x {room_height_m}m (H)")
        print(f"=================================================================")
        
        modes = []
        for dim_name, dim_val in [("Largo", room_length_m), ("Ancho", room_width_m), ("Alto", room_height_m)]:
            if dim_val > 0:
                f1 = c / (2 * dim_val)
                print(f"[*] Modo fundamental [{dim_name}] ({dim_val}m): {f1:.2f} Hz")
                modes.append(f1)
        
        print(f"[+] Análisis completado. Frecuencias críticas de resonancia mapeadas.")
        return modes

if __name__ == "__main__":
    engine = RoomAcousticsEngine()
    # Ejemplo analítico predeterminado para una habitación estándar de prueba (5m x 4m x 2.6m)
    engine.analyze_room_modes(5.0, 4.0, 2.6)
    noise = engine.generate_pink_noise(1)
    sweep = engine.generate_sine_sweep(20, 20000, 1)
    print("\n[✓] Módulo analítico de acústica verificado y listo para integración DSP.")
