package com.neopetsconnect.bot;

import com.neopetsconnect.utils.ConfigProperties;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class SetCommand implements CommandExecutor {

  @Command(aliases = {"!set"}, description = "Sets a property value.", 
      usage = "!set <type> <category> <subcategory> <value>")
  public String onCommand(String command, String[] args) {
    if (args.length < 4) {
      return "Requires at least 4 parameters.";
    }
    String category = args[1].toUpperCase();
    String subcategory = args[2];
    String value = args[3];
    if (!ConfigProperties.contains(category)) {
      return "Unknown category: " + category;
    }
    if (!ConfigProperties.contains(category, subcategory)) {
      return "Unknown subcategory: " + subcategory;
    }
    try {      
      switch (args[0]) {
        case "integer":
        case "int":
          ConfigProperties.set(category, subcategory, Integer.class, Integer.parseInt(value));
          break;
        case "double":
        case "float":
          ConfigProperties.set(category, subcategory, Double.class, Double.parseDouble(value));
          break;
        case "bool":
        case "boolean":
          ConfigProperties.set(category, subcategory, Boolean.class, Boolean.parseBoolean(value));
          break;
        case "string":
        case "str":
          ConfigProperties.set(category, subcategory, String.class, value);
          break;
        default:
          return "Unknown type: " + args[0];
      }
    } catch (ClassCastException e) {
      return "Invalid type for value : "+ value + ". Expecting: " + args[0];
    }
    return "Value set.";
  }
}
