package io.maxmya.bench.web;

import io.maxmya.bench.config.BenchProperties;
import io.maxmya.bench.domain.Project;
import io.maxmya.bench.domain.SiteContent;
import io.maxmya.bench.domain.Version;
import io.maxmya.bench.service.ContentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ContentService content;
    private final BenchProperties properties;

    public AdminController(ContentService content, BenchProperties properties) {
        this.content = content;
        this.properties = properties;
    }

    @PutMapping("/content")
    public SiteContent replaceContent(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestBody SiteContent body) {
        require(token);
        return content.replace(body);
    }

    @PutMapping("/projects/{slug}")
    public Project saveProject(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable String slug,
            @RequestBody Project body) {
        require(token);
        if (!slug.equals(body.slug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug in path and body must match");
        }
        content.upsertProject(body);
        return body;
    }

    @PostMapping("/projects/{slug}/versions")
    public Project addVersion(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable String slug,
            @RequestBody Version version) {
        require(token);
        try {
            content.addVersion(slug, version);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return content.get().projects().stream()
                .filter(p -> p.slug().equals(slug))
                .findFirst()
                .orElseThrow();
    }

    @DeleteMapping("/projects/{slug}")
    public void deleteProject(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable String slug) {
        require(token);
        try {
            content.deleteProject(slug);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private void require(String token) {
        String expected = properties.adminToken();
        if (expected == null || expected.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin API is disabled");
        }
        if (token == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad token");
        }
    }
}
