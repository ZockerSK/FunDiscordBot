package de.devgruppe.fundiscordbot;

import de.devgruppe.fundiscordbot.command.CommandRegistry;
import de.devgruppe.fundiscordbot.command.commands.CommandListCommand;
import de.devgruppe.fundiscordbot.command.commands.EchoCommand;
import de.devgruppe.fundiscordbot.command.impl.DefaultCommandRegistry;
import de.devgruppe.fundiscordbot.config.Config;
import de.devgruppe.fundiscordbot.config.Configuration;
import lombok.Getter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import org.apache.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Scanner;

public class FunDiscordBotStarter implements EventListener {

  @Getter
  private static FunDiscordBotStarter instance;
  @Getter
  private JDA jda;
  @Getter
  private Configuration configuration;
  @Getter
  private Config config;
  @Getter
  private CommandRegistry commandRegistry;
  private static Logger logger;

  private FunDiscordBotStarter() {
    configuration = new Configuration();
    if (!configuration.exists()) {
      Config config = new Config();
      configuration.writeConfiguration(config);
      logger.info("Config created! Program stopped!");
      System.exit(0);
    }
    configuration.readConfiguration();
    config = configuration.getConfig();
    this.commandRegistry = new DefaultCommandRegistry();
    logger.info("Connecting...");
    try {
      jda = new JDABuilder(AccountType.BOT)
          .setToken(this.config.getBotToken())
          .setAutoReconnect(true)
          .addEventListener(this)
          .setGame(Game.of("https://github.com/Dev-Gruppe/FunDiscordBot", "https://github.com/Dev-Gruppe/FunDiscordBot"))
          .buildAsync();
    } catch (LoginException | RateLimitedException e) {
      e.printStackTrace();
    }
    if (jda == null) {
      logger.error("JDA is null");
      System.exit(0);
    }
    this.jda.addEventListener(this.commandRegistry);
    this.registerCommands();
  }

  public static void main(String[] args) {
    logger = Logger.getLogger("FunDiscordBot");
    instance = new FunDiscordBotStarter();
  }

  private void registerCommands(){
    this.commandRegistry.registerCommand(new CommandListCommand());
    this.commandRegistry.registerCommand(new EchoCommand());
  }

  @Override
  public void onEvent(Event event) {
    if (event instanceof ReadyEvent) {
      logger.info("Connected");
      new Thread(() -> {
        Scanner scanner = new Scanner(System.in);
        String line;
        while ((line = scanner.nextLine()) != null) {
          if (line.equals("stop")) {
            stopBot(event);
          }
        }
      }).start();
    }
  }

  private void stopBot(Event event) {
    event.getJDA().shutdown();
    System.exit(0);
  }

}
