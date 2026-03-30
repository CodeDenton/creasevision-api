package com.creasevision.api.scheduler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

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

    @EventListener(ApplicationReadyEvent.class)
    public void fetchOnStartup() {
        fetchAndSaveGoalies();
    }

    @Scheduled(cron = "0 0 3 * * *") // runs every day at 3am
    public void fetchAndSaveGoalies() {
        System.out.println("Fetching goalies from NHL API...");
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://goaliestatsfastapi.onrender.com/goalies/full"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            List<Goalie> goalies = new ArrayList<>();

            for (JsonNode node : root) {
                JsonNode player = node.get("player");

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
                for (JsonNode s : node.get("shotLocationSummary")) {
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
                goalie.setShotLocationSummary(summaries);

                // Shot Location Details
                List<ShotLocationDetail> details = new ArrayList<>();
                for (JsonNode d : node.get("shotLocationDetails")) {
                    ShotLocationDetail detail = new ShotLocationDetail();
                    detail.setGoalie(goalie);
                    detail.setArea(d.get("area").asText());
                    detail.setSaves(d.get("saves").asInt());
                    detail.setSavesPercentile(d.get("savesPercentile").asDouble());
                    detail.setSavePctg(d.get("savePctg").asDouble());
                    detail.setSavePctgPercentile(d.get("savePctgPercentile").asDouble());
                    details.add(detail);
                }
                goalie.setShotLocationDetails(details);

                goalies.add(goalie);
            }

            goalieRepository.saveAll(goalies);
            System.out.println("Saved " + goalies.size() + " goalies to database!");

        } catch (Exception e) {
            System.err.println("Error fetching goalies: " + e.getMessage());
        }
    }
}