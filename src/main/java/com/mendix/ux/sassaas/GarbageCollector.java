package com.mendix.ux.sassaas;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

@EnableScheduling
@Component
public class GarbageCollector {

    private static final Logger logger = LoggerFactory.getLogger(GarbageCollector.class);

    @Value(value = "${ttl:43200}")
    private Long TTL;

    @Value(value = "${keep:10}")
    private Long KEEP;

    @Scheduled(fixedDelayString = "${delay:50000}", initialDelayString = "0")
    public void cleanUpCache() {
        BasicFileAttributes basicFileAttributes;
        Long currentTime = new Date().getTime();
        int removedCount = 0;
        int activeCount = 0;
        int failedCount = 0;
        File[] sessions = Application.CACHE_DIR.listFiles();
        if (sessions.length < KEEP) {
            logger.debug("Backing off cache clean up because of low usage. Current active sessions = " + sessions.length);
            return;
        }

        logger.debug("TTL = " + TTL);
        for (File session: sessions) {
            try {
                basicFileAttributes = Files.readAttributes(session.toPath(), BasicFileAttributes.class);
                Long modifiedTime = basicFileAttributes.lastModifiedTime().toMillis();
                if ((currentTime - modifiedTime) / 1000 > TTL) {
                    logger.info("Cleaning up: " + session);
                    FileUtils.deleteDirectory(session);
                    removedCount++;
                } else {
                    activeCount++;
                    logger.debug("Skipping active: " + session);
                }
            } catch (IOException e) {
                logger.warn("Failed to read attributes for: " + session);
                logger.warn(e.getMessage());
                failedCount++;
            }
        }
        logger.info(String.format("Failed = %d, Removed = %d, Active = %d", failedCount, removedCount, activeCount));
    }
}
