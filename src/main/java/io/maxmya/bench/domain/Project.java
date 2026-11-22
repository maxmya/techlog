package io.maxmya.bench.domain;

import java.util.List;

public record Project(
        String slug,
        String name,
        String tagline,
        String description,
        String status,
        String language,
        String repoUrl,
        String wikiUrl,
        String demoUrl,
        List<String> tags,
        String accent,
        Landing landing,
        List<Version> versions,
        List<Update> updates
) {
}
