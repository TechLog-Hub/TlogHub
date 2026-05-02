import { execFileSync } from 'node:child_process';

const forbiddenStagedPrefixes = ['docs/', 'review/', '검토-필요/'];
const forbiddenStagedPatterns = [
  {
    pattern: /^apps\/api\/src\/main\/resources\/application(?:-[^/]+)?\.properties$/,
    reason: 'T-Log API uses application.yml and application-<profile>.yml only',
  },
  {
    pattern: /^apps\/api\/src\/test\/resources\/application(?:-[^/]+)?\.properties$/,
    reason: 'T-Log API uses application.yml and application-<profile>.yml only',
  },
];

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

const stagedFiles = git(['diff', '--cached', '--name-only', '--diff-filter=ACMR'])
  .split(/\r?\n/)
  .map((file) => file.trim())
  .filter(Boolean);

const forbiddenFiles = stagedFiles.filter((file) =>
  forbiddenStagedPrefixes.some((prefix) => file.startsWith(prefix)),
);

if (forbiddenFiles.length > 0) {
  fail(`forbidden staged paths:\n${forbiddenFiles.map((file) => `- ${file}`).join('\n')}`);
}

const forbiddenPatternFiles = stagedFiles.flatMap((file) =>
  forbiddenStagedPatterns
    .filter(({ pattern }) => pattern.test(file))
    .map(({ reason }) => ({ file, reason })),
);

if (forbiddenPatternFiles.length > 0) {
  fail(
    `forbidden staged files:\n${forbiddenPatternFiles
      .map(({ file, reason }) => `- ${file}: ${reason}`)
      .join('\n')}`,
  );
}

console.log('repository validation passed');
