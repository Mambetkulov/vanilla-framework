package com.customframework.log;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ResourceBannerPrinter implements BannerPrinter {
    private final String path;
    private final ClassLoader classLoader;

    public ResourceBannerPrinter(String path, ClassLoader classLoader) {
        this.path = path;
        this.classLoader = classLoader;
    }


    @Override
    public void printBanner(PrintStream out) {
        try (InputStream input = classLoader.getResourceAsStream(path);
             BufferedReader reader = new BufferedReader
                     (new InputStreamReader(input, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }

        } catch (IOException e) {
           out.println("Log: a file named banner.txt not found.");
        }
    }
}
