#!/usr/bin/env python3
"""Report JUnit results without conflating them with device/service/coverage evidence."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parent.parent
files = sorted((root / 'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'))
if not files:
    sys.exit('No JUnit XML found; no passing-test claim is possible.')
results = [ET.parse(path).getroot() for path in files]
counts = {key: sum(int(result.get(key, '0')) for result in results)
          for key in ('tests', 'failures', 'errors', 'skipped')}
print(f'JVM XML: {len(results)} suites, ' + ', '.join(f'{key}={value}' for key, value in counts.items()))
print('XML alone does not prove a fresh run: inspect Gradle task status or use --fresh.')
if counts['failures'] or counts['errors']:
    sys.exit(1)
