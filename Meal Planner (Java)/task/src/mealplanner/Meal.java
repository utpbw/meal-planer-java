package mealplanner;

/** Immutable value type representing a persisted meal with its database id, category, name, and ingredients. */
record Meal(int id, String category, String name, String[] ingredients) {}
