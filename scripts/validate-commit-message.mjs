import { readFileSync } from 'node:fs';

const messagePath = process.argv[2];
const allowedTypes = ['feat', 'fix', 'docs', 'style', 'refactor', 'test', 'chore', 'build', 'ci', 'perf', 'hotfix'];
const gitmojiByType = new Map([
  ['feat', '✨'],
  ['fix', '🐛'],
  ['docs', '📝'],
  ['style', '💄'],
  ['refactor', '♻️'],
  ['test', '✅'],
  ['chore', '🔧'],
  ['build', '📦'],
  ['ci', '👷'],
  ['perf', '⚡'],
  ['hotfix', '🚑'],
]);
const requiredLabels = ['What changed:', 'Why:', 'Evidence:'];
const forbiddenFragments = ['Co-authored-by:', 'Co-committed-by:', 'Generated with', 'Claude', 'Codex'];

function fail(message) {
  console.error(message);
  process.exit(1);
}

if (!messagePath) {
  fail('commit message path is required');
}

const message = readFileSync(messagePath, 'utf8').replace(/\r\n/g, '\n').trimEnd();
const [subject = ''] = message.split('\n');

if (/^(Merge|Revert|fixup!|squash!)/.test(subject)) {
  process.exit(0);
}

const gitmoji = [...gitmojiByType.values()].find((emoji) => subject.startsWith(`${emoji} `));
const subjectWithoutGitmoji = gitmoji ? subject.slice(gitmoji.length + 1) : subject;
const subjectPattern = new RegExp(`^(${allowedTypes.join('|')})(\\([a-z0-9-]+\\))?: .+$`);
const subjectMatch = subjectWithoutGitmoji.match(subjectPattern);

if (!gitmoji) {
  fail(`commit subject must start with a configured gitmoji and a space\nactual: ${subject}`);
}

if (!subjectMatch) {
  fail(`commit subject must match: <gitmoji> <type>(optional-scope): <한국어 요약>\nactual: ${subject}`);
}

const [, type] = subjectMatch;
const expectedGitmoji = gitmojiByType.get(type);

if (gitmoji !== expectedGitmoji) {
  fail(`commit subject gitmoji must match type "${type}": expected "${expectedGitmoji}"\nactual: ${subject}`);
}

if (!/[가-힣]/.test(subject)) {
  fail('commit subject must include Korean text');
}

if (subject.endsWith('.')) {
  fail('commit subject must not end with a period');
}

for (const label of requiredLabels) {
  if (!message.includes(label)) {
    fail(`commit message body must include "${label}"`);
  }
}

for (const fragment of forbiddenFragments) {
  if (message.includes(fragment)) {
    fail(`commit message must not include "${fragment}"`);
  }
}

console.log('commit message validation passed');
