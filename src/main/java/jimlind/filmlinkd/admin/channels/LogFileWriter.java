package jimlind.filmlinkd.admin.channels;

import java.io.FileWriter;
import java.io.IOException;

/** Used to handle log file writing. */
public class LogFileWriter {
  /**
   * Write the content to a file using tag and timestamp to build the filename.
   *
   * @param content Content to write to file
   * @param tag Specific tag to include in file name
   * @param timestamp Timestamp ot include in file name
   */
  public void write(String content, String tag, String timestamp) {
    String filename = tag + "-" + timestamp + ".txt";
    try (FileWriter writer = new FileWriter(filename, true)) {
      writer.write(content + System.lineSeparator());
    } catch (IOException ignore) {
      // Do nothing
    }
  }
}
