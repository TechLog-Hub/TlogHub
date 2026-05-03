import { readFileSync } from 'node:fs';
import { extname } from 'node:path';
import { execFileSync } from 'node:child_process';

const ignoredPrefixes = [
  '.git/',
  '.gradle/',
  'node_modules/',
  'build/',
  'apps/api/build/',
  'apps/web/.next/',
  '검토-필요/',
];

const ignoredExtensions = new Set([
  '.class',
  '.jar',
  '.png',
  '.jpg',
  '.jpeg',
  '.gif',
  '.webp',
  '.ico',
  '.pdf',
  '.zip',
  '.tar',
  '.gz',
  '.tgz',
  '.bz2',
  '.xz',
  '.7z',
  '.rar',
  '.exe',
  '.dll',
  '.so',
  '.dylib',
]);

const secretPatterns = [
  {
    name: 'Discord webhook URL',
    pattern: /https:\/\/discord(?:app)?\.com\/api\/webhooks\/\d+\/[A-Za-z0-9._-]+/g,
  },
  {
    name: 'GitHub token',
    pattern: /\b(?:ghp|gho|ghu|ghs|ghr)_[A-Za-z0-9_]{36,}\b/g,
  },
  {
    name: 'GitHub fine-grained token',
    pattern: /\bgithub_pat_[A-Za-z0-9_]{40,}\b/g,
  },
  {
    name: 'AWS access key',
    pattern: /\bAKIA[0-9A-Z]{16}\b/g,
  },
  {
    name: 'OpenAI API key',
    pattern: /\bsk-[A-Za-z0-9]{32,}\b/g,
  },
  {
    name: 'Slack token',
    pattern: /\bxox[baprs]-[A-Za-z0-9-]{10,}\b/g,
  },
  {
    name: 'Google API key',
    pattern: /\bAIza[0-9A-Za-z_-]{35}\b/g,
  },
];

function git(args) {
  return execFileSync('git', args, { encoding: 'utf8' });
}

const trackedFiles = git(['ls-files'])
  .split(/\r?\n/)
  .map((file) => file.trim())
  .filter(Boolean)
  .filter((file) => !ignoredPrefixes.some((prefix) => file.startsWith(prefix)))
  .filter((file) => !ignoredExtensions.has(extname(file).toLowerCase()));

const findings = [];

for (const file of trackedFiles) {
  let content;
  try {
    content = readFileSync(file, 'utf8');
  } catch {
    continue;
  }

  const lines = content.split(/\r?\n/);
  for (const { name, pattern } of secretPatterns) {
    pattern.lastIndex = 0;
    let match;
    while ((match = pattern.exec(content)) !== null) {
      const line = content.slice(0, match.index).split(/\r?\n/).length;
      findings.push({ file, line, name });
    }
  }

  lines.forEach((lineContent, index) => {
    if (/DISCORD_WEBHOOK_URL\s*=\s*https:\/\/discord/.test(lineContent)) {
      findings.push({ file, line: index + 1, name: 'Hard-coded Discord webhook env value' });
    }
  });
}

if (findings.length > 0) {
  console.error('Potential secrets found:');
  for (const finding of findings) {
    console.error(`- ${finding.file}:${finding.line} ${finding.name}`);
  }
  process.exit(1);
}

console.log('secret scan passed');
