package com.example.spotifyapp.Auth;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SpotifyAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyAuthService.class);


    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.token-url}")
    private String tokenUrl;

    /**
     * Gets an access token from Spotify using Client Credentials flow.
     *
     * @return access token string
     */
    public String getAccessToken() {
        try {
            // 1. Create Basic Auth header
            String auth = clientId + ":" + clientSecret;
            //logger.debug("Auth: {}", auth);
            
            String encodedAuth = Base64.getEncoder()
                    .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            // 2. Request body
            String requestBody = "grant_type=" +
                    URLEncoder.encode("client_credentials", StandardCharsets.UTF_8);

            // 3. Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

                // 4. Send request
                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

                logger.info("Spotify token endpoint status: {}", response.statusCode());
                String bodyPreview = response.body() == null ? "<empty>" : response.body().substring(0, Math.min(500, response.body().length()));
                logger.debug("Spotify token endpoint body (preview): {}", bodyPreview);

                if (response.statusCode() != 200) {
                throw new RuntimeException(
                    "Failed to get token. HTTP " + response.statusCode()
                        + ": " + response.body());
                }

                // 5. Extract access_token from JSON (simple parsing)
                String accessToken = extractAccessToken(response.body());
                if (accessToken != null) {
                accessToken = accessToken.trim();
                }
                logger.debug("Retrieved Spotify access token (trimmed, masked): {}...",
                        accessToken == null ? "<null>" : accessToken.substring(0, Math.min(40, accessToken.length())));
                return accessToken;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving Spotify access token", e);
        }
    }

    /**
     * Simple JSON parsing without libraries.
     * Assumes Spotify's standard token response.
     */
    private static String extractAccessToken(String json) {
        String tokenKey = "\"access_token\":\"";
        int start = json.indexOf(tokenKey);
        if (start == -1) {
            throw new RuntimeException("access_token not found in response");
        }
        start += tokenKey.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
