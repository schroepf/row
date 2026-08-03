# Issue Tracker

This repo tracks issues as local markdown files under `.scratch/<feature>/<issue>.md`.

## Workflow
1. **Create an issue**: Write a markdown file under `.scratch/<feature>/<issue>.md`.
   - Example: `.scratch/auth/login-fails.md`
2. **Update status**: Edit the file to reflect progress (e.g., add comments, update labels).
3. **Close an issue**: Delete the file or move it to `.scratch/archive/`.

## Fields
Each issue file must include frontmatter with:
```yaml
---
title: "Issue title"
status: "open" | "closed" | "in-progress"
labels: ["needs-triage"]  # See triage-labels.md for valid labels
---
```

## Skills
- `to-tickets`, `triage`, `to-spec`, and `qa` read/write issues here.
- No external CLI tools are required.