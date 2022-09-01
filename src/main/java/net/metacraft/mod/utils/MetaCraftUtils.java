package net.metacraft.mod.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class MetaCraftUtils {
    private static int MAX_RETRY_TIME = 3;

    private static boolean isUrlValid(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static BufferedImage getBufferedImageForUrl(String input) {
        return imageIOReadWithRetry(input, 0);
    }

    public static BufferedImage imageIOReadWithRetry(String input, int count) {
        if (count == MAX_RETRY_TIME) {
            return null;
        }
        BufferedImage bufferedImage;
        System.out.println("start downloadImage url = " + input);
        try {
            if (isUrlValid(input)) {
                URL url = new URL(input);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", "the meta painting mod");
                connection.setConnectTimeout(5_000);
                connection.setReadTimeout(5_000);
                connection.connect();
                bufferedImage = ImageIO.read(connection.getInputStream());
            } else {
                File file = new File(input);
                bufferedImage = ImageIO.read(file);
            }
        } catch (Exception e) {
            return imageIOReadWithRetry(input, ++count);
        }
        return bufferedImage;
    }

    public static BufferedImage getBufferedImageForLocalPath(String input) {
        BufferedImage bufferedImage = null;
        try {
            File file = new File(input);
            bufferedImage = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("getBufferedImageForUrl IOException");
        }
        return bufferedImage;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
