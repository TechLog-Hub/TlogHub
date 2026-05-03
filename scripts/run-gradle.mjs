import { spawnSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import { join } from 'node:path';

const isWindows = process.platform === 'win32';
const wrapper = isWindows ? 'gradlew.bat' : './gradlew';
const wrapperPath = isWindows ? join(process.cwd(), wrapper) : wrapper;

if (!existsSync(isWindows ? wrapperPath : join(process.cwd(), 'gradlew'))) {
  console.error('Gradle wrapper not found in repository root.');
  process.exit(1);
}

const result = spawnSync(wrapperPath, process.argv.slice(2), {
  cwd: process.cwd(),
  shell: isWindows,
  stdio: 'inherit',
});

if (result.error) {
  console.error(result.error.message);
  process.exit(1);
}

process.exit(result.status ?? 1);
