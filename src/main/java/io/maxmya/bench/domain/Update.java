package io.maxmya.bench.domain;

/** A log entry. projectSlug is null for site-wide notes. */
public record Update(
        String date,
        String title,
        String body,
        String kind,
        String projectSlug
) {
}
