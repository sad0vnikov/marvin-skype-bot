package net.sadovnikov.marvinbot.plugins;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.corba.se.impl.activation.CommandHandler;
import net.sadovnikov.marvinbot.core.command.CommandExecutor;
import net.sadovnikov.marvinbot.core.command.annotations.Command;
import net.sadovnikov.marvinbot.core.events.EventHandler;
import net.sadovnikov.marvinbot.core.events.event_types.CommandEvent;
import net.sadovnikov.marvinbot.core.events.event_types.MessageEvent;
import net.sadovnikov.marvinbot.core.message.SentMessage;
import net.sadovnikov.marvinbot.core.message_sender.MessageSender;
import net.sadovnikov.marvinbot.core.plugin.Plugin;
import net.sadovnikov.marvinbot.core.plugin.PluginException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.PluginWrapper;



public class EchoPlugin extends Plugin {

    @Inject protected MessageSender messageSender;
    protected final Logger logger = LogManager.getLogger("core-logger");

    public EchoPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public class MessageHandler extends EventHandler<MessageEvent> {

        public void handle(MessageEvent ev) throws PluginException {

            if (getOption("enableEcho", "off").equals("on")) {
                SentMessage message = new SentMessage();
                message.setText(ev.getMessage().getText());
                message.setRecepientId(ev.getMessage().getChatId());

                messageSender.sendMessage(message);
            }

        }
    }

    @Command("echo")
    @Extension
    public class SwitcherCommand extends CommandExecutor {


        @Override
        public void execute(net.sadovnikov.marvinbot.core.command.Command cmd, MessageEvent ev) throws PluginException {
            String[] args = cmd.getArgs();

            if (args.length == 0) {
                return;
            }

            if ( args[0].equals("on")) {
                setOption("enableEcho", "on");
                messageSender.reply(ev.getMessage(), "Echoing is turned on!");
            } else if ( args[0].equals("off")) {
                setOption("enableEcho", "off");
                messageSender.reply(ev.getMessage(), "Echoing is turned off");
            }
        }
    }

}
