package com.adventurebook.backend.importer;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

@Component
public class SlugGenerator {
    // TODO-3: Consider that for example new-the-prisoner title has "(Fixed)", which should be mapped to "fixed"
    public String slugFrom(String title) {
        if (Objects.isNull(title)) {
            return "";
        }

        return title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }
}
