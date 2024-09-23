package com.remote.remotecontrol;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RemoteController {

    private static final String VIDEOS_PATH = "videos";

    @GetMapping("/")
    public String home(Model model) {
        addVideoListToModel(model);
        return "index";
    }

    @PostMapping("/show-message")
    public String showMessage(String message, Model model) {
        try {
            showDialog(message);
            model.addAttribute("status", "Message displayed: " + message);
        } catch (Exception e) {
            model.addAttribute("status", "Error: " + e.getMessage());
        }

        addVideoListToModel(model);
        return "index";
    }

    @PostMapping("/play-video")
    public String playVideo(String video, Model model) {
        try {
            playVideoFile(video);
            model.addAttribute("status", "Playing video: " + video);
        } catch (Exception e) {
            model.addAttribute("status", "Error: " + e.getMessage());
        }
        addVideoListToModel(model);
        return "index";
    }

    private void showDialog(String message) throws IOException {
        String command = "powershell -Command \"Add-Type -AssemblyName System.Windows.Forms; " +
                "$form = New-Object Windows.Forms.Form; " +
                "$form.TopMost = $true; " +
                "[System.Windows.Forms.MessageBox]::Show('" + message + "', 'Message', [System.Windows.Forms.MessageBoxButtons]::OK, [System.Windows.Forms.MessageBoxIcon]::Information, [System.Windows.Forms.MessageBoxDefaultButton]::Button1, [System.Windows.Forms.MessageBoxOptions]::DefaultDesktopOnly)\"";

        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
        processBuilder.inheritIO();
        Process process = processBuilder.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Failed to execute PowerShell command", e);
        }
    }

    private void playVideoFile(String video) throws IOException {
        String videoPath = VIDEOS_PATH + "/" + video;
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", videoPath);
        processBuilder.inheritIO();
        Process process = processBuilder.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Failed to play video", e);
        }
    }

    private void addVideoListToModel(Model model) {
        List<String> videos = getVideoList();
        model.addAttribute("videos", videos);
    }

    private List<String> getVideoList() {
        List<String> videoList = new ArrayList<>();
        File videoDir = new File(VIDEOS_PATH);
        if (videoDir.exists() && videoDir.isDirectory()) {
            File[] files = videoDir.listFiles((dir, name) -> name.endsWith(".mp4") || name.endsWith(".avi"));
            if (files != null) {
                for (File file : files) {
                    videoList.add(file.getName());
                }
            }
        }
        return videoList;
    }
}
