package com.neopetsconnect.bot;

import com.neopetsconnect.utils.ConfigProperties;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class EnableCommand implements CommandExecutor {
  
  @Command(aliases = {"!enable"}, description = "Enables a daily.", 
      usage = "!enable <daily>")
  public String onCommand(String command, String[] args) {
    if (args.length < 1) {
      return "Requires at least 1 parameter.";
    }
    String daily = args[0].toUpperCase();
    if (!ConfigProperties.contains(daily)) {
      return "Unknown daily: " + daily;
    }
    if (!ConfigProperties.contains(daily, "enabled")) {
      return "Daily is not enablable.";
    }
    ConfigProperties.set(daily, "enabled", Boolean.class, true);
    return "Value set.";
  }
}
