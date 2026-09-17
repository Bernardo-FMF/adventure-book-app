package com.adventurebook.backend.importer.mapper;

import com.adventurebook.backend.importer.dto.OptionDto;
import com.adventurebook.backend.persistence.OptionEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.adventurebook.backend.utils.StringUtils.blankToNull;

@Component
public class OptionMapper {
    private final ConsequenceMapper consequenceMapper;

    public OptionMapper(
            ConsequenceMapper consequenceMapper
    ) {
        this.consequenceMapper = consequenceMapper;
    }

    public OptionEntity toEntity(OptionDto option) {
        OptionEntity entity = new OptionEntity(option.description(), blankToNull(option.gotoId()));

        if (Objects.nonNull(option.consequence())) {
            entity.setConsequence(consequenceMapper.toEntity(option.consequence()));
        }
        return entity;
    }
}
