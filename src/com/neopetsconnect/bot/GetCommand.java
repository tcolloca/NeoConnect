package com.neopetsconnect.bot;

import com.neopetsconnect.utils.ConfigProperties;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class GetCommand implements CommandExecutor {

  @Command(aliases = {"!get"}, description = "Gets a property value.", 
      usage = "!get <type> <category> <subcategory>")
  public String onCommand(String command, String[] args) {
    if (args.length < 3) {
      return "Requires at least 3 parameters.";
    }
    String category = args[1].toUpperCase();
    String subcategory = args[2];
    if (!ConfigProperties.contains(category)) {
      return "Unknown category: " + category;
    }
    if (!ConfigProperties.contains(category, subcategory)) {
      return "Unknown subcategory: " + subcategory;
    }
    switch (args[0]) {
      case "integer":
      case "int":
        return String.valueOf(ConfigProperties.get(category, subcategory, Integer.class));
      case "double":
      case "float":
        return String.valueOf(ConfigProperties.get(category, subcategory, Double.class));
      case "bool":
      case "boolean":
        return String.valueOf(ConfigProperties.get(category, subcategory, Boolean.class));
      case "string":
      case "str":
        return ConfigProperties.get(category, subcategory, String.class);
      default:
        return "Unknown type: " + args[0];
    }
  }
}
