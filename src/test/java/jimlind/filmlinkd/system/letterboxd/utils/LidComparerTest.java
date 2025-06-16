package jimlind.filmlinkd.system.letterboxd.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LidComparerTest {

  @Nested
  @DisplayName("compare(String letterboxdIdA, String letterboxdIdB)")
  class CompareTests {

    @Test
    @DisplayName("should return 0 when both IDs are null")
    void compare_bothNull_returnsZero() {
      assertEquals(0, LidComparer.compare(null, null));
    }

    @Test
    @DisplayName("should return 0 when first ID is null")
    void compare_firstNull_returnsZero() {
      assertEquals(0, LidComparer.compare(null, "abc"));
    }

    @Test
    @DisplayName("should return 0 when second ID is null")
    void compare_secondNull_returnsZero() {
      assertEquals(0, LidComparer.compare("abc", null));
    }

    @Test
    @DisplayName("should return negative when A is shorter than B")
    void compare_aShorter_returnsNegative() {
      assertTrue(LidComparer.compare("a", "aa") < 0);
    }

    @Test
    @DisplayName("should return positive when A is longer than B")
    void compare_aLonger_returnsPositive() {
      assertTrue(LidComparer.compare("aa", "a") > 0);
    }

    @Test
    @DisplayName("should return 0 when A and B are identical")
    void compare_identical_returnsZero() {
      assertEquals(0, LidComparer.compare("abc123XYZ", "abc123XYZ"));
    }

    @ParameterizedTest
    @CsvSource({
      // Standard comparison: [0-9][A-Z][a-z]
      // LidComparer:       [0-9][a-z][A-Z]
      "1, 2, -1", // Numbers
      "a, b, -1", // Lowercase
      "A, B, -1", // Uppercase
      "9, a, -1", // Number vs Lowercase
      "z, A, -1", // Lowercase vs Uppercase (LidComparer specific)
      "A, z, 1", // Uppercase vs Lowercase (LidComparer specific, 'A' comes after 'z')
      "abc, abD, -1", // 'c' (swapped to 'C') vs 'D' (swapped to 'd') -> C vs d
      "abD, abc, 1", // 'D' (swapped to 'd') vs 'c' (swapped to 'C') -> d vs C
      "film1, filmA, -1", // '1' vs 'A' (swapped to 'a') -> 1 vs a
      "filmA, film1, 1", // 'A' (swapped to 'a') vs '1' -> a vs 1
      "Test, test, 1", // 'T' (swapped to 't') vs 't' (swapped to 'T') -> t vs T
      "test, Test, -1" // 't' (swapped to 'T') vs 'T' (swapped to 't') -> T vs t
    })
    @DisplayName("should correctly compare IDs based on custom Letterboxd order")
    void compare_customOrder(String idA, String idB, int expectedSign) {
      int result = LidComparer.compare(idA, idB);
      if (expectedSign < 0) {
        assertTrue(result < 0, "'" + idA + "' should be before '" + idB + "'");
      } else if (expectedSign > 0) {
        assertTrue(result > 0, "'" + idA + "' should be after '" + idB + "'");
      } else {
        assertEquals(0, result, "'" + idA + "' should be equal to '" + idB + "'");
      }
    }

    @Test
    @DisplayName("should handle complex mixed case and numbers")
    void compare_complexMixedCaseAndNumbers() {
      // LidComparer: [0-9][a-z][A-Z]
      // "a1B" -> swapCase -> "A1b"
      // "A1b" -> swapCase -> "a1B"
      // "A1b" comes after "a1B" in standard compare, so "a1B" is before "A1b" in LidComparer
      assertTrue(LidComparer.compare("a1B", "A1b") < 0); // A1b vs a1B
      assertTrue(LidComparer.compare("A1b", "a1B") > 0); // a1B vs A1b
    }
  }

  @Nested
  @DisplayName("buildMostRecentList(ArrayList<String> list, String diaryLid, int count)")
  class BuildMostRecentListTests {

    @Test
    @DisplayName("should add LID to empty list and respect count")
    void build_addToEmptyList() {
      ArrayList<String> initialList = new ArrayList<>();
      String newLid = "new1";
      int count = 5;
      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);

      assertEquals(1, result.size());
      assertTrue(result.contains(newLid));
      // Check original list is not the same instance if modified (it is modified then new list
      // created)
      // The method signature takes ArrayList, adds to it, then creates new ones.
      // So initialList will contain "new1" after the call.
      assertTrue(initialList.contains(newLid), "Original list should be modified by add()");
    }

    @Test
    @DisplayName("should add LID to existing list, sort, and trim")
    void build_addToExistingListAndTrim() {
      // LidComparer sort order: numbers, then lowercase, then uppercase
      // "c", "B", "a", "1" -> "1", "a", "c", "B"
      ArrayList<String> initialList = new ArrayList<>(Arrays.asList("c", "B")); // Sorted: B, c
      String newLid = "a"; // Add "a" -> B, c, a -> sorted by LidComparer: a, c, B
      int count = 2;
      // Expected after sort: "a", "c", "B"
      // Expected after trim (count 2, most recent): "c", "B"
      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);

      assertEquals(2, result.size());
      assertEquals(Arrays.asList("c", "B"), result);
    }

    @Test
    @DisplayName("should handle duplicate LIDs, sort, and trim")
    void build_handleDuplicates() {
      ArrayList<String> initialList =
          new ArrayList<>(Arrays.asList("a", "c", "B")); // Sorted: a, B, c
      String newLid = "c"; // Duplicate
      int count = 3;
      // Expected after add and unique: "a", "c", "B"
      // Expected after sort: "a", "c", "B" (LidComparer: a, c, B)
      // Expected after trim (count 3): "a", "c", "B"
      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);

      assertEquals(3, result.size());
      assertEquals(Arrays.asList("a", "c", "B"), result);
    }

    @Test
    @DisplayName("should return correct sublist when count is less than total unique LIDs")
    void build_countLessThanTotal() {
      ArrayList<String> initialList = new ArrayList<>(Arrays.asList("z", "M", "1", "x"));
      String newLid = "5";
      int count = 3;
      // LIDs: "z", "M", "1", "x", "5"
      // Unique: "z", "M", "1", "x", "5"
      // Sorted by LidComparer: "1", "5", "x", "z", "M"
      // Trimmed to 3 most recent: "x", "z", "M"
      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);

      assertEquals(3, result.size());
      assertEquals(Arrays.asList("x", "z", "M"), result);
    }

    @Test
    @DisplayName("should return all LIDs when count is greater than total unique LIDs")
    void build_countGreaterThanTotal() {
      ArrayList<String> initialList = new ArrayList<>(Arrays.asList("b", "A"));
      String newLid = "1";
      int count = 5;
      // LIDs: "b", "A", "1"
      // Unique: "b", "A", "1"
      // Sorted by LidComparer: "1", "b", "A"
      // Trimmed to 5 most recent: "1", "b", "A"
      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);

      assertEquals(3, result.size());
      assertEquals(Arrays.asList("1", "b", "A"), result);
    }

    @Test
    @DisplayName("should return empty list when count is 0")
    void build_countIsZero() {
      ArrayList<String> initialList = new ArrayList<>(Arrays.asList("a", "b"));
      String newLid = "c";
      int count = 0;
      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should handle negative count as 0, returning empty list")
    void build_negativeCount() {
      ArrayList<String> initialList = new ArrayList<>(Arrays.asList("a", "b"));
      String newLid = "c";
      int count = -2;
      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return a new list instance")
    void build_returnsNewListInstance() {
      ArrayList<String> initialList = new ArrayList<>(Collections.singletonList("a"));
      String newLid = "b";
      int count = 1;
      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);

      assertNotSame(
          initialList,
          result,
          "Should return a new list instance, not modify in-place and return same.");
      // The original list *is* modified by the .add() call before new lists are created.
      assertEquals(2, initialList.size()); // Contains "a", "b"
      assertEquals(1, result.size()); // Contains "b" (if "a" < "b" by LidComparer)
      // "a" -> "A", "b" -> "B". "A" < "B". So sorted: "a", "b". Last 1 is "b".
      assertEquals(Collections.singletonList("b"), result);
    }

    @Test
    @DisplayName("should correctly sort with mixed case and numbers before trimming")
    void build_complexSortAndTrim() {
      ArrayList<String> initialList =
          new ArrayList<>(Arrays.asList("Movie99", "filmAb", "Action1"));
      String newLid = "epicZ"; // lowercase 'e', uppercase 'Z'
      int count = 2;
      // Combined: "Movie99", "filmAb", "Action1", "epicZ"
      // Sorted: "epicZ", "filmAb", "Action1", "Movie99"
      // Trimmed: "Action1", "Movie99"

      ArrayList<String> result = LidComparer.buildMostRecentList(initialList, newLid, count);
      List<String> expected = Arrays.asList("Action1", "Movie99");

      assertEquals(2, result.size());
      assertEquals(expected, result);
    }
  }
}
