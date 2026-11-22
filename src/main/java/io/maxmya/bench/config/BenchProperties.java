package io.maxmya.bench.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bench")
public record BenchProperties(String dataFile, String adminToken, String siteUrl) {

    public BenchProperties {
        if (dataFile == null || dataFile.isBlank()) {
            dataFile = "/app/data/site.json";
        }
        if (siteUrl == null || siteUrl.isBlank()) {
            siteUrl = "https://maxmya.com";
        }
        if (adminToken == null) {
            adminToken = "";
        }
    }
}
