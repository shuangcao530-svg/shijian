import java.io.*;
import java.net.*;
import java.nio.file.*;

public class Downloader {
    public static void main(String[] args) throws Exception {
        String url = args[0];
        String dest = args[1];
        System.out.println("Downloading: " + url);
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Downloaded to: " + dest);
    }
}
