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

function gitResult(args) {
  try {
    return {
      ok: true,
      output: execFileSync('git', args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim(),
    };
  } catch (error) {
    return {
      ok: false,
      output: `${error.stdout ?? ''}${error.stderr ?? ''}`.trim(),
    };
  }
}

function fail(message) {
  console.error(message);
  process.exit(1);
}

function assertRemoteBranch(branchName) {
  const result = gitResult(['show-ref', '--verify', '--quiet', `refs/remotes/origin/${branchName}`]);
  if (!result.ok) {
    fail(`required remote branch is missing: origin/${branchName}`);
  }
}

function isAncestor(ancestor, descendant) {
  return gitResult(['merge-base', '--is-ancestor', ancestor, descendant]).ok;
}

function validateBranchContext() {
  const defaultBranch = 'main';
  const integrationBranch = 'develop';
  const currentBranch = git(['branch', '--show-current']).trim();

  if (!currentBranch) {
    fail('current branch is empty or detached');
  }

  if ([defaultBranch, integrationBranch, 'master'].includes(currentBranch)) {
    fail(`do not commit directly on protected branch: ${currentBranch}`);
  }

  if (!/^(feat|refactor|hotfix)\/[a-z0-9]+(-[a-z0-9]+)*$/.test(currentBranch)) {
    fail(`branch must match feat|refactor|hotfix lowercase-kebab format: ${currentBranch}`);
  }

  assertRemoteBranch(defaultBranch);
  assertRemoteBranch(integrationBranch);

  const originHead = gitResult(['symbolic-ref', '--short', 'refs/remotes/origin/HEAD']);
  if (!originHead.ok || originHead.output !== `origin/${defaultBranch}`) {
    fail(`origin HEAD must point to origin/${defaultBranch}; run git remote set-head origin -a after updating GitHub default branch`);
  }

  const [branchType] = currentBranch.split('/');
  const expectedBase = branchType === 'hotfix' ? defaultBranch : integrationBranch;
  if (!isAncestor(`origin/${expectedBase}`, 'HEAD')) {
    fail(`current branch must contain origin/${expectedBase}: ${currentBranch}`);
  }

  const remoteWorkBranches = gitResult([
    'for-each-ref',
    '--format=%(refname:short)',
    'refs/remotes/origin/feat',
    'refs/remotes/origin/refactor',
    'refs/remotes/origin/hotfix',
  ]);

  if (!remoteWorkBranches.ok || !remoteWorkBranches.output) {
    return;
  }

  for (const remoteBranch of remoteWorkBranches.output.split(/\r?\n/).filter(Boolean)) {
    const shortName = remoteBranch.replace(/^origin\//, '');
    if (shortName === currentBranch) {
      continue;
    }

    const remoteBranchInHead = isAncestor(remoteBranch, 'HEAD');
    const remoteBranchIntegrated = isAncestor(remoteBranch, `origin/${expectedBase}`);
    if (remoteBranchInHead && !remoteBranchIntegrated) {
      fail(`current branch contains unintegrated work branch: ${remoteBranch}`);
    }
  }
}

validateBranchContext();

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
