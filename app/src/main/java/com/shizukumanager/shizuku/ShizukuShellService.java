package com.shizukumanager.app.shizuku;

import android.os.Build;
import android.os.Process;
import com.shizukumanager.app.shizuku.aidl.IShizukuShellService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

// Dich vu chay trong tien trinh Shizuku de thuc thi lenh PackageManager / ActivityManager an toan
public class ShizukuShellService extends IShizukuShellService.Stub {

    private static final Set<String> ALLOWED_EXECUTABLES = new HashSet<>(Arrays.asList("pm", "am", "cmd"));

    public ShizukuShellService() {
    }

    @Override
    public void destroy() {
        System.exit(0);
    }

    @Override
    public int getServiceUid() {
        return Process.myUid();
    }

    @Override
    public String executeCommand(String[] commandArgs) {
        JSONObject response = new JSONObject();
        try {
            if (commandArgs == null || commandArgs.length == 0 || !ALLOWED_EXECUTABLES.contains(commandArgs[0])) {
                response.put("exitCode", -1);
                response.put("stdout", "");
                response.put("stderr", "Disallowed or empty command");
                return response.toString();
            }

            ProcessBuilder pb = new ProcessBuilder(commandArgs);
            java.lang.Process proc = pb.start();

            String stdout = readStream(proc.getInputStream());
            String stderr = readStream(proc.getErrorStream());

            int exitCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean finished = proc.waitFor(15, TimeUnit.SECONDS);
                if (!finished) {
                    proc.destroyForcibly();
                    exitCode = -2;
                    stderr = stderr + "\nCommand timed out";
                } else {
                    exitCode = proc.exitValue();
                }
            } else {
                exitCode = proc.waitFor();
            }

            response.put("exitCode", exitCode);
            response.put("stdout", stdout.trim());
            response.put("stderr", stderr.trim());
        } catch (Exception e) {
            try {
                response.put("exitCode", -3);
                response.put("stdout", "");
                response.put("stderr", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            } catch (Exception ignored) {
            }
        }
        return response.toString();
    }

    private String readStream(java.io.InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
        } catch (Exception ignored) {
        }
        return sb.toString();
    }
}
