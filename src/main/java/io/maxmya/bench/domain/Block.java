package io.maxmya.bench.domain;

import java.util.List;

/**
 * A block on a project landing page.
 * type: text | features | quote | install | links
 */
public record Block(
        String type,
        String title,
        String body,
        List<String> items,
        List<Link> links
) {
}
