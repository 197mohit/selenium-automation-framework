configure repo identity for telemetry and dashboards.

Configure repo identity for OTel tracing and dashboard visibility.
Sets team_name, main_branch, and repo slug as mandatory trace attributes.
Use -i for interactive mode or pass --team-name and --main-branch.

**parameters:**
- `interactive`: interactive registration
- `team_name`: team name for this repo
- `main_branch`: primary branch name

**examples:**
  foundry register -i                                          interactive registration
  foundry register --team-name platform --main-branch main     non-interactive

load context:
- .foundry/state.json
- .foundry/tasks.json
- .foundry/people.md

integrations enabled: bitbucket

current tasks: 76 total (19 backlog, 56 done, 1 in-progress)

$ARGUMENTS
