"""Generate the fork's original, offline sajdah chimes (24 kHz mono PCM).

No samples or third-party recordings are used. Regenerating produces identical
assets. Gain is applied by the app, independently of Android's media volume.
"""
import math
import pathlib
import struct
import wave

RATE = 24000
OUT = pathlib.Path(__file__).resolve().parents[1] / 'app/src/main/res/raw'
CHIMES = {
    'soft_bells': ([72, 76, 79], .34, 2.8, [.8, .16, .04]),
    'warm_marimba': ([65, 69, 72, 77], .30, 2.9, [.9, .08, .02]),
    'gentle_harp': ([72, 74, 79, 81, 84], .20, 2.8, [.82, .13, .05]),
    'crystal_chime': ([79, 83, 86], .43, 3.1, [.9, .06, .04]),
    'evening_glow': ([69, 72, 76, 81], .35, 3.0, [.94, .05, .01]),
    'peaceful_rise': ([74, 76, 81, 86], .30, 2.9, [.88, .09, .03]),
}

def render(notes, spacing, duration, partials):
    result = []
    for i in range(round(RATE * duration)):
        t = i / RATE
        value = 0.
        for index, note in enumerate(notes):
            age = t - index * spacing
            if age < 0:
                continue
            frequency = 440 * 2 ** ((note - 69) / 12)
            envelope = min(age / .018, 1) * math.exp(-age / .65)
            # A gentle release reaches zero before the end of the file.
            envelope *= min(max((duration - t) / .30, 0), 1)
            value += envelope * sum(gain * math.sin(2 * math.pi * frequency * (h + 1) * age)
                                    for h, gain in enumerate(partials))
        result.append(value)
    scale = .94 / max(abs(v) for v in result)
    return b''.join(struct.pack('<h', round(v * scale * 32767)) for v in result)

if __name__ == '__main__':
    OUT.mkdir(exist_ok=True)
    for name, spec in CHIMES.items():
        with wave.open(str(OUT / f'sajdah_{name}.wav'), 'wb') as file:
            file.setparams((1, 2, RATE, 0, 'NONE', 'not compressed'))
            file.writeframes(render(*spec))
        print(f'{name}: {spec[2]:.1f}s')
