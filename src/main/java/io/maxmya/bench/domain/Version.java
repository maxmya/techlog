package io.maxmya.bench.domain;

public record Version(
        String tag,
        String date,
        String title,
        String notes,
        String changelogUrl,
        String downloadUrl,
        boolean current
) {
}
