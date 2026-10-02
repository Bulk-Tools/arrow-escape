#!/usr/bin/env python3
"""Synthesize the Arrow Escape sound effects as mono 16-bit 22050 Hz WAVs.

Stdlib only (wave, struct, math, os). Output goes to app/src/main/res/raw/.
"""

import math
import os
import wave

SAMPLE_RATE = 22050
PEAK = 0.95  # normalization target (fraction of int16 full scale)

RAW_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "app", "src", "main", "res", "raw",
)


def write_wav(name, samples):
    """Normalize to int16 range and write a mono WAV via the wave module."""
    peak = max(abs(s) for s in samples) or 1.0
    scale = (PEAK * 32767.0) / peak
    frames = bytearray()
    for s in samples:
        v = int(max(-1.0, min(1.0, s * scale / 32767.0)) * 32767.0)
        # pack as little-endian int16
        frames += (v & 0xFFFF).to_bytes(2, "little", signed=False) \
            if v >= 0 else ((v + 0x10000) & 0xFFFF).to_bytes(2, "little", signed=False)
    path = os.path.join(RAW_DIR, name)
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SAMPLE_RATE)
        w.writeframes(bytes(frames))
    print(f"wrote {path} ({len(samples)} frames)")


def sweep_sine(duration, f0, f1, decay):
    """Sine burst sweeping f0 -> f1 Hz with exponential amplitude decay."""
    n = int(duration * SAMPLE_RATE)
    out = []
    phase = 0.0
    for i in range(n):
        t = i / SAMPLE_RATE
        frac = i / n
        freq = f0 + (f1 - f0) * frac
        phase += 2.0 * math.pi * freq / SAMPLE_RATE
        out.append(math.sin(phase) * math.exp(-decay * t))
    return out


def blip_squareish(duration, freq, decay):
    """Soft-clipped square-ish blip with fast decay."""
    n = int(duration * SAMPLE_RATE)
    out = []
    for i in range(n):
        t = i / SAMPLE_RATE
        s = math.sin(2.0 * math.pi * freq * t)
        out.append(math.tanh(3.0 * s) * math.exp(-decay * t))
    return out


def two_tone(duration, f_a, f_b, duty=0.5):
    """Square-wave buzz: first `duty` of duration at f_a, rest at f_b."""
    n = int(duration * SAMPLE_RATE)
    split = int(n * duty)
    out = []
    for i in range(n):
        freq = f_a if i < split else f_b
        t = i / SAMPLE_RATE
        s = math.sin(2.0 * math.pi * freq * t)
        sq = 1.0 if s >= 0 else -1.0
        # slight overall fade to avoid end clicks
        edge = min(1.0, i / (0.005 * SAMPLE_RATE), (n - i) / (0.005 * SAMPLE_RATE))
        out.append(sq * 0.6 * edge)
    return out


def arpeggio(notes, note_duration, decay):
    """Concatenated decaying sine notes with tiny edge fades to avoid clicks."""
    out = []
    for freq in notes:
        n = int(note_duration * SAMPLE_RATE)
        fade = int(0.005 * SAMPLE_RATE)
        for i in range(n):
            t = i / SAMPLE_RATE
            env = math.exp(-decay * t)
            edge = min(1.0, i / fade, (n - i) / fade) if fade > 0 else 1.0
            out.append(math.sin(2.0 * math.pi * freq * t) * env * edge)
    return out


def chime(duration, fundamental, harmonic, decay, harmonic_gain=0.4):
    """Bright chime: fundamental + one harmonic, exponential decay."""
    n = int(duration * SAMPLE_RATE)
    return [
        (math.sin(2.0 * math.pi * fundamental * i / SAMPLE_RATE)
         + harmonic_gain * math.sin(2.0 * math.pi * harmonic * i / SAMPLE_RATE))
        * math.exp(-decay * i / SAMPLE_RATE)
        for i in range(n)
    ]


def main():
    os.makedirs(RAW_DIR, exist_ok=True)

    # pop: 0.12s sine burst, 600 -> 200 Hz pitch drop, exponential decay
    write_wav("pop.wav", sweep_sine(0.12, 600.0, 200.0, decay=28.0))

    # click: 0.06s short 1200 Hz square-ish blip, fast decay
    write_wav("click.wav", blip_squareish(0.06, 1200.0, decay=90.0))

    # error: 0.3s two-tone low buzz (180 Hz then 140 Hz square waves)
    write_wav("error.wav", two_tone(0.3, 180.0, 140.0))

    # unfreeze: 0.35s rising glissando 300 -> 1200 Hz sine with decay
    write_wav("unfreeze.wav", sweep_sine(0.35, 300.0, 1200.0, decay=5.0))

    # win: 0.8s arpeggio of 4 sine notes (523, 659, 784, 1047 Hz), 0.2s each
    write_wav("win.wav", arpeggio([523.0, 659.0, 784.0, 1047.0], 0.2, decay=8.0))

    # star: 0.5s bright chime, 1568 Hz sine + 2093 Hz harmonic
    write_wav("star.wav", chime(0.5, 1568.0, 2093.0, decay=7.0))


if __name__ == "__main__":
    main()
