package mealplanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Meal Planner application.
 * Runs an interactive loop letting the user add and view meals until they exit.
 */
public class Main {

  private static final List<String> VALID_CATEGORIES = List.of("breakfast", "lunch", "dinner");

  /** Drives the main add/show/exit command loop. */
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    List<Meal> meals = new ArrayList<>();

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
  }

  /**
   * Prompts the user for a meal category, name, and ingredients (with validation),
   * then appends the new meal to the list.
   */
  private static void addMeal(Scanner scanner, List<Meal> meals) {
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

    meals.add(new Meal(category, name, ingredients));
    System.out.println("The meal has been added!");
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
