package io.riddles.gamewrapper.runner;

import java.io.IOException;

import org.json.JSONObject;

public interface Runnable {
    void prepare(JSONObject var1) throws IOException;

    void run() throws IOException;

    int postrun(long var1) throws IOException;
}
