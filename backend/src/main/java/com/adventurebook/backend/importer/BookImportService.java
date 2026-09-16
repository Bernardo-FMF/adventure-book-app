package com.adventurebook.backend.importer;

import com.adventurebook.backend.importer.validator.BookValidator;
import com.adventurebook.backend.importer.validator.ParseResult;
import com.adventurebook.backend.importer.validator.ValidationError;
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

    private final BookValidator validator;
    private final String seedLocation;

    public BookImportService(
            BookValidator validator,
            @Value("${app.books.seed-location}") String seedLocation
    ) {
        this.validator = validator;
        this.seedLocation = seedLocation;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Importing seed data from: {}", seedLocation);

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(seedLocation);

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            ParseResult res = validator.validate(content);
            if (res.isValid()) {
                log.info(res.book().toString());
            } else {
                for (ValidationError err : res.errors()) {
                    log.error("{}: {}", err.type(), err.message());
                }
            }
        }
    }
}
