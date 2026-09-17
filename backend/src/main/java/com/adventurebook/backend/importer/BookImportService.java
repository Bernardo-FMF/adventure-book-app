package com.adventurebook.backend.importer;

import com.adventurebook.backend.importer.dto.BookFileDto;
import com.adventurebook.backend.importer.mapper.BookMapper;
import com.adventurebook.backend.importer.validator.BookValidator;
import com.adventurebook.backend.importer.validator.ParseResult;
import com.adventurebook.backend.importer.validator.ValidationError;
import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class BookImportService implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BookImportService.class);

    private final BookValidator validator;
    private final SlugGenerator slugGenerator;
    private final BookMapper mapper;
    private final BookRepository repository;
    private final String seedLocation;

    public BookImportService(
            BookValidator validator,
            SlugGenerator slugGenerator,
            BookMapper mapper,
            BookRepository repository,
            @Value("${app.books.seed-location}") String seedLocation
    ) {
        this.validator = validator;
        this.slugGenerator = slugGenerator;
        this.mapper = mapper;
        this.repository = repository;
        this.seedLocation = seedLocation;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Importing seed data from: {}", seedLocation);

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(seedLocation);

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            String content;
            try (InputStream in = resource.getInputStream()) {
                content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            ParseResult res = validator.validate(content);
            if (!res.isValid()) {
                for (ValidationError err : res.errors()) {
                    log.error("{}: {}: {}", filename, err.type(), err.message());
                }
                continue;
            }

            BookFileDto book = res.book();
            log.info("Processing {}: {}", filename, book);

            String slug = slugGenerator.slugFrom(book.title());
            if (repository.existsBySlug(slug)) {
                log.debug("Book {} already present in database, will not process it", filename);
                continue;
            }

            BookEntity entity = mapper.toEntity(book, slug);
            try {
                repository.save(entity);
            } catch (Exception ex) {
                log.warn("Book {} was rejected by the database. Cause: {}", filename, NestedExceptionUtils.getMostSpecificCause(ex).getMessage());
            }

            log.info("Book {} has been imported to the database", filename);
        }
    }
}
