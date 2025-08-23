// Server.java
import javax.bluetooth.*;
import javax.microedition.io.*;
import javax.sound.sampled.*;
import java.io.*;

public class Server {

    public static void main(String[] args) throws Exception {
        UUID uuid = new UUID("1101", true);  // SPP UUID
        String connectionString = "btspp://localhost:" + uuid + ";name=BluetoothVoiceServer";
        StreamConnectionNotifier notifier = (StreamConnectionNotifier) Connector.open(connectionString);

        System.out.println("[SERVER] Waiting for client to connect...");
        StreamConnection connection = notifier.acceptAndOpen();
        System.out.println("[SERVER] Client connected!");

        InputStream in = connection.openInputStream();
        OutputStream out = connection.openOutputStream();

        // Start receiving audio
        new Thread(() -> receiveAudio(in)).start();

        // Start sending audio
        sendAudio(out);
    }

    private static void sendAudio(OutputStream out) {
        try {
            AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
            TargetDataLine mic = AudioSystem.getTargetDataLine(format);
            mic.open(format);
            mic.start();

            byte[] buffer = new byte[1024];
            while (true) {
                int count = mic.read(buffer, 0, buffer.length);
                if (count > 0) {
                    out.write(buffer, 0, count);
                }
            }
        } catch (Exception e) {
            System.out.println("[SERVER] Error sending audio: " + e);
        }
    }

    private static void receiveAudio(InputStream in) {
        try {
            AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
            SourceDataLine speaker = AudioSystem.getSourceDataLine(format);
            speaker.open(format);
            speaker.start();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                speaker.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            System.out.println("[SERVER] Error receiving audio: " + e);
        }
    }
}
