package io.riddles.gamewrapper.runner;

import io.riddles.gamewrapper.EngineAPI;
import io.riddles.gamewrapper.io.IOEngine;
import io.riddles.gamewrapper.io.IOPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MatchRunner extends AbstractRunner implements Runnable, Reportable {
    private EngineAPI api;
    private IOEngine engine = null;
    private ArrayList<IOPlayer> players = new ArrayList();

    public MatchRunner(Long timebankMax, Long timePerMove, int maxTimeouts) {
        super(timebankMax, timePerMove, maxTimeouts);
    }

    public void prepare(JSONObject config) {
        if (!config.has("engine")) {
            throw new RuntimeException("No configuration present for engine");
        } else {
            JSONObject engineConfig = config.getJSONObject("engine");
            this.prepareEngine(engineConfig);
            if (!config.has("bots")) {
                throw new RuntimeException("No bots found in configuration");
            } else {
                JSONArray bots = config.getJSONArray("bots");

                try {
                    bots.forEach((botConfig) -> {
                        this.prepareBot((JSONObject) botConfig);
                    });
                } catch (Exception var5) {
                    var5.printStackTrace();
                    throw new RuntimeException("Failed to start bot.");
                }
            }
        }
    }

    public void run() throws IOException {
        this.api = new EngineAPI(this.engine, this.players);
        this.api.run();
    }

    public int postrun(long timeElapsed) {
        this.setResults(this.createResults(timeElapsed));
        int playerStatusSum = this.players.stream().mapToInt(IOPlayer::finish).sum();
        int engineStatus = this.engine.finish();
        return playerStatusSum + engineStatus > 0 ? 1 : 0;
    }

    private JSONObject createResults(long timeElapsed) {
        JSONObject output = new JSONObject();
        JSONArray players = new JSONArray();
        String details = this.api.askGameDetails();
        String playedGame = this.api.askPlayedGame();
        Iterator var7 = this.players.iterator();

        while (var7.hasNext()) {
            IOPlayer player = (IOPlayer) var7.next();
            String log = player.getDump();
            String errors = player.getStderr();
            JSONArray responseTimes = new JSONArray(player.getResponseTimes());
            long totalResponseTime = ((Long) player.getResponseTimes().stream().reduce(Long.valueOf(0L), (a, b) -> {
                return Long.valueOf(a.longValue() + b.longValue());
            })).longValue();
            JSONObject playerOutput = new JSONObject();
            playerOutput.put("log", log);
            playerOutput.put("errors", errors);
            playerOutput.put("responseTimes", responseTimes);
            playerOutput.put("totalResponseTime", totalResponseTime);
            players.put(playerOutput);
        }

        output.put("timeElapsed", timeElapsed);
        output.put("details", details);
        output.put("game", playedGame);
        output.put("players", players);
        return output;
    }

    private void printGame() {
        System.out.println("Bot data:");
        Iterator var1 = this.players.iterator();

        while (var1.hasNext()) {
            IOPlayer bot = (IOPlayer) var1.next();
            System.out.println(bot.getDump());
            System.out.println(bot.getStdout());
            System.out.println(bot.getStderr());
        }

        System.out.println("Engine data:");
        System.out.println(this.engine.getStdout());
        System.out.println(this.engine.getStderr());
    }

    private void prepareBot(JSONObject config) {
        if (!config.has("command")) {
            throw new RuntimeException("No command specified for engine");
        } else {
            String command = config.getString("command");

            boolean stderrEnabled = config.has("stderrEnabled") && config.getBoolean("stderrEnabled");

            try {
                this.addPlayer(command, stderrEnabled);
            } catch (Exception var4) {
                var4.printStackTrace();
                throw new RuntimeException("Failed to start engine.");
            }
        }
    }

    private void prepareEngine(JSONObject config) {
        if (!config.has("command")) {
            throw new RuntimeException("No command specified for engine");
        } else {
            String command = config.getString("command");

            JSONObject engineConfig;
            try {
                engineConfig = config.getJSONObject("configuration");
            } catch (JSONException var6) {
                engineConfig = new JSONObject();
            }

            try {
                this.setEngine(command, engineConfig);
            } catch (Exception var5) {
                var5.printStackTrace();
                throw new RuntimeException("Failed to start engine.");
            }
        }
    }

    private void addPlayer(String command, boolean stderrEnabled) throws IOException {
        int id = this.players.size();
        this.players.add(this.createPlayer(command, id, stderrEnabled));
    }

    private void setEngine(String command, JSONObject engineConfig) throws IOException {
        this.engine = this.createEngine(command, engineConfig);
    }
}
