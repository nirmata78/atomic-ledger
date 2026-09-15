# T-010 — Polish & publish checklist

**Module:** root · **Closes:** nothing new technically — this is the "make it presentable" pass,
once T-001–T-008 (and optionally T-009) are done.

## Checklist

- [ ] `./gradlew build` green across all four modules (Docker running).
- [ ] `tasks/README.md`'s status table updated to reflect what's actually done — an unchecked box
      next to finished work is worse than an honest gap, because it reads as unfinished when it
      isn't.
- [ ] Each `NaiveLedgerTransferService`/isolation/settlement task file has your one-sentence
      takeaway written in, not left as a TODO placeholder — these are your talking points, write
      them while the material is fresh.
- [ ] `.github/workflows/ci.yml` passes on a real push (GitHub Actions' `ubuntu-latest` runners
      have Docker available by default, so the Testcontainers-based tests should run there without
      extra setup).
- [ ] `.github/workflows/pages.yml` publishes `docs/` successfully once Pages is enabled
      (Settings → Pages → Source: GitHub Actions).
- [ ] Skim `README.md` and this file's own repo for anything that still says "stub" where the code
      no longer is one.
- [ ] Decide on a repo visibility timeline — public from the start, or public once T-001–T-008 are
      done and green. Either is defensible; just decide on purpose rather than by default.

## Publishing

```bash
gh repo create atomic-ledger --public --source=. --remote=origin
git push -u origin main
```

Then Settings → Pages → Source: GitHub Actions, and the next push triggers `pages.yml`.
