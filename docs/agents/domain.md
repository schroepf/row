# Domain Docs

## Layout
Single-context:
- `CONTEXT.md` at the repo root: Ubiquitous language, domain model, and high-level architecture.
- `docs/adr/`: Architectural Decision Records (ADRs) as markdown files.

## Consumer Rules
1. **Read `CONTEXT.md`** before implementing features or fixing bugs.
2. **Update `CONTEXT.md`** when domain terminology or rules change.
3. **Create ADRs** for non-trivial architectural decisions (template: `docs/adr/NNNN-title.md`).
4. **Link ADRs** in `CONTEXT.md` under a "Decisions" section.