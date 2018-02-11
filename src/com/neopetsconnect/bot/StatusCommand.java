package com.neopetsconnect.bot;

import com.neopetsconnect.main.Status;
import com.neopetsconnect.utils.ConfigProperties;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class StatusCommand implements CommandExecutor {

  @Command(aliases = {"!status"}, description = "Retrieves or updates the status of the program.",
      usage = "!status [(play|on)|(stop|pause)|(kill|off)]")

  public String onCommand(String command, String[] args) {
    if (args.length == 0) {
      return ConfigProperties.getStatus().name();
    }
    if (args[0].equals("play") || args[0].equals("on")) {
      ConfigProperties.updateStatus(Status.ON);
      return "Status changed to on.";
    } else if (args[0].equals("pause")) {
      ConfigProperties.updateStatus(Status.PAUSED);
      return "Status changed to paused.";
    } else if (args[0].equals("kill") || args[0].equals("off")) {
      ConfigProperties.updateStatus(Status.OFF);
      return "Status changed to off. Bye!";
    } else {
      return "Unknown status change action: " + args[0];
    }
  }
}
