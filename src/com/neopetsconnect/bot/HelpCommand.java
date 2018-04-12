package com.neopetsconnect.bot;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;

public class HelpCommand implements CommandExecutor {

  private final CommandHandler commandHandler;

  public HelpCommand(CommandHandler commandHandler) {
      this.commandHandler = commandHandler;
  }

  @Command(aliases = {"!help", "!commands"}, description = "Shows this page")
  public String onCommand(String command, String[] args) {
      StringBuilder builder = new StringBuilder();
      builder.append("```xml"); // a xml code block looks fancy
      for (CommandHandler.SimpleCommand simpleCommand : commandHandler.getCommands()) {
          boolean show = true;
          if (args.length >= 1) {
            show = false;
            for (String alias: simpleCommand.getCommandAnnotation().aliases()) {
              if (("!" + args[0]).equals(alias)) {
                show = true;
              }
            }            
          }
          if (!simpleCommand.getCommandAnnotation().showInHelpPage() || !show) {
              continue; // skip command
          }
          builder.append("\n");
          if (!simpleCommand.getCommandAnnotation().requiresMention()) {
              // the default prefix only works if the command does not require a mention
              builder.append(commandHandler.getDefaultPrefix());
          }
          String usage = simpleCommand.getCommandAnnotation().usage();
          if (usage.isEmpty()) { // no usage provided, using the first alias
              usage = simpleCommand.getCommandAnnotation().aliases()[0];
          }
          builder.append(usage);
          String description = simpleCommand.getCommandAnnotation().description();
          if (!description.equals("none")) {
              builder.append(" | ").append(description);
          }
      }
      builder.append("\n```"); // end of xml code block
      return builder.toString();
  }
}
