package jimlind.filmlinkd.system.letterboxd.utils;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.HashSet;

/** Utilities to build data sets that come from comparing Letterboxd IDs. */
public class LidComparer {
  /**
   * Compare Letterboxd IDs.
   *
   * <p>Negative Value: A is before B.<br>
   * Zero Value: A is the same as B.<br>
   * Positive Value: A is after B.
   *
   * <p>Shorter strings come before longer strings.<br>
   * Strings are normally compared lowest to highest [0-9][A-Z][a-z] Letterboxd LIDs are compared
   * lowest to highest [0-9][a-z][A-Z] so we need to swap cases before doing a comparison.
   *
   * @param letterboxdIdA Any Letterboxd ID (could represent any data model)
   * @param letterboxdIdB Any Letterboxd ID (could represent any data model)
   * @return Indicate which Letterboxd ID comes first
   */
  public static int compare(String letterboxdIdA, String letterboxdIdB) {
    if (letterboxdIdA == null || letterboxdIdB == null) {
      return 0;
    }

    if (letterboxdIdA.length() != letterboxdIdB.length()) {
      return letterboxdIdA.length() - letterboxdIdB.length();
    }

    return swapCase(letterboxdIdA).compareTo(swapCase(letterboxdIdB));
  }

  /**
   * Compare new Diary LID to existing list and add it while keeping things sorted and limiting the
   * total size of the list.
   *
   * @param list An input list of Letterboxd IDs
   * @param diaryLid A new Letterboxd ID to add to the existing list
   * @param count The maximum size of the returned list
   * @return A new list of Letterboxd IDs
   */
  public static ArrayList<String> buildMostRecentList(
      ArrayList<String> list, String diaryLid, int count) {
    list.add(diaryLid);
    ArrayList<String> uniqueList =
        new ArrayList<String>(new HashSet<String>(list)); // Remove duplicates
    uniqueList.sort(LidComparer::compare);
    int positiveCount = max(0, count);

    int fromIndex = max(0, uniqueList.size() - positiveCount);
    int toIndex = uniqueList.size();

    return new ArrayList<String>(uniqueList.subList(fromIndex, toIndex));
  }

  /**
   * Build a string where uppercase and lowercase characters are swapped to match Letterboxd string
   * sorting.
   */
  private static String swapCase(String input) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      stringBuilder.append(reverseCase(input.charAt(i)));
    }

    return stringBuilder.toString();
  }

  /** Make upper case if lower case. Make lower case if upper case. */
  private static char reverseCase(char c) {
    return Character.isLowerCase(c) ? Character.toUpperCase(c) : Character.toLowerCase(c);
  }
}
