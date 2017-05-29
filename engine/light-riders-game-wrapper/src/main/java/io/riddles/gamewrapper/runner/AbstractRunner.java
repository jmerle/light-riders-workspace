package io.riddles.gamewrapper.runner;

import io.riddles.gamewrapper.io.IOEngine;
import io.riddles.gamewrapper.io.IOPlayer;

import java.io.IOException;

import org.json.JSONObject;

public class AbstractRunner implements Reportable {
    private Long timebankMax;
    private Long timePerMove;
    private int maxTimeouts;
    private JSONObject results;

    public AbstractRunner(Long timebankMax, Long timePerMove, int maxTimeouts) {
        this.timebankMax = timebankMax;
        this.timePerMove = timePerMove;
        this.maxTimeouts = maxTimeouts;
        this.results = new JSONObject();
    }

    protected IOPlayer createPlayer(String command, int id) throws IOException {
        return this.createPlayer(command, id, false);
    }

    protected IOPlayer createPlayer(String command, int id, boolean stderrEnabled) throws IOException {
        IOPlayer player = new IOPlayer(this.wrapCommand(command), id, this.timebankMax.longValue(), this.timePerMove.longValue(), this.maxTimeouts, stderrEnabled);
        player.run();
        return player;
    }

    protected IOEngine createEngine(String command, JSONObject engineConfig) throws IOException {
        IOEngine engine = new IOEngine(this.wrapCommand(command), engineConfig);
        engine.run();
        return engine;
    }

    protected void setResults(JSONObject value) {
        this.results = value;
    }

    private Process wrapCommand(String command) throws IOException {
        System.out.println("executing: " + command);
        return Runtime.getRuntime().exec(command);
    }

    public JSONObject getResults() {
        return this.results;
    }
}
