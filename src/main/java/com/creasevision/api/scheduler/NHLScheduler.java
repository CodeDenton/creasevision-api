package com.creasevision.api.scheduler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.model.ShotLocationDetail;
import com.creasevision.api.model.ShotLocationSummary;
import com.creasevision.api.repository.GoalieRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NHLScheduler {

    private final GoalieRepository goalieRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SEASON = "20252026";
    private static final String SITUATION = "2";

    private static final List<String> TEAMS = List.of(
        "ANA", "BOS", "BUF", "CAR", "CBJ", "CGY", "CHI", "COL", "DAL", "DET",
        "EDM", "FLA", "LAK", "MIN", "MTL", "NJD", "NSH", "NYI", "NYR", "OTT",
        "PHI", "PIT", "SEA", "SJS", "STL", "TBL", "TOR", "UTA", "VAN", "VGK",
        "WPG", "WSH"
    );

    @EventListener(ApplicationReadyEvent.class)
    public void fetchOnStartup() {
        fetchAndSaveGoalies();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void fetchAndSaveGoalies() {
        System.out.println("Fetching goalies directly from NHL API...");
        try {
            HttpClient client = HttpClient.newHttpClient();

            //fetch all goalie IDs from every roster
            List<CompletableFuture<String>> rosterFutures = TEAMS.stream()
                .map(team -> client.sendAsync(
                    HttpRequest.newBuilder()
                        .uri(URI.create("https://api-web.nhle.com/v1/roster/" + team + "/current"))
                        .GET()
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                ).thenApply(HttpResponse::body))
                .toList();

            List<Long> goalieIds = new ArrayList<>();
            for (CompletableFuture<String> future : rosterFutures) {
                try {
                    String body = future.get();
                    JsonNode roster = objectMapper.readTree(body);
                    JsonNode goalies = roster.get("goalies");
                    if (goalies != null) {
                        for (JsonNode g : goalies) {
                            goalieIds.add(g.get("id").asLong());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching roster: " + e.getMessage());
                }
            }

            System.out.println("Found " + goalieIds.size() + " goalies across all rosters");

            //fetch details for each goalie
            List<CompletableFuture<String>> detailFutures = goalieIds.stream()
                .map(id -> client.sendAsync(
                    HttpRequest.newBuilder()
                        .uri(URI.create("https://api-web.nhle.com/v1/edge/goalie-detail/" + id + "/" + SEASON + "/" + SITUATION))
                        .GET()
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                ).thenApply(HttpResponse::body))
                .toList();

            List<Goalie> goalies = new ArrayList<>();
            for (CompletableFuture<String> future : detailFutures) {
                try {
                    String body = future.get();
                    JsonNode node = objectMapper.readTree(body);
                    JsonNode player = node.get("player");
                    if (player == null) continue;

                    Goalie goalie = new Goalie();
                    goalie.setId(player.get("id").asLong());
                    goalie.setFirstName(player.get("firstName").get("default").asText());
                    goalie.setLastName(player.get("lastName").get("default").asText());
                    goalie.setHeadshot(player.get("headshot").asText());
                    goalie.setSweaterNumber(player.get("sweaterNumber").asInt());
                    goalie.setTeamAbbrev(player.get("team").get("abbrev").asText());
                    goalie.setTeamName(player.get("team").get("commonName").get("default").asText());
                    goalie.setTeamLogoLight(player.get("team").get("teamLogo").get("light").asText());
                    goalie.setTeamLogoDark(player.get("team").get("teamLogo").get("dark").asText());
                    goalie.setWins(player.get("wins").asInt());
                    goalie.setLosses(player.get("losses").asInt());
                    goalie.setOvertimeLosses(player.get("overtimeLosses").asInt());
                    goalie.setGamesPlayed(player.get("gamesPlayed").asInt());
                    goalie.setGoalsAgainstAvg(player.get("goalsAgainstAvg").asDouble());
                    goalie.setSavePctg(player.get("savePctg").asDouble());

                    // Shot Location Summary
                    List<ShotLocationSummary> summaries = new ArrayList<>();
                    JsonNode summaryNode = node.get("shotLocationSummary");
                    if (summaryNode != null) {
                        for (JsonNode s : summaryNode) {
                            ShotLocationSummary summary = new ShotLocationSummary();
                            summary.setGoalie(goalie);
                            summary.setLocationCode(s.get("locationCode").asText());
                            summary.setGoalsAgainst(s.get("goalsAgainst").asInt());
                            summary.setGoalsAgainstPercentile(s.get("goalsAgainstPercentile").asDouble());
                            summary.setGoalsAgainstLeagueAvg(s.get("goalsAgainstLeagueAvg").asDouble());
                            summary.setSaves(s.get("saves").asInt());
                            summary.setSavesPercentile(s.get("savesPercentile").asDouble());
                            summary.setSavesLeagueAvg(s.get("savesLeagueAvg").asDouble());
                            summary.setSavePctg(s.get("savePctg").asDouble());
                            summary.setSavePctgPercentile(s.get("savePctgPercentile").asDouble());
                            summary.setSavePctgLeagueAvg(s.get("savePctgLeagueAvg").asDouble());
                            summaries.add(summary);
                        }
                    }
                    goalie.setShotLocationSummary(summaries);

                    // Shot Location Details
                    List<ShotLocationDetail> details = new ArrayList<>();
                    JsonNode detailNode = node.get("shotLocationDetails");
                    if (detailNode != null) {
                        for (JsonNode d : detailNode) {
                            ShotLocationDetail detail = new ShotLocationDetail();
                            detail.setGoalie(goalie);
                            detail.setArea(d.get("area").asText());
                            detail.setSaves(d.get("saves").asInt());
                            detail.setSavesPercentile(d.get("savesPercentile").asDouble());
                            detail.setSavePctg(d.get("savePctg").asDouble());
                            detail.setSavePctgPercentile(d.get("savePctgPercentile").asDouble());
                            details.add(detail);
                        }
                    }
                    goalie.setShotLocationDetails(details);

                    goalies.add(goalie);
                } catch (Exception e) {
                    System.err.println("Error parsing goalie: " + e.getMessage());
                }
            }

            goalieRepository.saveAll(goalies);
            System.out.println("Saved " + goalies.size() + " goalies to database!");

        } catch (Exception e) {
            System.err.println("Error in fetchAndSaveGoalies: " + e.getMessage());
        }
    }
}