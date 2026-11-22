package io.maxmya.bench.domain;

import java.util.List;

public record SiteContent(
        Profile profile,
        List<Project> projects,
        List<Update> updates
) {
}
