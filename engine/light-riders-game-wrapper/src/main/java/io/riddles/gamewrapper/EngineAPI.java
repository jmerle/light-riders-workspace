package io.riddles.gamewrapper;

import io.riddles.gamewrapper.io.IOEngine;
import io.riddles.gamewrapper.io.IOPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EngineAPI {
    private static final Pattern BOTNR_ASK = Pattern.compile("^bot (\\d+) ask (.*)");
    private static final Pattern BOTNR_SEND = Pattern.compile("^bot (\\d+) send (.*)");
    private static final Pattern BOTNR_WARNING = Pattern.compile("^bot (\\d+) warning (.*)");
    private static final Pattern BOTALL_SEND = Pattern.compile("^bot all send (.*)");
    private IOEngine engine;
    private ArrayList<IOPlayer> bots;
    private boolean ended;

    public EngineAPI(IOEngine engine, ArrayList<IOPlayer> bots) {
        this.engine = engine;
        this.bots = bots;
        this.ended = false;
    }

    public void handle(String message) throws IOException {
        if (message != null && message.length() > 0 && !message.equals("end")) {
            Matcher m;
            if ((m = BOTNR_ASK.matcher(message)).find()) {
                this.engine.send(this.botAsk(Integer.parseInt(m.group(1)), m.group(2)));
            } else if ((m = BOTNR_SEND.matcher(message)).find()) {
                this.botSend(Integer.parseInt(m.group(1)), m.group(2));
            } else if ((m = BOTNR_WARNING.matcher(message)).find()) {
                this.botWarning(Integer.parseInt(m.group(1)), m.group(2));
            } else if ((m = BOTALL_SEND.matcher(message)).find()) {
                this.botBroadcast(m.group(1));
            } else if (!message.equals("ok")) {
                System.err.println(String.format("'%s' did not match any action", new Object[]{message}));
                this.ended = true;
            }

        } else {
            this.ended = true;
        }
    }

    public void run() throws IOException {
        if (this.askAndExpect("initialize", "ok")) {
            System.out.println("Engine initialized. Sending settings to engine..");
            this.engine.sendPlayers(this.bots);
            this.engine.sendConfiguration();
            System.out.println("Settings sent to engine. Sending settings to bots...");
            this.sendBotSettings();
            System.out.println("Settings sent to bots. Starting engine...");
            this.engine.send("start");
            System.out.println("Engine Started. Playing game...");

            while (!this.ended) {
                this.handle(this.engine.getMessage());
            }

        }
    }

    private void sendBotSettings() {
        String playerNames = "";
        String connector = "";

        Iterator var3;
        IOPlayer bot;
        for (var3 = this.bots.iterator(); var3.hasNext(); connector = ",") {
            bot = (IOPlayer) var3.next();
            playerNames = playerNames + String.format("%splayer%d", new Object[]{connector, Integer.valueOf(bot.getId())});
        }

        this.botBroadcast(String.format("settings player_names %s", new Object[]{playerNames}));
        var3 = this.bots.iterator();

        while (var3.hasNext()) {
            bot = (IOPlayer) var3.next();
            bot.send(String.format("settings your_bot player%d", new Object[]{Integer.valueOf(bot.getId())}));
            bot.send(String.format("settings timebank %d", new Object[]{Long.valueOf(bot.getTimebankMax())}));
            bot.send(String.format("settings time_per_move %d", new Object[]{Long.valueOf(bot.getTimePerMove())}));
        }

    }

    public String askGameDetails() {
        try {
            return this.engine.ask("details");
        } catch (IOException var2) {
            System.err.println(var2);
            return "";
        }
    }

    public String askPlayedGame() {
        try {
            return this.engine.ask("game");
        } catch (IOException var2) {
            System.err.println(var2);
            return "";
        }
    }

    private String botAsk(int botIndex, String message) throws IOException {
        IOPlayer bot = (IOPlayer) this.bots.get(botIndex);
        return String.format("bot %d %s", new Object[]{Integer.valueOf(botIndex), bot.ask(message)});
    }

    private void botSend(int botIndex, String message) throws IOException {
        IOPlayer bot = (IOPlayer) this.bots.get(botIndex);
        bot.send(message);
    }

    private void botWarning(int botIndex, String warning) {
        IOPlayer bot = (IOPlayer) this.bots.get(botIndex);
        bot.addToDump(warning);
    }

    private void botBroadcast(String message) {
        Iterator var2 = this.bots.iterator();

        while (var2.hasNext()) {
            IOPlayer bot = (IOPlayer) var2.next();
            bot.send(message);
        }

    }

    private boolean askAndExpect(String message, String expected) throws IOException {
        String response = this.engine.ask(message);
        if (!response.equals(expected)) {
            System.err.println(String.format("Unexpected response: %s\n to message: %s", new Object[]{response, message}));
            return false;
        } else {
            return true;
        }
    }
}
