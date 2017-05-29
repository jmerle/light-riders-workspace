package io.riddles.gamewrapper.io;

import io.riddles.gamewrapper.GameWrapper;

import java.io.IOException;
import java.util.ArrayList;

public class IOPlayer extends IOWrapper {
    private int id;
    private long timebank;
    private long timebankMax;
    private long timePerMove;
    private int maxTimeouts;
    private StringBuilder dump;
    private int errorCounter;
    private ArrayList<Long> responseTimes;
    private final String NULL_MOVE1 = "no_moves";
    private final String NULL_MOVE2 = "pass";

    public IOPlayer(Process process, int id, long timebankMax, long timePerMove, int maxTimeouts, boolean stderrEnabled) {
        super(process, stderrEnabled);
        this.id = id;
        this.timebank = timebankMax;
        this.timebankMax = timebankMax;
        this.timePerMove = timePerMove;
        this.maxTimeouts = maxTimeouts;
        this.dump = new StringBuilder();
        this.errorCounter = 0;
        this.responseTimes = new ArrayList();
    }

    public void send(String line) {
        this.addToDump(line);
        if (!super.write(line) && !this.finished) {
            this.addToDump("Write to bot failed, shutting down...");
        }

    }

    public String ask(String line) throws IOException {
        this.response = null;
        this.send(String.format("%s %d", new Object[]{line, Long.valueOf(this.timebank)}));
        return this.getResponse();
    }

    public String getResponse() {
        if (this.errorCounter > this.maxTimeouts) {
            this.addToDump(String.format("Maximum number (%d) of time-outs reached: skipping all moves.", new Object[]{Integer.valueOf(this.maxTimeouts)}));
            return "null";
        } else {
            long startTime = System.currentTimeMillis();
            String response = super.getResponse(this.timebank);
            long timeElapsed = System.currentTimeMillis() - startTime;
            this.responseTimes.add(Long.valueOf(timeElapsed));
            this.updateTimeBank(timeElapsed);
            if (response.equalsIgnoreCase("no_moves")) {
                this.botDump("no_moves");
                return "pass";
            } else if (response.equalsIgnoreCase("pass")) {
                this.botDump("pass");
                return "pass";
            } else if (response.isEmpty()) {
                this.botDump("");
                return "null";
            } else {
                this.botDump(response);
                return response;
            }
        }
    }

    protected String handleResponseTimeout(long timeout) {
        this.addToDump(String.format("Response timed out (%dms), let your bot return '%s' instead of nothing or make it faster.", new Object[]{Long.valueOf(timeout), "no_moves"}));
        this.addError();
        return "";
    }

    private void addError() {
        this.errored = true;
        ++this.errorCounter;
        if (this.errorCounter > this.maxTimeouts) {
            this.finish();
        }

    }

    public int finish() {
        int exitStatus = super.finish();
        if (!GameWrapper.PROPAGATE_BOT_EXIT_CODE) {
            exitStatus = 0;
        }

        System.out.println("Bot shut down.");
        return exitStatus;
    }

    private void updateTimeBank(long timeElapsed) {
        this.timebank = Math.max(this.timebank - timeElapsed, 0L);
        this.timebank = Math.min(this.timebank + this.timePerMove, this.timebankMax);
    }

    private void botDump(String dumpy) {
        String engineSays = "Output from your bot: \"%s\"";
        this.addToDump(String.format(engineSays, new Object[]{dumpy}));
    }

    public void addToDump(String dumpy) {
        this.dump.append(dumpy).append("\n");
    }

    public String getDump() {
        return this.dump.toString();
    }

    public int getId() {
        return this.id;
    }

    public long getTimebankMax() {
        return this.timebankMax;
    }

    public long getTimePerMove() {
        return this.timePerMove;
    }

    public ArrayList<Long> getResponseTimes() {
        return this.responseTimes;
    }
}
