package io.riddles.gamewrapper.io;

import io.riddles.gamewrapper.GameWrapper;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;

public class IOEngine extends IOWrapper {
    private final long TIMEOUT = 2000L;
    private JSONObject configuration;

    public IOEngine(Process process, JSONObject configuration) {
        super(process);
        this.configuration = configuration;
        this.inputQueue = new ConcurrentLinkedQueue();
    }

    public boolean send(String message) {
        System.out.println(String.format("Engine in: '%s'", new Object[]{message}));
        return this.write(message);
    }

    public String ask(String message) throws IOException {
        this.getClass();
        return super.ask(message, 2000L);
    }

    public String getResponse() {
        this.getClass();
        return super.getResponse(2000L);
    }

    public String getMessage() {
        long timeStart = System.currentTimeMillis();

        String message;
        for (message = (String) this.inputQueue.poll(); message == null; message = (String) this.inputQueue.poll()) {
            long timeNow = System.currentTimeMillis();
            long timeElapsed = timeNow - timeStart;
            this.getClass();
            if (timeElapsed >= 2000L) {
                this.getClass();
                return this.handleResponseTimeout(2000L);
            }

            try {
                Thread.sleep(2L);
            } catch (InterruptedException var9) {
            }
        }

        this.response = null;
        System.out.println(String.format("Engine out: '%s'", new Object[]{message}));
        return message;
    }

    public int finish() {
        int exitStatus = super.finish();
        System.out.println("Engine shut down.");
        if (GameWrapper.DEBUG) {
            this.printErrors();
        }

        return exitStatus;
    }

    protected String handleResponseTimeout(long timeout) {
        PrintStream var10000 = System.err;
        Object[] var10002 = new Object[1];
        this.getClass();
        var10002[0] = Long.valueOf(2000L);
        var10000.println(String.format("Engine took too long! (%dms)", var10002));
        this.errored = true;
        if (!GameWrapper.DEBUG) {
            this.printErrors();
        }

        return "";
    }

    public boolean sendPlayers(ArrayList<IOPlayer> bots) {
        StringBuilder message = new StringBuilder();
        message.append("bot_ids ");
        String connector = "";

        for (int i = 0; i < bots.size(); ++i) {
            message.append(String.format("%s%d", new Object[]{connector, Integer.valueOf(i)}));
            connector = ",";
        }

        return this.send(message.toString());
    }

    public boolean sendConfiguration() {
        return this.send("configuration " + this.configuration.toString());
    }

    private void printErrors() {
        System.err.println("ENGINE ERROR LOG:\n");
        System.err.println(this.getStderr());
        System.err.println("\nEND ENGINE ERROR LOG");
    }
}
