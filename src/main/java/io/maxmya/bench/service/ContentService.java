package io.maxmya.bench.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.maxmya.bench.config.BenchProperties;
import io.maxmya.bench.domain.Project;
import io.maxmya.bench.domain.SiteContent;
import io.maxmya.bench.domain.Update;
import io.maxmya.bench.domain.Version;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** Single-file content store. Seed ships in the jar; edits live on a mounted volume. */
@Service
public class ContentService {

    private final ObjectMapper mapper;
    private final Path dataFile;
    private final AtomicReference<SiteContent> current = new AtomicReference<>();
    private final Object writeLock = new Object();

    public ContentService(ObjectMapper mapper, BenchProperties properties) {
        this.mapper = mapper;
        this.dataFile = Path.of(properties.dataFile());
    }

    @PostConstruct
    void init() throws IOException {
        Path parent = dataFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(dataFile)) {
            ClassPathResource seed = new ClassPathResource("content/site.json");
            try (var in = seed.getInputStream()) {
                Files.copy(in, dataFile);
            }
        }
        current.set(normalize(mapper.readValue(Files.readAllBytes(dataFile), SiteContent.class)));
    }

    public SiteContent get() {
        return current.get();
    }

    public SiteContent replace(SiteContent content) {
        SiteContent next = normalize(content);
        persist(next);
        return next;
    }

    public SiteContent upsertProject(Project project) {
        List<Project> projects = new ArrayList<>(current.get().projects());
        projects.removeIf(p -> p.slug().equals(project.slug()));
        projects.add(project);
        return replace(new SiteContent(current.get().profile(), projects, current.get().updates()));
    }

    public SiteContent deleteProject(String slug) {
        List<Project> projects = new ArrayList<>(current.get().projects());
        boolean removed = projects.removeIf(p -> p.slug().equals(slug));
        if (!removed) {
            throw new IllegalArgumentException("Unknown project: " + slug);
        }
        return replace(new SiteContent(current.get().profile(), projects, current.get().updates()));
    }

    public SiteContent addVersion(String slug, Version version) {
        List<Project> projects = new ArrayList<>();
        boolean found = false;
        for (Project project : current.get().projects()) {
            if (project.slug().equals(slug)) {
                List<Version> versions = new ArrayList<>(project.versions());
                versions.removeIf(v -> v.tag().equals(version.tag()));
                versions.add(0, version);
                projects.add(new Project(project.slug(), project.name(), project.tagline(),
                        project.description(), project.status(), project.language(), project.repoUrl(),
                        project.wikiUrl(), project.demoUrl(), project.tags(), project.accent(),
                        project.landing(), versions, project.updates()));
                found = true;
            } else {
                projects.add(project);
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Unknown project: " + slug);
        }
        return replace(new SiteContent(current.get().profile(), projects, current.get().updates()));
    }

    private void persist(SiteContent content) {
        synchronized (writeLock) {
            try {
                Path tmp = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
                mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), content);
                try {
                    Files.move(tmp, dataFile, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tmp, dataFile, StandardCopyOption.REPLACE_EXISTING);
                }
                current.set(content);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not write site content", e);
            }
        }
    }

    private SiteContent normalize(SiteContent content) {
        if (content == null) {
            throw new IllegalArgumentException("Content is required");
        }
        List<Project> projects = new ArrayList<>();
        for (Project project : content.projects() == null ? List.<Project>of() : content.projects()) {
            projects.add(new Project(
                    project.slug(), project.name(),
                    project.tagline() == null ? "" : project.tagline(),
                    project.description() == null ? "" : project.description(),
                    project.status() == null ? "active" : project.status(),
                    project.language() == null ? "misc" : project.language(),
                    project.repoUrl(), project.wikiUrl(), project.demoUrl(),
                    project.tags() == null ? List.of() : project.tags(),
                    project.accent() == null ? "#d8402b" : project.accent(),
                    project.landing(),
                    project.versions() == null ? List.of() : project.versions(),
                    project.updates() == null ? List.<Update>of() : project.updates()));
        }
        return new SiteContent(content.profile(), projects,
                content.updates() == null ? List.of() : content.updates());
    }

    public Path dataFile() {
        return dataFile;
    }
}
