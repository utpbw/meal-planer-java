# Meal Planner (Java)

A console application for planning your weekly meals and generating a shopping
list. Meals and the weekly plan are persisted in a PostgreSQL database, so your
data survives restarts. This is the [Hyperskill](https://hyperskill.org/) "Meal
Planner" project, built stage by stage.

## Features

- **Add meals** — store a meal under a category (breakfast, lunch, dinner) with a
  list of ingredients. Input is validated (letters only).
- **Show meals** — print all saved meals for a chosen category.
- **Plan the week** — pick a breakfast, lunch, and dinner for each day of the
  week from the saved meals; the plan is saved to the database.
- **List plan** — print the saved weekly plan.
- **Save shopping list** — generate a shopping list from the weekly plan,
  aggregating duplicate ingredients (`name xN`), and write it to a file.

## Requirements

- Java 17+
- PostgreSQL, with a database named `meals_db`

The database connection is configured in
`Meal Planner (Java)/task/src/mealplanner/Main.java`:

```java
private static final String DB_URL  = "jdbc:postgresql:meals_db";
private static final String DB_USER = "postgres";
private static final String DB_PASS = "1111";
```

Adjust these constants to match your local PostgreSQL setup. The tables
(`meals`, `ingredients`, `plan`) are created automatically on first run.

## Build & Run

From the repository root:

```bash
./gradlew compileJava        # compile
./gradlew run                # run (if the application plugin is configured)
```

Or run `mealplanner.Main` directly from your IDE.

## Usage

When started, the program loops on a menu:

```
What would you like to do (add, show, plan, list plan, save, exit)?
```

| Command     | Action                                                        |
|-------------|---------------------------------------------------------------|
| `add`       | Add a new meal with ingredients.                              |
| `show`      | Print saved meals for a category.                            |
| `plan`      | Build and save the weekly plan.                              |
| `list plan` | Print the saved weekly plan.                                 |
| `save`      | Write the shopping list to a file (requires an existing plan).|
| `exit`      | Quit the program.                                            |

### Shopping list example

After planning the week, `save` produces a file like:

```
eggs x5
milk x6
cheese x9
bacon
tomato x6
```

Each ingredient appears once; a count suffix `xN` is added when an ingredient is
needed more than once across the plan.

## Project structure

```
Meal Planner (Java)/task/
├── src/mealplanner/
│   ├── Main.java   # entry point, command loop, DB + file I/O
│   └── Meal.java   # immutable record (id, category, name, ingredients)
└── test/
    └── MealPlannerTests.java   # Hyperskill stage tests
```

## Development

Built incrementally across six stages — see [changelog.md](changelog.md) for the
history.
