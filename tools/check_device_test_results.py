"""Fail a device gate if Android instrumentation crashed or discovered no tests."""
import pathlib
import xml.etree.ElementTree as ET

root = pathlib.Path(__file__).resolve().parents[1]
reports = list((root / 'app/build/outputs/androidTest-results/connected/debug').rglob('TEST-*.xml'))
assert reports, 'No debug instrumentation results were produced'
suites = [ET.parse(report).getroot() for report in reports]
tests = sum(int(suite.get('tests', 0)) for suite in suites)
failures = sum(int(suite.get('failures', 0)) + int(suite.get('errors', 0)) for suite in suites)
skipped = sum(int(suite.get('skipped', 0)) for suite in suites)
assert tests >= 4 and failures == 0 and skipped == 0, (tests, failures, skipped)
print(f'Device gate: {tests} tests, {failures} failures, {skipped} skipped')
