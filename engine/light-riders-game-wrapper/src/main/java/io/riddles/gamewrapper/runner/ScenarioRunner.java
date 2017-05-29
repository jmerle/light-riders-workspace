package io.riddles.gamewrapper.runner;

import io.riddles.gamewrapper.io.IOPlayer;
import io.riddles.gamewrapper.io.IOWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class ScenarioRunner extends AbstractRunner implements Runnable, Reportable {
    private IOWrapper subject;
    private String subjectType;
    private JSONArray scenario;
    private Long timeout = Long.valueOf(2000L);
    private boolean errored;

    public ScenarioRunner(Long timebankMax, Long timePerMove, int maxTimeouts) {
        super(timebankMax, timePerMove, maxTimeouts);
    }

    public void prepare(JSONObject config) throws IOException {
        this.scenario = config.getJSONArray("scenario");
        JSONObject subjectConfig = config.getJSONObject("subject");
        String subjectCommand = subjectConfig.getString("command");
        this.subjectType = subjectConfig.getString("type");
        String var4 = this.subjectType;
        byte var5 = -1;
        switch (var4.hashCode()) {
            case -1298662846:
                if (var4.equals("engine")) {
                    var5 = 1;
                }
                break;
            case 97735:
                if (var4.equals("bot")) {
                    var5 = 0;
                }
        }

        switch (var5) {
            case 0:
                this.subject = this.createPlayer(subjectCommand, 0);
                return;
            case 1:
                JSONObject engineConfig = new JSONObject();

                try {
                    engineConfig = subjectConfig.getJSONObject("configuration");
                } catch (JSONException var8) {
                }

                this.subject = this.createEngine(subjectCommand, engineConfig);
                return;
            default:
                throw new RuntimeException("Scenario should contain a subject with type bot or engine");
        }
    }

    public void run() {
        this.errored = false;
        int scenarioSize = this.scenario.length();

        JSONObject result;
        try {
            for (int i = 0; i < scenarioSize; ++i) {
                String action = this.scenario.getString(i);
                if (i + 1 < scenarioSize) {
                    this.subject.write(action);
                } else {
                    String response = this.subject.ask(action, this.timeout.longValue());
                    if (response.isEmpty()) {
                        throw new IOException("Response timed out (2000ms)");
                    }
                }
            }

            result = this.createSuccessResult();
            this.setResults(result);
        } catch (IOException var6) {
            result = this.createErrorResult(var6);
            this.setResults(result);
            this.errored = true;
        }

    }

    public int postrun(long timeElapsed) {
        return this.errored ? 1 : 0;
    }

    private JSONObject createSuccessResult() {
        return this.createResult("ok");
    }

    private JSONObject createErrorResult(Exception exception) {
        JSONObject error = new JSONObject();
        error.put("message", exception.getMessage());
        JSONObject result = this.createResult("error");
        result.put("error", error);
        return result;
    }

    private JSONObject createResult(String status) {
        String errors = this.subject.getStderr();
        JSONObject subjectResult = new JSONObject();
        subjectResult.put("errors", errors);
        if (Objects.equals(this.subjectType, "bot")) {
            String dump = ((IOPlayer) this.subject).getDump();
            subjectResult.put("log", dump);
        }

        JSONObject result = new JSONObject();
        result.put("status", status);
        result.put("subject", subjectResult);
        return result;
    }
}
