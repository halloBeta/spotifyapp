package com.example.spotifyapp.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.spotifyapp.Auth.SpotifyAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class SpotifyCategoriesService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(SpotifyCategoriesService.class);

    @Value("${spotify.api-base-url}")
    private String apiBaseUrl;

    private SpotifyAuthService authService;

    public SpotifyCategoriesService(SpotifyAuthService authService) {
        this.authService = authService;
    }

    /**
     * Calls Spotify "Browse Categories" endpoint to get popular genres/categories.
     * Uses Client Credentials flow - no user authorization required.
     * @return list of genre categories available on Spotify
     */
    public List<String> getTopGenres() throws InterruptedException, IOException {
        String accessToken = authService.getAccessToken();
        try {
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalArgumentException("accessToken is required");
            }

            logger.debug("Access Token obtained (masked)='{}...' len={}",
                    accessToken.substring(0, Math.min(12, accessToken.length())), accessToken.length());
                
            String url = apiBaseUrl + "/browse/categories?limit=50&locale=en_US";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

            logger.debug("Sending Spotify request to URL={}", url);
            long start = System.currentTimeMillis();

            //send http request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long durationMs = System.currentTimeMillis() - start;

            logger.info("Spotify API Response Status: {} ({} ms)", response.statusCode(), durationMs);
            String respBody = response.body() == null ? "" : response.body();
            String respPreview = respBody.length() == 0 ? "<empty>" : respBody.substring(0, Math.min(1000, respBody.length()));
            logger.debug("Spotify API Response Body (preview): {}", respPreview);
            logger.debug("Spotify API Response Headers: {}", response.headers().map());

            if (response.statusCode() > 226) {
                logger.error("Spotify API returned error status {}: {}", response.statusCode(), respPreview);
                throw new RuntimeException("Spotify API returned status " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode categories = root.has("categories") ? root.get("categories") : null;
            ArrayNode items = (categories != null && categories.has("items")) ? (ArrayNode) categories.get("items") : null;

            List<String> genres = new java.util.ArrayList<>();
            if (items != null) {
                for (JsonNode item : items) {
                    if (item.has("name")) {
                        String name = item.get("name").asText();
                        if (name != null && !name.isBlank()) {
                            genres.add(name);
                        }
                    }
                }
            }

            logger.info("Retrieved {} genres from Spotify", genres.size());
            return genres;
        } catch (Exception e) {
            logger.error("Error fetching top genres: {}", e.getMessage(), e);
            throw e;
        }
    }
}
