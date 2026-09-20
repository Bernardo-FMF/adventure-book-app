package com.adventurebook.backend.config;

import com.adventurebook.backend.player.PlayerArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class PlayerResolverConfig implements WebMvcConfigurer {
    private final PlayerArgumentResolver playerResolver;

    public PlayerResolverConfig(PlayerArgumentResolver playerResolver) {
        this.playerResolver = playerResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(playerResolver);
    }
}