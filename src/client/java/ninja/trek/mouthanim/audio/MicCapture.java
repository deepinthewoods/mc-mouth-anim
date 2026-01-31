package ninja.trek.mouthanim.audio;

import ninja.trek.mouthanim.MouthAnim;
import ninja.trek.mouthanim.MouthState;
import ninja.trek.mouthanim.config.MouthAnimConfig;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class MicCapture {
    private static final float SAMPLE_RATE = 44100f;
    private static final int BUFFER_SIZE = 1024;
    private static final float SMOOTHING_ALPHA = 0.3f;

    private volatile double maxRms = 500.0;
    private volatile double pctSlightlyOpen = 10.0;
    private volatile double pctOpen = 30.0;
    private volatile double pctWideOpen = 60.0;
    private volatile String selectedMixer = "";

    private volatile MouthState currentState = MouthState.CLOSED;
    private volatile boolean running = false;
    private Thread captureThread;

    public MouthState getCurrentState() {
        return currentState;
    }

    public void setThresholds(double maxRms, double pctSlightlyOpen, double pctOpen, double pctWideOpen) {
        this.maxRms = maxRms;
        this.pctSlightlyOpen = pctSlightlyOpen;
        this.pctOpen = pctOpen;
        this.pctWideOpen = pctWideOpen;
    }

    public void setMixer(String mixerName) {
        String oldMixer = this.selectedMixer;
        this.selectedMixer = mixerName == null ? "" : mixerName;
        if (!oldMixer.equals(this.selectedMixer)) {
            restart();
        }
    }

    public void restart() {
        stop();
        start();
    }

    public void start() {
        if (running) return;
        running = true;

        captureThread = new Thread(this::captureLoop, "MicCapture");
        captureThread.setDaemon(true);
        captureThread.start();
    }

    public void stop() {
        running = false;
        if (captureThread != null) {
            captureThread.interrupt();
            try {
                captureThread.join(1000);
            } catch (InterruptedException ignored) {}
            captureThread = null;
        }
    }

    private void captureLoop() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try (TargetDataLine line = openLine(info, format)) {
            if (line == null) {
                MouthAnim.LOGGER.warn("No microphone available - mouth will stay CLOSED");
                return;
            }

            line.start();
            MouthAnim.LOGGER.info("MicCapture started (device: {})",
                    selectedMixer.isEmpty() ? "System Default" : selectedMixer);

            byte[] buffer = new byte[BUFFER_SIZE * 2]; // 16-bit = 2 bytes per sample
            double smoothedRms = 0.0;

            while (running) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead <= 0) continue;

                double rms = computeRms(buffer, bytesRead);
                smoothedRms = SMOOTHING_ALPHA * rms + (1.0 - SMOOTHING_ALPHA) * smoothedRms;
                currentState = mapToState(smoothedRms);
            }

            line.stop();
        } catch (LineUnavailableException e) {
            MouthAnim.LOGGER.warn("Could not open microphone: {}", e.getMessage());
        } catch (Exception e) {
            if (running) {
                MouthAnim.LOGGER.error("MicCapture error", e);
            }
        }
    }

    private TargetDataLine openLine(DataLine.Info info, AudioFormat format) throws LineUnavailableException {
        if (!selectedMixer.isEmpty()) {
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
                if (mixerInfo.getName().equals(selectedMixer)) {
                    Mixer mixer = AudioSystem.getMixer(mixerInfo);
                    if (mixer.isLineSupported(info)) {
                        TargetDataLine line = (TargetDataLine) mixer.getLine(info);
                        line.open(format, BUFFER_SIZE * 2);
                        return line;
                    }
                }
            }
            MouthAnim.LOGGER.warn("Selected mixer '{}' not found or unsupported, falling back to default", selectedMixer);
        }

        if (!AudioSystem.isLineSupported(info)) {
            return null;
        }
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format, BUFFER_SIZE * 2);
        return line;
    }

    private static double computeRms(byte[] buffer, int bytesRead) {
        int samples = bytesRead / 2;
        long sumSquares = 0;

        for (int i = 0; i < bytesRead - 1; i += 2) {
            // Little-endian 16-bit signed
            short sample = (short) ((buffer[i] & 0xFF) | (buffer[i + 1] << 8));
            sumSquares += (long) sample * sample;
        }

        return Math.sqrt((double) sumSquares / samples);
    }

    private MouthState mapToState(double rms) {
        double max = this.maxRms;
        if (rms >= max * pctWideOpen / 100.0) return MouthState.WIDE_OPEN;
        if (rms >= max * pctOpen / 100.0) return MouthState.OPEN;
        if (rms >= max * pctSlightlyOpen / 100.0) return MouthState.SLIGHTLY_OPEN;
        return MouthState.CLOSED;
    }

    public static List<String> getAvailableInputDevices() {
        List<String> devices = new ArrayList<>();
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            try {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                if (mixer.isLineSupported(info)) {
                    devices.add(mixerInfo.getName());
                }
            } catch (Exception ignored) {}
        }
        return devices;
    }

    public static void applyConfig(MouthAnimConfig config) {
        // This is called from the config screen save callback.
        // The actual MicCapture instance is in MouthAnimClient, so we delegate there.
        ninja.trek.mouthanim.MouthAnimClient.applyConfig(config);
    }
}
