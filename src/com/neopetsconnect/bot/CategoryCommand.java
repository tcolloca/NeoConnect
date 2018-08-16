package com.neopetsconnect.bot;

import java.util.List;
import java.util.stream.Collectors;
import com.neopetsconnect.utils.ConfigProperties;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class CategoryCommand implements CommandExecutor {

  @Command(aliases = {"!category"}, description = "Lists categories and subcategories.", 
      usage = "!category list [category]")
  public String onCommand(String command, String[] args) {
    if (args.length < 1) {
      return "Requires at least one parameter.";
    }
    if (args[0].equals("list")) {
      if (args.length == 1) {
        return String.join("\n", toLowerCase(ConfigProperties.listCategories()));
      } else {
        if (!ConfigProperties.contains(args[1].toUpperCase())) {
          return "Unknown category: " + args[1];
        }
        return String.join("\n", toLowerCase(ConfigProperties.listSubCategories(
            args[1].toUpperCase())));
      }
    }
    return "Unknown argument: " + args[0];
  }
  
  private List<String> toLowerCase(List<String> strings) {
    return strings.stream().map(str -> str.toLowerCase()).collect(Collectors.toList());
  }
}
