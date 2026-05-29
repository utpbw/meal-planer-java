package mealplanner;

/** Immutable value type representing a meal with a category, name, and ordered ingredient list. */
record Meal(String category, String name, String[] ingredients) {}
