package io.maxmya.bench.web;

import io.maxmya.bench.domain.GitHubRepo;
import io.maxmya.bench.domain.Project;
import io.maxmya.bench.domain.SiteContent;
import io.maxmya.bench.domain.Update;
import io.maxmya.bench.service.ContentService;
import io.maxmya.bench.service.GitHubService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SiteController {

    private final ContentService content;
    private final GitHubService github;

    public SiteController(ContentService content, GitHubService github) {
        this.content = content;
        this.github = github;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "bench",
                "projects", content.get().projects().size(),
                "time", Instant.now().toString()
        );
    }

    @GetMapping("/site")
    public SiteContent site() {
        return content.get();
    }

    @GetMapping("/projects")
    public List<Project> projects() {
        return content.get().projects();
    }

    @GetMapping("/projects/{slug}")
    public Project project(@PathVariable String slug) {
        return content.get().projects().stream()
                .filter(p -> p.slug().equalsIgnoreCase(slug))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such project"));
    }

    @GetMapping("/updates")
    public List<Update> updates() {
        return content.get().updates().stream()
                .sorted(Comparator.comparing(Update::date).reversed())
                .toList();
    }

    @GetMapping("/github/repos")
    public List<GitHubRepo> repos() {
        return github.repos();
    }
}
