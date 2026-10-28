"""Check Tanzil ordering and real audio endpoints without downloading whole recitations."""
import concurrent.futures
import datetime
import html.parser
import json
import pathlib
import re
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[1]
REPORT = ROOT / 'build/revelation-verification/live-sources.json'


class TableParser(html.parser.HTMLParser):
    def __init__(self):
        super().__init__()
        self.rows, self.row, self.cell = [], [], None

    def handle_starttag(self, tag, attrs):
        if tag == 'tr':
            self.row = []
        elif tag in ('td', 'th'):
            self.cell = ''

    def handle_data(self, data):
        if self.cell is not None:
            self.cell += data

    def handle_endtag(self, tag):
        if tag in ('td', 'th') and self.cell is not None:
            self.row.append(self.cell.strip())
            self.cell = None
        elif tag == 'tr':
            self.rows.append(self.row)


def probe(task):
    reciter_id, kind, surah, url = task
    result = dict(id=reciter_id, kind=kind, surah=surah, url=url)
    for attempt in range(2):
        try:
            request = urllib.request.Request(url, headers={'Range': 'bytes=0-255', 'User-Agent': 'QuranRevelationReleaseVerification/1.0'})
            with urllib.request.urlopen(request, timeout=30) as response:
                sample = response.read(256)
                result.update(status=response.status, contentType=response.headers.get('Content-Type'),
                              bytesRead=len(sample), finalUrl=response.url)
                # Reject HTML/error bodies served with a misleading HTTP 200.
                result['ok'] = response.status in (200, 206) and len(sample) > 32 and not sample.lstrip().startswith((b'<', b'{'))
                if kind == 'timings':
                    result['ok'] = result['ok'] and sample.startswith(b'PK')
            return result
        except Exception as error:
            result.update(ok=False, error=str(error))
    return result


def main():
    catalog = json.loads((ROOT / 'app/src/test/resources/reference-reciters-3.6.4.json').read_text())
    parser = TableParser()
    parser.feed(urllib.request.urlopen('https://tanzil.net/docs/revelation_order', timeout=30).read().decode())
    tanzil = [int(row[2]) for row in parser.rows if len(row) >= 3 and row[0].isdigit() and row[2].isdigit()]
    source = (ROOT / 'app/src/main/java/com/quran/labs/androidquran/data/RevelationOrder.kt').read_text()
    actual = list(map(int, re.findall(r'\d+', source.split('intArrayOf(')[1].split(')')[0])))
    assert len(tanzil) == 114 and tanzil == actual, 'Source ordering differs from Tanzil'
    tasks = []
    for reciter in catalog:
        for extension, base in [('mp3', reciter['url']), ('opus', reciter['opusUrl'])]:
            if not base:
                continue
            for surah in (96, 68):
                filename = f'{surah:03d}.{extension}' if reciter['db'] else f'{surah:03d}001.{extension}'
                tasks.append((reciter['id'], extension, surah, base + filename))
        if reciter['db']:
            tasks.append((reciter['id'], 'timings', None, f"https://android.quran.com/data/databases/audio/{reciter['db']}.zip"))
    results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as executor:
        for index, result in enumerate(executor.map(probe, tasks), 1):
            results.append(result)
            if index % 30 == 0 or not result['ok']:
                print(f"{index}/{len(tasks)} checked; failures={sum(not x['ok'] for x in results)}", flush=True)
    report = dict(timestamp=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  tanzilOrderMatches=True, tanzilOrder=tanzil, reciterEntries=len(catalog),
                  probes=len(results), failures=sum(not x['ok'] for x in results), results=results)
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    REPORT.write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps({key: value for key, value in report.items() if key not in ('results', 'tanzilOrder')}))
    raise SystemExit(1 if report['failures'] else 0)


if __name__ == '__main__':
    main()
