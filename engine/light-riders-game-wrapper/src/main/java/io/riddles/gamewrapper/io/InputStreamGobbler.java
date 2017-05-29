package io.riddles.gamewrapper.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamGobbler extends Thread {
    private InputStream inputStream;
    private IOWrapper wrapper;
    private String type;
    private StringBuffer buffer;
    private boolean finished;
    private boolean stderrEnabled;

    InputStreamGobbler(InputStream inputStream, IOWrapper wrapper, String type, boolean stderrEnabled) {
        this.inputStream = inputStream;
        this.wrapper = wrapper;
        this.type = type;
        this.buffer = new StringBuffer();
        this.finished = false;
        this.stderrEnabled = stderrEnabled;
    }

    public void run() {
        this.run(false);
    }

    public void run(boolean stderrEnabled) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(this.inputStream);

            String lastLine;
            BufferedReader bufferedReader;
            for (bufferedReader = new BufferedReader(inputStreamReader); !this.finished && (lastLine = bufferedReader.readLine()) != null && this.buffer.length() <= 1000000; this.buffer.append(lastLine).append("\n")) {
                if (this.type.equals("output")) {
                    this.wrapper.response = lastLine;
                    if (this.wrapper.inputQueue != null) {
                        this.wrapper.inputQueue.add(lastLine);
                    }
                } else if (this.type.equals("error") && this.stderrEnabled) {
                    System.err.println(lastLine);
                }
            }

            try {
                bufferedReader.close();
            } catch (IOException var5) {
            }
        } catch (IOException var6) {
            System.err.println(String.format("Readline failed: %s, type: %s", new Object[]{var6, this.type}));
        }

    }

    public String getData() {
        return this.buffer.toString();
    }

    public void finish() {
        this.finished = true;
    }
}
