package com.neopetsconnect.bot;

import com.google.common.util.concurrent.FutureCallback;
import com.logger.main.LogListener;
import com.neopetsconnect.main.Status;
import com.neopetsconnect.utils.Categories;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;

public class Bot implements LogListener, Categories {

  private final DiscordAPI api;

  public Bot() {
    this.api = Javacord.getApi(ConfigProperties.TOKEN, true);
    CommandHandler cmdHandler = new JavacordHandler(api);
    cmdHandler.registerCommand(new LogCommand());
    cmdHandler.registerCommand(new StatusCommand());
    cmdHandler.registerCommand(new CategoryCommand());
    cmdHandler.registerCommand(new GetCommand());
    cmdHandler.registerCommand(new SetCommand());
    cmdHandler.registerCommand(new EnableCommand());
    cmdHandler.registerCommand(new DisableCommand());
    cmdHandler.registerCommand(new HelpCommand(cmdHandler));
    Logger.out.addListener(EXCEPTION, this);
  }

  /**
   * Non-blocking function.
   */
  private void connect() {
    api.connect(new FutureCallback<DiscordAPI>() {
      @Override
      public void onFailure(Throwable api) {}

      @Override
      public void onSuccess(DiscordAPI api) {
        Logger.out.log(BOT, "Connected :)");
        send("Hey! :)");
      }
    });
  }
  
  public void send(String message) {
    api.getChannels().stream().forEach(channel -> channel.sendMessage(message));
  }
  
  public void disconnect() {
    api.disconnect();
  }

  /**
   * non-blocking.
   */
  public void run() {
    connect();
  }

  public static void main(String[] args) {
    ConfigProperties.updateStatus(Status.ON);
    new Bot().run();
  }

  @Override
  public void onLog(String category, String logMessage) {
    api.getChannels().stream().forEach(channel -> channel.sendMessage("There was an exception!"));
    api.getChannels().stream()
        .forEach(channel -> channel.sendMessage(Utils.maxLength(logMessage, 
            ConfigProperties.DISCORD_MAX_LENGTH)));
  }
}
