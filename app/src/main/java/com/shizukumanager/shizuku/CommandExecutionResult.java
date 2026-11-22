package com.shizukumanager.app.shizuku;

// Ket qua tho sau khi goi lenh qua ShizukuShellService
public class CommandExecutionResult {
    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final boolean binderLost;

    public CommandExecutionResult(int exitCode, String stdout, String stderr, boolean binderLost) {
        this.exitCode = exitCode;
        this.stdout = stdout == null ? "" : stdout;
        this.stderr = stderr == null ? "" : stderr;
        this.binderLost = binderLost;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public boolean isBinderLost() {
        return binderLost;
    }

    public boolean isExitZero() {
        return !binderLost && exitCode == 0;
    }
}
