package com.adventurebook.backend.player;

import com.adventurebook.backend.exception.MissingPlayerException;
import com.adventurebook.backend.persistence.PlayerEntity;
import com.adventurebook.backend.service.PlayerService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class PlayerArgumentResolver implements HandlerMethodArgumentResolver {
    private final PlayerService playerService;

    public PlayerArgumentResolver(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Player.class)
                && PlayerEntity.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public PlayerEntity resolveArgument(
            @NonNull MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory
    ) {
        String header = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MissingPlayerException("A player name is required to play");
        }

        return playerService.findOrCreate(header.substring("Bearer ".length()).trim());
    }
}
