package io.maxmya.bench.domain;

import java.util.List;

public record Landing(
        String eyebrow,
        String headline,
        String deck,
        String ctaLabel,
        String ctaUrl,
        List<Block> blocks
) {
}
