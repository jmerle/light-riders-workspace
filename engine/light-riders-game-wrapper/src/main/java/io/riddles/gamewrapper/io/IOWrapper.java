package io.riddles.gamewrapper.io;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IOWrapper implements Runnable {
    private Process process;
    private OutputStreamWriter inputStream;
    private InputStreamGobbler outputGobbler;
    private InputStreamGobbler errorGobbler;
    protected boolean finished;
    protected boolean errored;
    public String response;
    public ConcurrentLinkedQueue<String> inputQueue;

    public IOWrapper(Process process) {
        this(process, false);
    }

    public IOWrapper(Process process, boolean stderrEnabled) {
        this.inputStream = new OutputStreamWriter(process.getOutputStream());
        this.outputGobbler = new InputStreamGobbler(process.getInputStream(), this, "output", stderrEnabled);
        this.errorGobbler = new InputStreamGobbler(process.getErrorStream(), this, "error", stderrEnabled);
        this.process = process;
        this.errored = false;
        this.finished = false;
    }

    public boolean write(String line) {
        if (this.finished) {
            return false;
        } else {
            try {
                this.inputStream.write(line + "\n");
                this.inputStream.flush();
                return true;
            } catch (IOException var3) {
                System.err.println("Writing to inputstream failed.");
                this.finish();
                return false;
            }
        }
    }

    public String ask(String line, long timeout) throws IOException {
        this.response = null;
        return this.write(line) ? this.getResponse(timeout) : "";
    }

    public String getResponse(long timeout) {
        long timeStart = System.currentTimeMillis();

        while (this.response == null) {
            long timeNow = System.currentTimeMillis();
            long timeElapsed = timeNow - timeStart;
            if (timeElapsed >= timeout) {
                return this.handleResponseTimeout(timeout);
            }

            try {
                Thread.sleep(2L);
            } catch (InterruptedException var11) {
            }
        }

        if (this.inputQueue != null) {
            this.inputQueue.remove(this.response);
        }

        String response = this.response;
        this.response = null;
        return response;
    }

    protected String handleResponseTimeout(long timeout) {
        return "";
    }

    public int finish() {
        if (this.finished) {
            return this.errored ? 1 : 0;
        } else {
            try {
                this.inputStream.close();
            } catch (IOException var3) {
            }

            this.outputGobbler.finish();
            this.errorGobbler.finish();
            this.process.destroy();

            try {
                this.process.waitFor();
            } catch (InterruptedException var2) {
            }

            this.finished = true;
            return this.errored ? 1 : 0;
        }
    }

    public Process getProcess() {
        return this.process;
    }

    public String getStdout() {
        return this.outputGobbler.getData();
    }

    public String getStderr() {
        return this.errorGobbler.getData();
    }

    public void run() {
        this.outputGobbler.start();
        this.errorGobbler.start();
    }
}
