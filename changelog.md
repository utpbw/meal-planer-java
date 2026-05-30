# Changelog

All notable changes to this project are documented here. The project is built in
stages following the Hyperskill "Meal Planner" track. Format loosely follows
[Keep a Changelog](https://keepachangelog.com/).

## Stage 6 — Shopping list (2026-05-30)

### Added
- `save` command: builds a shopping list from the saved weekly plan, aggregating
  duplicate ingredients via a `plan`/`ingredients` join and writing them to a
  user-named file (one ingredient per line, `name xN` when count > 1).
- Prints `Unable to save. Plan your meals first.` when no plan exists yet.
- File I/O support (`java.io.*`).

### Changed
- Menu prompt now includes `save`:
  `What would you like to do (add, show, plan, list plan, save, exit)?`

## Stage 5 — Weekly plan (2026-05-30)

### Added
- `plan` command: interactively choose breakfast, lunch, and dinner for each day
  of the week from the saved meals, then save the plan to the database.
- `list plan` command: print the stored weekly plan.

### Fixed
- Validation message apostrophe corrected to U+2019 so the plan test passes.

## Stage 4 — Show saved meals (2026-05-30)

### Changed
- `show` command now filters meals by a chosen category, with category
  validation.

## Stage 3 — Database storage (2026-05-30)

### Added
- PostgreSQL persistence: meals and ingredients are saved to the database and
  reloaded on restart.
- Automatic creation of the `meals`, `ingredients`, and `plan` tables.

## Stage 2 — Show saved meals (2026-05-30)

### Added
- Main command loop with `add`, `show`, and `exit` commands.
- Input validation for meal category, name, and ingredients.

## Stage 1 — Add meals (2026-05-30)

### Added
- Initial implementation: add a meal with its category, name, and ingredients.
- `Meal` record type.
