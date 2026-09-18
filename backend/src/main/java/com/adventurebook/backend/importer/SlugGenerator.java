package com.adventurebook.backend.importer;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

import static com.adventurebook.backend.utils.StringUtils.blankToNull;

@Component
public class SlugGenerator {
    public String slugFrom(String title) {
        String normalized = blankToNull(title);
        if (Objects.isNull(normalized)) {
            return "";
        }

        return Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }
}
