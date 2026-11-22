package io.maxmya.bench.service;

import io.maxmya.bench.domain.GitHubRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** Reads the public repository list from GitHub and keeps a short-lived copy. */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    private final RestClient client;
    private final String user;
    private final Duration ttl;
    private final AtomicReference<Cached> cache = new AtomicReference<>();

    public GitHubService(
            RestClient.Builder builder,
            @Value("${bench.github.user:maxmya}") String user,
            @Value("${bench.github.token:}") String token,
            @Value("${bench.github.cache-minutes:30}") long cacheMinutes) {
        RestClient.Builder configured = builder
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "maxmya-bench/1.0")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (token != null && !token.isBlank()) {
            configured.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        this.client = configured.build();
        this.user = user;
        this.ttl = Duration.ofMinutes(cacheMinutes);
    }

    public List<GitHubRepo> repos() {
        Cached current = cache.get();
        if (current != null && !current.expired()) return current.repos();

        synchronized (this) {
            current = cache.get();
            if (current != null && !current.expired()) return current.repos();
            try {
                GitHubRepo[] body = client.get()
                        .uri(uri -> uri.path("/users/{user}/repos")
                                .queryParam("type", "owner")
                                .queryParam("sort", "updated")
                                .queryParam("per_page", 100)
                                .build(user))
                        .retrieve()
                        .body(GitHubRepo[].class);

                List<GitHubRepo> repos = Arrays.stream(body == null ? new GitHubRepo[0] : body)
                        .sorted(Comparator.comparing(
                                GitHubRepo::pushedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .toList();
                cache.set(new Cached(repos, Instant.now().plus(ttl)));
                log.info("Loaded {} public repositories from GitHub", repos.size());
                return repos;
            } catch (RestClientException ex) {
                log.warn("GitHub repository lookup failed: {}", ex.getMessage());
                return current == null ? List.of() : current.repos();
            }
        }
    }

    private record Cached(List<GitHubRepo> repos, Instant expiresAt) {
        boolean expired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
