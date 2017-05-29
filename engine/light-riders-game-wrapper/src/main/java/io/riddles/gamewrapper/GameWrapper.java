package io.riddles.gamewrapper;

import io.riddles.gamewrapper.runner.MatchRunner;
import io.riddles.gamewrapper.runner.Reportable;
import io.riddles.gamewrapper.runner.Runnable;
import io.riddles.gamewrapper.runner.ScenarioRunner;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class GameWrapper implements Runnable {
    public static boolean DEBUG = false;
    public static boolean PROPAGATE_BOT_EXIT_CODE = false;
    private long timebankMax = 10000L;
    private long timePerMove = 500L;
    private int maxTimeouts = 2;
    private String resultFilePath;
    private Runnable runner;

    public GameWrapper() {
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        GameWrapper game = new GameWrapper();

        try {
            JSONObject config = new JSONObject(args[0]);
            game.prepare(config);
        } catch (Exception var8) {
            throw new RuntimeException("Failed to parse settings." + var8.getMessage());
        }

        System.out.println("Starting...");
        game.run();
        long timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Stopping...");
        int exitStatus = game.postrun(timeElapsed);
        System.out.println("Done.");
        System.exit(exitStatus);
    }

    public void prepare(JSONObject config) throws IOException {
        this.parseSettings(config);
        JSONObject runnerConfig;
        if (config.has("match")) {
            runnerConfig = config.getJSONObject("match");
            this.runner = new MatchRunner(Long.valueOf(this.timebankMax), Long.valueOf(this.timePerMove), this.maxTimeouts);
        } else {
            if (!config.has("scenario")) {
                throw new RuntimeException("Config does not contain either match or scenario");
            }

            runnerConfig = config.getJSONObject("scenario");
            this.runner = new ScenarioRunner(Long.valueOf(this.timebankMax), Long.valueOf(this.timePerMove), this.maxTimeouts);
        }

        this.runner.prepare(runnerConfig);
    }

    private void parseSettings(JSONObject config) {
        JSONObject wrapperConfig = config.getJSONObject("wrapper");
        if (wrapperConfig.has("timebankMax")) {
            this.timebankMax = wrapperConfig.getLong("timebankMax");
        }

        if (wrapperConfig.has("timePerMove")) {
            this.timePerMove = wrapperConfig.getLong("timePerMove");
        }

        if (wrapperConfig.has("maxTimeouts")) {
            this.maxTimeouts = wrapperConfig.getInt("maxTimeouts");
        }

        if (wrapperConfig.has("debug")) {
            DEBUG = wrapperConfig.getBoolean("debug");
        }

        if (wrapperConfig.has("propagateBotExitCode")) {
            PROPAGATE_BOT_EXIT_CODE = wrapperConfig.getBoolean("propagateBotExitCode");
        }

        this.resultFilePath = wrapperConfig.getString("resultFile");
    }

    public void run() throws IOException {
        this.runner.run();
    }

    public int postrun(long responseTime) throws IOException {
        int exitStatus = this.runner.postrun(responseTime);
        JSONObject resultSet = ((Reportable) this.runner).getResults();
        System.out.println("Saving game...");
        this.saveGame(resultSet);
        return exitStatus;
    }

    private void saveGame(JSONObject result) throws IOException {
        System.out.println("Writing to result.json");
        FileWriter writer = new FileWriter(this.resultFilePath);
        writer.write(result.toString());
        writer.close();
        System.out.println("Finished writing to result.json");
    }
}
