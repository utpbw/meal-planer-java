package mealplanner;

import java.sql.*;
import java.util.*;

/**
 * Entry point for the Meal Planner application.
 * Persists meals and weekly plans in a PostgreSQL database so data survives restarts.
 */
public class Main {

  private static final List<String> VALID_CATEGORIES = List.of("breakfast", "lunch", "dinner");
  private static final String[] DAYS = {
      "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
  };
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
        System.out.println("What would you like to do (add, show, plan, list plan, exit)?");
        String action = scanner.nextLine().trim();
        switch (action) {
          case "add" -> addMeal(scanner, meals);
          case "show" -> showMeals(scanner, meals);
          case "plan" -> planWeek(scanner, meals);
          case "list plan" -> listPlan();
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

  /** Creates the meals, ingredients, and plan tables if they do not already exist. */
  private static void createTables() throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS meals (" +
          "meal_id INTEGER, category VARCHAR(255), meal VARCHAR(255))");
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS ingredients (" +
          "ingredient_id INTEGER, ingredient VARCHAR(255), meal_id INTEGER)");
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS plan (" +
          "day_of_week VARCHAR(20), meal_category VARCHAR(20), meal_id INTEGER)");
    }
  }

  /**
   * Reads all meals from the database in insertion order with their ingredients,
   * and initialises ID counters for subsequent inserts.
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
            while (irs.next()) ingredientList.add(irs.getString("ingredient"));
          }
        }
        meals.add(new Meal(mealId, category, name, ingredientList.toArray(new String[0])));
      }
    }
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT MAX(ingredient_id) FROM ingredients")) {
      if (rs.next() && rs.getObject(1) != null) nextIngredientId = rs.getInt(1) + 1;
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

    int mealId = nextMealId++;
    Meal meal = new Meal(mealId, category, name, ingredients);
    saveMeal(meal);
    meals.add(meal);
    System.out.println("The meal has been added!");
  }

  /** Inserts a meal and its ingredients into the database, linking them by meal_id. */
  private static void saveMeal(Meal meal) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO meals (meal_id, category, meal) VALUES (?, ?, ?)")) {
      ps.setInt(1, meal.id());
      ps.setString(2, meal.category());
      ps.setString(3, meal.name());
      ps.executeUpdate();
    }
    try (PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO ingredients (ingredient_id, ingredient, meal_id) VALUES (?, ?, ?)")) {
      for (String ingredient : meal.ingredients()) {
        ps.setInt(1, nextIngredientId++);
        ps.setString(2, ingredient);
        ps.setInt(3, meal.id());
        ps.executeUpdate();
      }
    }
  }

  /**
   * Asks for a category, then prints only the meals in that category
   * or a message when none exist for it.
   */
  private static void showMeals(Scanner scanner, List<Meal> meals) {
    String category;
    while (true) {
      System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
      category = scanner.nextLine().trim();
      if (VALID_CATEGORIES.contains(category)) break;
      System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
    }

    final String selectedCategory = category;
    List<Meal> filtered = meals.stream()
        .filter(m -> m.category().equals(selectedCategory))
        .toList();

    if (filtered.isEmpty()) {
      System.out.println("No meals found.");
      return;
    }

    System.out.println("Category: " + category);
    for (Meal meal : filtered) {
      System.out.println("Name: " + meal.name());
      System.out.println("Ingredients:");
      for (String ingredient : meal.ingredients()) {
        System.out.println(ingredient);
      }
    }
  }

  /**
   * Interactively builds a weekly meal plan day by day, saves it to the database,
   * then prints the complete plan.
   */
  private static void planWeek(Scanner scanner, List<Meal> meals) throws SQLException {
    String[][] plan = new String[7][3];

    for (int d = 0; d < DAYS.length; d++) {
      System.out.println(DAYS[d]);

      for (int c = 0; c < VALID_CATEGORIES.size(); c++) {
        String category = VALID_CATEGORIES.get(c);
        List<String> categoryMeals = meals.stream()
            .filter(m -> m.category().equals(category))
            .map(Meal::name)
            .sorted()
            .toList();

        for (String mealName : categoryMeals) {
          System.out.println(mealName);
        }
        System.out.println("Choose the " + category + " for " + DAYS[d] + " from the list above:");
        System.out.flush();

        while (true) {
          String choice = scanner.nextLine().trim();
          if (categoryMeals.contains(choice)) {
            plan[d][c] = choice;
            break;
          }
          System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
          System.out.flush();
        }
      }

      System.out.println("Yeah! We planned the meals for " + DAYS[d] + ".");
      System.out.println();
    }

    savePlanToDb(meals, plan);
    printPlan(plan);
  }

  /** Deletes any existing plan and inserts the new one, keyed by meal_id. */
  private static void savePlanToDb(List<Meal> meals, String[][] plan) throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("DELETE FROM plan");
    }
    try (PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO plan (day_of_week, meal_category, meal_id) VALUES (?, ?, ?)")) {
      for (int d = 0; d < DAYS.length; d++) {
        for (int c = 0; c < VALID_CATEGORIES.size(); c++) {
          String mealName = plan[d][c];
          String cat = VALID_CATEGORIES.get(c);
          int mealId = -1;
          for (Meal meal : meals) {
            if (meal.name().equals(mealName) && meal.category().equals(cat)) {
              mealId = meal.id();
              break;
            }
          }
          ps.setString(1, DAYS[d]);
          ps.setString(2, cat);
          ps.setInt(3, mealId);
          ps.executeUpdate();
        }
      }
    }
  }

  /** Prints the weekly plan in day/Breakfast/Lunch/Dinner format. */
  private static void printPlan(String[][] plan) {
    for (int d = 0; d < DAYS.length; d++) {
      System.out.println(DAYS[d]);
      System.out.println("Breakfast: " + plan[d][0]);
      System.out.println("Lunch: " + plan[d][1]);
      System.out.println("Dinner: " + plan[d][2]);
      System.out.println();
    }
  }

  /** Loads and prints the stored weekly plan, or an error message if no plan exists. */
  private static void listPlan() throws SQLException {
    String[][] plan = loadPlanFromDb();
    if (plan == null) {
      System.out.println("Database does not contain any meal plans.");
      return;
    }
    printPlan(plan);
  }

  /**
   * Reads the weekly plan from the database joined with meal names.
   * Returns null when no plan is stored.
   */
  private static String[][] loadPlanFromDb() throws SQLException {
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM plan")) {
      rs.next();
      if (rs.getInt(1) == 0) return null;
    }

    Map<String, Map<String, String>> planMap = new HashMap<>();
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(
             "SELECT p.day_of_week, p.meal_category, m.meal " +
             "FROM plan p JOIN meals m ON p.meal_id = m.meal_id")) {
      while (rs.next()) {
        String day = rs.getString("day_of_week");
        String cat = rs.getString("meal_category");
        String mealName = rs.getString("meal");
        planMap.computeIfAbsent(day, k -> new HashMap<>()).put(cat, mealName);
      }
    }

    String[][] plan = new String[7][3];
    for (int d = 0; d < DAYS.length; d++) {
      Map<String, String> dayPlan = planMap.getOrDefault(DAYS[d], Collections.emptyMap());
      for (int c = 0; c < VALID_CATEGORIES.size(); c++) {
        plan[d][c] = dayPlan.get(VALID_CATEGORIES.get(c));
      }
    }
    return plan;
  }
}
