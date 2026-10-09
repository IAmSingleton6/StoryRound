# AGENTS.md

## Project

This is a Kotlin Multiplatform real-time multiplayer story game.

The project documentation is the source of truth. Before implementing or modifying anything, consult the relevant documentation below.

## Documentation

### Clean Code & Engineering

All project-wide coding, architecture, testing, and engineering standards are documented in `docs/clean_code/`.

When writing or modifying code, follow the relevant guidance in this directory. This documentation takes precedence over personal coding preferences or introducing alternative patterns.

### Game Documentation

The overall game specification is split into these documents:

- `docs/game/architecture.md`
- `docs/game/screens.md`
- `docs/game/game_design.md`

`architecture.md` defines the technical architecture, including KMP structure, client/server responsibilities, WebSockets, state management, persistence, and networking. Read it before making architectural, networking, backend, or significant state-management changes.

`screens.md` defines the client UI, screens, navigation, transitions, and user interactions. Read it before creating or modifying screens or UI behaviour.

`game_design.md` defines the game itself, including the game loop, turns, timers, player behaviour, rules, and completion flow. Read it before implementing or modifying gameplay behaviour.

## Rules

- Treat the documentation as the source of truth.
- Read relevant documentation before coding.
- Inspect existing code before introducing new patterns or abstractions.
- Follow the clean-code conventions in `docs/clean_code/`.
- Follow the game architecture in `docs/game/architecture.md`.
- Follow the documented UI in `docs/game/screens.md`.
- Follow the documented game rules in `docs/game/game_design.md`.
- Prefer simple, maintainable solutions over unnecessary abstraction.
- Do not introduce dependencies, frameworks, modules, or patterns without a reason.
- Keep KMP code shared wherever practical.
- Keep platform-specific code isolated.
- Keep business and game logic out of the UI.
- Keep the server authoritative for multiplayer state.
- Add or update tests when changing behaviour.
- Avoid unrelated refactoring.

## Before Each Task

1. Determine which documentation is relevant.
2. Read it before implementing the change.
3. Inspect the existing implementation.
4. Make the smallest change that satisfies the requirements.
5. Follow the project's clean-code and architecture conventions.
6. Test the change.
7. Do not introduce behaviour that conflicts with the documented game design.

If a change intentionally alters an existing architectural or game-design decision, update the relevant documentation rather than leaving the documentation out of date.
