package com.example.spotifyapp.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotifyapp.Service.SpotifyCategoriesService;
import com.example.spotifyapp.Service.SpotifyNewReleasesService;

@RestController
@RequestMapping("/api/spotify")
public class StatsController {

    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);
    private final SpotifyCategoriesService spotifyCategoriesService;
    private final SpotifyNewReleasesService spotifyNewReleasesService;

    public StatsController(SpotifyCategoriesService spotifyCategoriesService, SpotifyNewReleasesService spotifyNewReleasesService) {
        this.spotifyCategoriesService = spotifyCategoriesService;
        this.spotifyNewReleasesService = spotifyNewReleasesService;
    }

    @GetMapping("/genres")
    public ResponseEntity<?> getTopGenres() {
        try {
            logger.debug("=== Top Genres endpoint called ===");
            List<String> genres = spotifyCategoriesService.getTopGenres();
            logger.info("Retrieved {} genres", genres.size());
            return ResponseEntity.ok(genres);
        } catch (Exception e) {
            logger.error("Error in top Genres: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/new-releases")
    public ResponseEntity<?> getNewReleases() {
        try {
            logger.debug("=== New Releases endpoint called ===");
            List<String> newReleases = spotifyNewReleasesService.getNewReleases();
            logger.info("Retrieved {} New Releases", newReleases.size());
            return ResponseEntity.ok(newReleases);
        } catch (Exception e) {
            logger.error("Error in New releases: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
