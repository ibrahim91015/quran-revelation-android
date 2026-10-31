"""Check bundled cues are complete, audible-level, unclipped offline PCM assets."""
import json
import math
import pathlib
import struct
import wave

root = pathlib.Path(__file__).resolve().parents[1]
assets = sorted((root / 'app/src/main/res/raw').glob('sajdah_*.wav'))
assert len(assets) == 6
results = []
for path in assets:
    with wave.open(str(path)) as audio:
        assert audio.getnchannels() == 1 and audio.getsampwidth() == 2
        duration = audio.getnframes() / audio.getframerate()
        samples = struct.unpack(f'<{audio.getnframes()}h', audio.readframes(audio.getnframes()))
    peak = max(abs(value) for value in samples) / 32768
    rms = math.sqrt(sum(value * value for value in samples) / len(samples)) / 32768
    assert 2.5 <= duration <= 3.5, (path, duration)
    assert .90 <= peak < .98 and rms > .12, (path, peak, rms)
    assert samples[0] == 0 and abs(samples[-1]) < 8, (path, 'hard boundary')
    results.append(dict(sound=path.stem, seconds=round(duration,2), peak=round(peak,3), rms=round(rms,3)))
output = root / 'build/revelation-verification/sajdah-chimes.json'
output.parent.mkdir(parents=True,exist_ok=True)
output.write_text(json.dumps(results,indent=2)+'\n',encoding='utf-8')
print(json.dumps(results))
