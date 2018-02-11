package com.neopetsconnect.bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.filemanager.core.FileManager;
import org.filemanager.core.SimpleFile;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.Utils;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class LogCommand implements CommandExecutor, ConfigProperties {

  @Command(aliases = {"!log"}, description = "Display all the logs.", usage = "!log [list|<type>]")
  public String onCommand(String command, String[] args) {
    if (args.length < 1) {
      return "Requires at least one parameter.";
    }
    if (args[0].equals("list")) {
      try {
        return Files.list(Paths.get(".")).filter(path -> path.toString().endsWith(".log"))
            .map(path -> {
              return path.getFileName().toString().split("\\.")[0];
            }).reduce((x, y) -> x + ", " + y).orElse("No log file found.");
      } catch (IOException e) {
        throw new IllegalStateException("Current directory should be found.");
      }
    } else {
      if (!Files.exists(Paths.get(args[0] + ".log"))) {
        return "Unknown argument: " + args[0];
      } else {
        SimpleFile logFile = null;
        try {
          logFile = FileManager.open(args[0] + ".log");
          if (logFile.isEmpty()) {
            return args[0] + ".log is empty.";
          }
          try {
            String content;
            if (args.length >= 2) {
              switch (args[1]) {
                case "between":
                  if (args.length < 4) {
                    return "Requires at least 2 number.";
                  }
                  int start = Integer.parseInt(args[2]);
                  int end;
                  if (args[3].equals("and") && args.length >= 5) {
                    end = Integer.parseInt(args[4]);
                  } else {
                    end = Integer.parseInt(args[3]);
                  }
                  content =
                      Utils.maxLength(logFile.readBetweenLines(start, end), DISCORD_MAX_LENGTH);
                  break;
                case "first":
                  if (args.length < 3) {
                    return "Requires at least 1 number.";
                  }
                  int n = Integer.parseInt(args[2]);
                  content = Utils.maxLength(logFile.readFirstLines(n), DISCORD_MAX_LENGTH);
                  break;
                case "last":
                  if (args.length < 3) {
                    return "Requires at least 1 number.";
                  }
                  if (args.length > 3) {
                    int startEnd = Integer.parseInt(args[2]);
                    int endEnd = Integer.parseInt(args[3]);
                    content = logFile.readBetweenLinesFromEnd(startEnd, endEnd);
                  } else {
                    int m = Integer.parseInt(args[2]);
                    content = logFile.readLastLines(m);
                  }
                  break;
                case "clear":
                  FileManager.override(logFile);
                  return args[0] + ".log cleared.";
                default:
                  return "Unknown argument: " + args[1];
              }
            } else {
              content = logFile.readAll();
            }
            System.out.println(content.length());
            System.out.println(Utils.maxLengthReversed(content, DISCORD_MAX_LENGTH).length());
            return Utils.maxLengthReversed(content, DISCORD_MAX_LENGTH);
          } catch (NumberFormatException e) {
            return "Expected a number.";
          }
        } finally {
          if (logFile != null) {
            logFile.close();
          }
        }
      }
    }
  }

}
