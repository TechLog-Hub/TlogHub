import { execFileSync } from 'node:child_process';

const forbiddenStagedPrefixes = ['docs/', 'review/', '검토-필요/'];

function git(args) {
  return execFileSync('git', args, { encoding: 'utf8' });
}

function fail(message) {
  console.error(message);
  process.exit(1);
}

try {
  execFileSync('git', ['diff', '--cached', '--check'], { stdio: 'inherit' });
} catch {
  fail('staged diff has whitespace or conflict-marker errors');
}

const stagedFiles = git(['diff', '--cached', '--name-only'])
  .split(/\r?\n/)
  .map((file) => file.trim())
  .filter(Boolean);

const forbiddenFiles = stagedFiles.filter((file) =>
  forbiddenStagedPrefixes.some((prefix) => file.startsWith(prefix)),
);

if (forbiddenFiles.length > 0) {
  fail(`forbidden staged paths:\n${forbiddenFiles.map((file) => `- ${file}`).join('\n')}`);
}

console.log('repository validation passed');
