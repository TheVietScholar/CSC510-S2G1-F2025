# Contributing to BoozeBuddies

Thanks for helping build **BoozeBuddies**! This guide explains how to propose changes, our review process, coding standards, licensing, and how we responsibly use AI assistance.

---

## Table of Contents
- [Contributing to BoozeBuddies](#contributing-to-boozebuddies)
  - [Table of Contents](#table-of-contents)
  - [Code of Conduct](#code-of-conduct)
  - [Acceptance of Contributions](#acceptance-of-contributions)
  - [Project Roles](#project-roles)
  - [Branching \& Workflow](#branching--workflow)
  - [Issues \& Proposals](#issues--proposals)
  - [Commit Messages](#commit-messages)
  - [Pull Requests](#pull-requests)
  - [AI-Assisted Contributions](#ai-assisted-contributions)
  - [Testing \& Coverage](#testing--coverage)
  - [Style \& Linting](#style--linting)
  - [Security \& Secrets](#security--secrets)
  - [Intellectual Property \& License](#intellectual-property--license)
  - [Public Availability](#public-availability)
  - [Contact Information](#contact-information)

---

## Code of Conduct
All contributors must follow our [Code of Conduct](**CODE_OF_CONDUCT.md**).
- Be respectful and inclusive.
- Collaborate in good faith.
- No discrimination, harassment, or disrespectful behavior.

---

## Acceptance of Contributions
We welcome contributions of all kinds:
- 🐛 Bug fixes  
- ⚙️ Feature enhancements  
- 📝 Documentation updates  
- 🎓 Tutorials, examples, and tests

Before submitting, please review this policy to ensure quality and consistency.

---

## Project Roles
- **Maintainers/Owners**: final approvals, releases, roadmap.
- **Contributors**: features, fixes, tests, docs.
- **Reviewers**: cross-team reviews (Team A ↔ Team B).

> **Minimum review requirement:** Every PR must be approved by **at least two team members**, including **one reviewer from the other sub-team** when touching shared modules.

---

## Branching & Workflow
- Default branch: **`main`**
- Use short-lived branches:  
  `feature/<scope>-<short-desc>` or `fix/<issue-id>-<desc>`
- Keep PRs **small & focused** (aim < 500 LOC reviewable diff).
- Rebase or squash before merge; avoid merge commits to `main`.
- Link issues in PR descriptions when applicable.

**Typical flow**
1. Create an issue (or pick an existing one).  
2. Branch off `main`.  
3. Commit early/often (see Conventional Commits below).  
4. Open a Draft PR to get feedback.  
5. Mark Ready for Review when it’s green locally and in CI.

---

## Issues & Proposals
- Use **Issues** for bugs, enhancements, and questions.
- For significant changes, attach a short **design note** (problem, options, decision).
- Tag appropriately: `bug`, `feature`, `docs`, `tech-debt`.

---

## Commit Messages
Follow **Conventional Commits**:

```
feat(order): add pickup flow
fix(driver): prevent double assignment in dispatcher
docs(README): update local run instructions
test(delivery): add end-to-end status transition
refactor: extract totals calculator to service
```

Add `BREAKING CHANGE:` footer when relevant. Reference issues (e.g., `#123`).

---

## Pull Requests
**PR checklist (required)**
- [ ] Motivation, scope, and approach described
- [ ] Small & focused (or explain why not)
- [ ] Screenshots/GIF for UI changes
- [ ] Unit/acceptance tests added or updated
- [ ] Lint & format pass locally
- [ ] No secrets/keys in diff
- [ ] **AI Assistance** section completed (if any AI was used)
- [ ] At least **two approvals**, including **cross-team** (if applicable)

**Local verification (Maven):**

```
# Format & style
./mvnw -q spotless:apply
./mvnw -q spotless:check
./mvnw -q checkstyle:check

# Tests
./mvnw -q -DskipITs=false verify
```

---

## AI-Assisted Contributions
AI can accelerate work, but **humans are responsible** for correctness, security, and licensing.

**Required disclosure in each PR using AI:**
- **Source**: tool/model (e.g., “ChatGPT (GPT-5)”, “Claude Sonnet 4”).
- **Scope**: what was AI-generated (code, tests, docs, commit text, etc.).
- **Review**: you **manually reviewed and edited** the AI output for logic, correctness, style, and security.
- **Attribution**: no license-incompatible or copyrighted material was introduced.
- **Privacy**: no sensitive or non-public data was shared with AI tools.
- **Provenance** (optional but recommended): attach prompts or a summary as PR comments.

**Double-check requirement (must pass before merge):**
1. Compiles and runs locally (or passes CI).  
2. Reviewed by a **human not involved** in generating the AI content.  
3. **Tests** cover the change (bugfix → regression test; feature → unit + happy-path acceptance).  
4. **Security check** for obvious risks (injection, auth/perm bypass, PII leakage, unsafe defaults).

**Prohibited AI usage**
- Generating or altering secrets/keys or auth material.  
- Introducing **license-incompatible** code.  
- Fabricating benchmarks or test results.

---

## Testing & Coverage
- Backend statement coverage: **≥ 70%** (PRs below this must justify exceptions).
- Add tests for nontrivial logic, state transitions, and error paths.
- Provide E2E acceptance tests for the **order → dispatch → deliver** happy path.
- Prefer **Testcontainers (MySQL)** for integration tests.

---

## Style & Linting
We enforce **Google Java Style** project-wide.

- **Format**: `google-java-format` via **Spotless (Maven)**  
- **Static checks**: **Checkstyle** (Google rules + small tweaks)  
- **Editor**: `.editorconfig` for consistent whitespace & line endings  
- **CI**: PRs fail on style/lint/test violations

**Key files**
- `pom.xml` (Spotless + Checkstyle plugins)
- `config/checkstyle/checkstyle.xml`
- `.editorconfig`
- Google Java Style: https://google.github.io/styleguide/javaguide.html

---

## Security & Secrets
- Never commit secrets, API keys, tokens, or credentials.
- Use environment variables or CI secrets.
- Report vulnerabilities privately to maintainers.

---

## Intellectual Property & License
Contributors retain copyright to their individual contributions.
By submitting a contribution, you **license it under the project’s MIT License**, consistent with the rest of the repository.

---

## Public Availability
This policy is publicly available at **`CONTRIBUTING.md`** in the repository so anyone can reference and follow our guidelines.

---

## Contact Information
For questions or clarifications, contact the maintainers:

| Name           | Phone        | Email              |
|----------------|--------------|--------------------|
| Robert Kemp    | 252-414-6551 | rckemp2@ncsu.edu   |
| Matthew Nguyen | 919-267-0396 | mknguye2@ncsu.edu  |
| Rishi Jeswani  | 984-480-7289 | rjeswan2@ncsu.edu  |
| Tiehang Zhang  | 919-810-6663 | tzhang33@ncsu.edu  |
