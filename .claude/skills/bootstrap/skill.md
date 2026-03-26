---
description: Bootstrap a new project from this template. Interactive setup that asks for project name, package, handler selection, and then renames/restructures everything.
user_invocable: true
---

# Bootstrap New Project from Template

You are scaffolding a new project from the `template-aws-quarkus-lambda-cdk` template.

## Instructions

1. Read `BOOTSTRAP.md` at the project root — it contains the complete scaffolding specification.
2. Follow the **Questions to Ask** section: ask all questions in a single message, presenting the options clearly.
3. Wait for the user's answers before making any changes.
4. Show the user a summary of what will be done (derived values, handlers to keep/remove, shared module decision) and ask for confirmation.
5. Execute the **Transformation Steps** from BOOTSTRAP.md in order.
6. After all transformations, verify the build compiles.
7. Present the **Post-Setup Guidance** to the user.

## Important

- Do NOT start making changes until the user confirms the summary.
- Follow AGENTS.md coding guidelines for any generated code.
- If the clone came from a remote URL (empty directory flow), clone first, then read BOOTSTRAP.md.
