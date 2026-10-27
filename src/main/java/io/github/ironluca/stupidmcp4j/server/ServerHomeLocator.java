package io.github.ironluca.stupidmcp4j.server;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerHomeLocator {

    public static Path locateHome() {
        // 1. Explicit system property override: -Dstupidmcp.home=/path
        String override = System.getProperty("stupidmcp.home");
        if (override != null && !override.isBlank()) {
            return Paths.get(override).toAbsolutePath().normalize();
        }

        try {
            // 2. Locate the JAR containing this exact class
            File codeSourceFile = new File(
                ServerHomeLocator.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            );

            // If running inside "lib/stupidmcp4j.jar", go up one level to get root
            if (codeSourceFile.isFile()) {
                File parentDir = codeSourceFile.getParentFile(); // /path/to/lib
                if (parentDir != null && "lib".equalsIgnoreCase(parentDir.getName())) {
                    return parentDir.getParentFile().toPath().toAbsolutePath().normalize();
                }
                return parentDir.toPath().toAbsolutePath().normalize();
            }

            // Fallback for running unpacked classes during IDE development
            return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        } catch (URISyntaxException e) {
            return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        }
    }

    public static void main(String[] args) {
        
        System.out.println(ServerHomeLocator.locateHome());

    }
}