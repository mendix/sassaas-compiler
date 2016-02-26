package com.mendix.ux.sassaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class Application {

    public static File SESSION_DIR = new File("cache");

    public static void main(String[] args) {
        ensureSessionDir();
        SpringApplication.run(Application.class, args);
    }

    private static void ensureSessionDir() {
        if (!SESSION_DIR.isDirectory()) {
            SESSION_DIR.mkdirs();
        }
    }
}
