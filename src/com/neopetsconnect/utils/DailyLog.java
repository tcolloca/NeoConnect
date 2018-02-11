package com.neopetsconnect.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.filemanager.core.Properties;

public class DailyLog implements ConfigProperties {

  public static Properties props;

  static {
    createIfNecessary();
    props = new Properties(getDailyLogFilePath(Utils.neopetsNow().toLocalDate()).toString());
  }

  private DailyLog() {}

  public static void createIfNecessary() {
    Path dailiesPath = Paths.get(DAILIES_LOG_PATH);
    Path dailyLogPath = getDailyLogFilePath(Utils.neopetsNow().toLocalDate());
    try {
      if (Files.notExists(dailiesPath)) {
        Files.createDirectory(dailiesPath);
      }
      Optional<Path> todaysFile =
          Files.list(dailiesPath).filter(path -> path.equals(dailyLogPath)).findFirst();
      if (!todaysFile.isPresent()) {
        Files.createFile(dailyLogPath);
      }
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create dailies folder: " + dailyLogPath.toString(), e);
    }
  }

  private static Path getDailyLogFilePath(LocalDate date) {
    Path dailiesPath = Paths.get(DAILIES_LOG_PATH);
    return dailiesPath.resolve(
        Paths.get(date.format(DateTimeFormatter.ofPattern(DAILIES_LOG_FILE_FORMAT)) + ".txt"));
  }
}
