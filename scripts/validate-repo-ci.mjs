import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';

const forbiddenTrackedPrefixes = ['docs/', 'review/', '검토-필요/'];
const forbiddenTrackedPatterns = [
  {
    pattern: /^apps\/api\/src\/main\/resources\/application(?:-[^/]+)?\.properties$/,
    reason: 'T-Log API uses application.yml and application-<profile>.yml only',
  },
  {
    pattern: /^apps\/api\/src\/test\/resources\/application(?:-[^/]+)?\.properties$/,
    reason: 'T-Log API uses application.yml and application-<profile>.yml only',
  },
  {
    pattern: /^apps\/api\/pom\.xml$/,
    reason: 'T-Log API uses Gradle. Do not add Maven build files.',
  },
  {
    pattern: /^apps\/api\/mvnw(?:\.cmd)?$/,
    reason: 'T-Log API uses the root Gradle wrapper.',
  },
  {
    pattern: /^apps\/api\/\.mvn\//,
    reason: 'T-Log API uses the root Gradle wrapper.',
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
  execFileSync('git', ['diff', '--check'], { stdio: 'inherit' });
} catch {
  fail('working tree diff has whitespace or conflict-marker errors');
}

const trackedFiles = git(['ls-files'])
  .split(/\r?\n/)
  .map((file) => file.trim())
  .filter(Boolean);

const javaTestFilePattern = /^apps\/api\/src\/test\/java\/.+Tests?\.java$/;
const validJUnitTagPattern = /@Tag\(\s*(?:(?:TestTags\.)?(?:UNIT|INTEGRATION)|['"](?:unit|integration)['"])\s*\)/;

const forbiddenFiles = trackedFiles.filter((file) =>
  forbiddenTrackedPrefixes.some((prefix) => file.startsWith(prefix)),
);

if (forbiddenFiles.length > 0) {
  fail(`forbidden tracked paths:\n${forbiddenFiles.map((file) => `- ${file}`).join('\n')}`);
}

const forbiddenPatternFiles = trackedFiles.flatMap((file) =>
  forbiddenTrackedPatterns
    .filter(({ pattern }) => pattern.test(file))
    .map(({ reason }) => ({ file, reason })),
);

if (forbiddenPatternFiles.length > 0) {
  fail(
    `forbidden tracked files:\n${forbiddenPatternFiles
      .map(({ file, reason }) => `- ${file}: ${reason}`)
      .join('\n')}`,
  );
}

const untaggedTestFiles = trackedFiles
  .filter((file) => javaTestFilePattern.test(file))
  .filter((file) => !validJUnitTagPattern.test(readFileSync(file, 'utf8')));

if (untaggedTestFiles.length > 0) {
  fail(
    `test classes must declare a valid JUnit @Tag(unit|integration):\n${untaggedTestFiles
      .map((file) => `- ${file}`)
      .join('\n')}`,
  );
}

console.log('CI repository validation passed');
