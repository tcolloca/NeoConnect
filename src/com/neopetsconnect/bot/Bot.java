package com.neopetsconnect.bot;

import com.google.common.util.concurrent.FutureCallback;
import com.logger.main.LogListener;
import com.neopetsconnect.main.Status;
import com.neopetsconnect.utils.ConfigProperties;
import com.neopetsconnect.utils.Logger;
import com.neopetsconnect.utils.Utils;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;

public class Bot implements ConfigProperties, LogListener {

	private final DiscordAPI api;
	
	public Bot() {
        this.api = Javacord.getApi(TOKEN, true);
        CommandHandler cmdHandler = new JavacordHandler(api);
        cmdHandler.registerCommand(new LogCommand());
        cmdHandler.registerCommand(new StatusCommand());
		Logger.out.addListener(EXCEPTION, this);
    }
	
	/**
	 * Blocking function.
	 */
	private void connect() {
		api.connect(new FutureCallback<DiscordAPI>() {
			@Override
			public void onFailure(Throwable api) {
			}

			@Override
			public void onSuccess(DiscordAPI api) {
				Logger.out.log(BOT, "Connected :)");
				api.getChannels().stream().forEach(channel -> channel.sendMessage("Hey! :)"));
			}
		});
	}
	
	public void run() {
		connect();
		while (!ConfigProperties.getStatus().equals(Status.OFF)) {
			Utils.sleep(0.01);
		}
		Utils.sleep(0.1);
		System.exit(0);
	}
	
	public static void main(String[] args) {
		ConfigProperties.updateStatus(Status.ON);
		new Bot().run();
	}

	@Override
	public void onLog(String category, String logMessage) {
		api.getChannels().stream().forEach(channel -> channel.sendMessage("There was an exception!"));
		api.getChannels().stream().forEach(channel -> 
			channel.sendMessage(Utils.maxLength(logMessage, DISCORD_MAX_LENGTH)));
	}
}
