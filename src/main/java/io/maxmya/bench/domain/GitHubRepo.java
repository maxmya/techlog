package io.maxmya.bench.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepo(
        String name,
        String description,
        @JsonProperty("html_url") String htmlUrl,
        String language,
        @JsonProperty("pushed_at") String pushedAt,
        @JsonProperty("stargazers_count") int stars,
        boolean fork,
        boolean archived,
        List<String> topics
) {
}
