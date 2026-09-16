package com.adventurebook.backend.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class BookImportService implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BookImportService.class);

    private final String seedLocation;

    public BookImportService(
            @Value("${app.books.seed-location}") String seedLocation
    ) {
        this.seedLocation = seedLocation;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Importing from: {}", seedLocation);

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(seedLocation);

        for (Resource resource: resources) {
            String filename = resource.getFilename();
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
