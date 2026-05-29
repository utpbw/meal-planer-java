package mealplanner;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Entry point for the Meal Planner application.
 * Prompts the user for a meal's category, name, and ingredients, then displays a summary.
 */
public class Main {

  /**
   * Reads one meal from stdin, prints its details, and exits.
   */
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
    String category = scanner.nextLine().trim();

    System.out.println("Input the meal's name:");
    String name = scanner.nextLine().trim();

    System.out.println("Input the ingredients:");
    String ingredientsLine = scanner.nextLine();
    String[] ingredients = Arrays.stream(ingredientsLine.split(","))
        .map(String::trim)
        .toArray(String[]::new);

    System.out.println("Category: " + category);
    System.out.println("Name: " + name);
    System.out.println("Ingredients:");
    for (String ingredient : ingredients) {
      System.out.println(ingredient);
    }
    System.out.println("The meal has been added!");
  }
}
