package io.maxmya.bench.domain;

import java.util.List;

public record Profile(
        String handle,
        String name,
        String role,
        String location,
        String since,
        String bio,
        List<Link> links
) {
}
