package mealplanner;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Meal Planner application.
 * Persists meals in a PostgreSQL database so data survives restarts.
 */
public class Main {

  private static final List<String> VALID_CATEGORIES = List.of("breakfast", "lunch", "dinner");
  private static final String DB_URL = "jdbc:postgresql:meals_db";
  private static final String DB_USER = "postgres";
  private static final String DB_PASS = "1111";

  private static Connection connection;
  private static int nextMealId = 1;
  private static int nextIngredientId = 1;

  /** Connects to the database, creates tables if needed, then drives the command loop. */
  public static void main(String[] args) throws Exception {
    connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    connection.setAutoCommit(true);

    try {
      createTables();
      List<Meal> meals = loadMeals();
      Scanner scanner = new Scanner(System.in);

      while (true) {
        System.out.println("What would you like to do (add, show, exit)?");
        String action = scanner.nextLine().trim();
        switch (action) {
          case "add" -> addMeal(scanner, meals);
          case "show" -> showMeals(meals);
          case "exit" -> {
            System.out.println("Bye!");
            return;
          }
        }
      }
    } finally {
      connection.close();
    }
  }

  /** Creates the meals and ingredients tables if they do not already exist. */
  private static void createTables() throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS meals (" +
          "meal_id INTEGER, " +
          "category VARCHAR(255), " +
          "meal VARCHAR(255)" +
          ")"
      );
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS ingredients (" +
          "ingredient_id INTEGER, " +
          "ingredient VARCHAR(255), " +
          "meal_id INTEGER" +
          ")"
      );
    }
  }

  /**
   * Reads all meals from the database in insertion order, initialises ID counters
   * so new inserts continue from the correct next value.
   */
  private static List<Meal> loadMeals() throws SQLException {
    List<Meal> meals = new ArrayList<>();
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(
             "SELECT meal_id, category, meal FROM meals ORDER BY meal_id")) {

      while (rs.next()) {
        int mealId = rs.getInt("meal_id");
        String category = rs.getString("category");
        String name = rs.getString("meal");

        if (mealId >= nextMealId) nextMealId = mealId + 1;

        List<String> ingredientList = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT ingredient FROM ingredients WHERE meal_id = ? ORDER BY ingredient_id")) {
          ps.setInt(1, mealId);
          try (ResultSet irs = ps.executeQuery()) {
            while (irs.next()) {
              ingredientList.add(irs.getString("ingredient"));
            }
          }
        }
        meals.add(new Meal(category, name, ingredientList.toArray(new String[0])));
      }
    }

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT MAX(ingredient_id) FROM ingredients")) {
      if (rs.next() && rs.getObject(1) != null) {
        nextIngredientId = rs.getInt(1) + 1;
      }
    }

    return meals;
  }

  /**
   * Prompts for and validates a meal, persists it to the database,
   * and appends it to the in-memory list.
   */
  private static void addMeal(Scanner scanner, List<Meal> meals) throws SQLException {
    String category;
    while (true) {
      System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
      category = scanner.nextLine().trim();
      if (VALID_CATEGORIES.contains(category)) break;
      System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
    }

    String name;
    while (true) {
      System.out.println("Input the meal's name:");
      String input = scanner.nextLine();
      if (!input.isBlank() && input.trim().matches("[a-zA-Z ]+")) {
        name = input.trim();
        break;
      }
      System.out.println("Wrong format. Use letters only!");
    }

    String[] ingredients;
    while (true) {
      System.out.println("Input the ingredients:");
      String[] parts = scanner.nextLine().split(",");
      if (Arrays.stream(parts).allMatch(p -> !p.isBlank() && p.trim().matches("[a-zA-Z ]+"))) {
        ingredients = Arrays.stream(parts).map(String::trim).toArray(String[]::new);
        break;
      }
      System.out.println("Wrong format. Use letters only!");
    }

    Meal meal = new Meal(category, name, ingredients);
    saveMeal(meal);
    meals.add(meal);
    System.out.println("The meal has been added!");
  }

  /** Inserts a meal and its ingredients into the database, linking them by meal_id. */
  private static void saveMeal(Meal meal) throws SQLException {
    int mealId = nextMealId++;
    try (PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO meals (meal_id, category, meal) VALUES (?, ?, ?)")) {
      ps.setInt(1, mealId);
      ps.setString(2, meal.category());
      ps.setString(3, meal.name());
      ps.executeUpdate();
    }

    try (PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO ingredients (ingredient_id, ingredient, meal_id) VALUES (?, ?, ?)")) {
      for (String ingredient : meal.ingredients()) {
        ps.setInt(1, nextIngredientId++);
        ps.setString(2, ingredient);
        ps.setInt(3, mealId);
        ps.executeUpdate();
      }
    }
  }

  /** Prints all saved meals in insertion order, or a message when none exist. */
  private static void showMeals(List<Meal> meals) {
    if (meals.isEmpty()) {
      System.out.println("No meals saved. Add a meal first.");
      return;
    }
    for (Meal meal : meals) {
      System.out.println("Category: " + meal.category());
      System.out.println("Name: " + meal.name());
      System.out.println("Ingredients:");
      for (String ingredient : meal.ingredients()) {
        System.out.println(ingredient);
      }
    }
  }
}
