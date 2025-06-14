package jimlind.filmlinkd.system.letterboxd.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import jimlind.filmlinkd.system.letterboxd.model.LBImage;
import jimlind.filmlinkd.system.letterboxd.model.LBImageSize;
import org.junit.jupiter.api.Test;

public class ImageUtilsTest {

  private LBImageSize createImageSize(String url, int height, int width) {
    LBImageSize size = new LBImageSize();
    size.url = url;
    size.height = height;
    size.width = width;
    return size;
  }

  @Test
  void getTallest_whenImageIsNull_returnsEmptyString() {
    String tallestUrl = ImageUtils.getTallest(null);
    assertEquals("", tallestUrl, "Should return an empty string for a null LBImage.");
  }

  @Test
  void getTallest_whenImageSizesIsNull_thenThrowsNullPointerException() {
    LBImage image = new LBImage();
    image.sizes = null; // Explicitly set to null

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals(
        "", tallestUrl, "Should return an empty string if the sizes list if image.sizes is null.");
  }

  @Test
  void getTallest_whenImageSizesIsEmpty_returnsEmptyString() {
    LBImage image = new LBImage();
    image.sizes = Collections.emptyList();

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals("", tallestUrl, "Should return an empty string if the sizes list is empty.");
  }

  @Test
  void getTallest_whenSingleImageSize_returnsItsUrl() {
    LBImage image = new LBImage();
    image.sizes =
        Collections.singletonList(createImageSize("http://example.com/image1.jpg", 100, 50));

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals(
        "http://example.com/image1.jpg", tallestUrl, "Should return the URL of the single image.");
  }

  @Test
  void getTallest_whenMultipleImageSizes_tallestIsFirst() {
    LBImage image = new LBImage();
    image.sizes =
        Arrays.asList(
            createImageSize("http://example.com/tallest.jpg", 200, 100),
            createImageSize("http://example.com/shorter.jpg", 100, 50));

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals(
        "http://example.com/tallest.jpg",
        tallestUrl,
        "Should return the URL of the tallest image when it's first.");
  }

  @Test
  void getTallest_whenMultipleImageSizes_tallestIsInMiddle() {
    LBImage image = new LBImage();
    image.sizes =
        Arrays.asList(
            createImageSize("http://example.com/shorter1.jpg", 100, 50),
            createImageSize("http://example.com/tallest.jpg", 300, 150),
            createImageSize("http://example.com/shorter2.jpg", 200, 100));

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals(
        "http://example.com/tallest.jpg",
        tallestUrl,
        "Should return the URL of the tallest image when it's in the middle.");
  }

  @Test
  void getTallest_whenMultipleImageSizes_tallestIsLast() {
    LBImage image = new LBImage();
    image.sizes =
        Arrays.asList(
            createImageSize("http://example.com/shorter1.jpg", 100, 50),
            createImageSize("http://example.com/shorter2.jpg", 200, 100),
            createImageSize("http://example.com/tallest.jpg", 300, 150));

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals(
        "http://example.com/tallest.jpg",
        tallestUrl,
        "Should return the URL of the tallest image when it's last.");
  }

  @Test
  void getTallest_whenMultipleImageSizes_allSameMaxHeight_returnsFirstEncounteredWithMaxHeight() {
    LBImage image = new LBImage();
    // The reduce logic (next.height > result.height) means the first one establishing the max
    // height is kept.
    image.sizes =
        Arrays.asList(
            createImageSize("http://example.com/first_tall.jpg", 200, 100),
            createImageSize("http://example.com/second_tall.jpg", 200, 110),
            createImageSize("http://example.com/shorter.jpg", 100, 50));

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals(
        "http://example.com/first_tall.jpg",
        tallestUrl,
        "Should return the URL of the first image with the max height.");
  }

  @Test
  void
      getTallest_whenMultipleImageSizes_differentOrderSameMaxHeight_returnsFirstEncounteredWithMaxHeight() {
    LBImage image = new LBImage();
    image.sizes =
        Arrays.asList(
            createImageSize("http://example.com/shorter.jpg", 100, 50),
            createImageSize("http://example.com/first_max_height.jpg", 200, 100),
            createImageSize("http://example.com/second_max_height.jpg", 200, 120));

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals(
        "http://example.com/first_max_height.jpg",
        tallestUrl,
        "Should return the URL of the first image that achieves the max height in stream order.");
  }

  @Test
  void getTallest_whenAllImageSizesHaveZeroHeight_returnsEmptyString() {
    LBImage image = new LBImage();
    image.sizes =
        Arrays.asList(
            createImageSize("http://example.com/zero1.jpg", 0, 50),
            createImageSize("http://example.com/zero2.jpg", 0, 60));

    String tallestUrl = ImageUtils.getTallest(image);
    // The initial 'emptyImage' in ImageUtils has height 0 and url ""
    // Since no image is taller, the initial emptyImage.url is returned.
    assertEquals("", tallestUrl, "Should return an empty string if all images have zero height.");
  }

  @Test
  void getTallest_whenSomeImagesHaveZeroHeightAndOneIsTaller() {
    LBImage image = new LBImage();
    image.sizes =
        Arrays.asList(
            createImageSize("http://example.com/zero1.jpg", 0, 50),
            createImageSize("http://example.com/tallest.jpg", 100, 70),
            createImageSize("http://example.com/zero2.jpg", 0, 60));

    String tallestUrl = ImageUtils.getTallest(image);
    assertEquals(
        "http://example.com/tallest.jpg",
        tallestUrl,
        "Should correctly find the tallest image even with zero-height images present.");
  }
}
