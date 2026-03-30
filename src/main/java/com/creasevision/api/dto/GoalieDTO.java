package com.creasevision.api.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.creasevision.api.model.Goalie;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GoalieDTO {

    private PlayerDTO player;
    private List<ShotSummaryDTO> shotLocationSummary;
    private List<ShotDetailDTO> shotLocationDetails;

    @Data
    public static class PlayerDTO {

        private Long id;
        private NameDTO firstName;
        private NameDTO lastName;
        private String headshot;
        private Integer sweaterNumber;
        private Integer wins;
        private Integer losses;
        private Integer overtimeLosses;
        private Integer gamesPlayed;
        private Double goalsAgainstAvg;
        private Double savePctg;
        private TeamDTO team;
    }

    @Data
    public static class NameDTO {

        @JsonProperty("default")
        private String default_;
    }

    @Data
    public static class TeamDTO {

        private String abbrev;
        private CommonNameDTO commonName;
        private TeamLogoDTO teamLogo;
    }

    @Data
    public static class CommonNameDTO {

        @JsonProperty("default")
        private String default_;
    }

    @Data
    public static class TeamLogoDTO {

        private String light;
        private String dark;
    }

    @Data
    public static class ShotSummaryDTO {

        private String locationCode;
        private Integer goalsAgainst;
        private Double goalsAgainstPercentile;
        private Double goalsAgainstLeagueAvg;
        private Integer saves;
        private Double savesPercentile;
        private Double savesLeagueAvg;
        private Double savePctg;
        private Double savePctgPercentile;
        private Double savePctgLeagueAvg;
    }

    @Data
    public static class ShotDetailDTO {

        private String area;
        private Integer saves;
        private Double savesPercentile;
        private Double savePctg;
        private Double savePctgPercentile;
    }

    // converts a Goalie entity into a GoalieDTO
    public static GoalieDTO fromEntity(Goalie goalie) {
        GoalieDTO dto = new GoalieDTO();

        PlayerDTO player = new PlayerDTO();
        player.setId(goalie.getId());

        NameDTO firstName = new NameDTO();
        firstName.setDefault_(goalie.getFirstName());
        player.setFirstName(firstName);

        NameDTO lastName = new NameDTO();
        lastName.setDefault_(goalie.getLastName());
        player.setLastName(lastName);

        player.setHeadshot(goalie.getHeadshot());
        player.setSweaterNumber(goalie.getSweaterNumber());
        player.setWins(goalie.getWins());
        player.setLosses(goalie.getLosses());
        player.setOvertimeLosses(goalie.getOvertimeLosses());
        player.setGamesPlayed(goalie.getGamesPlayed());
        player.setGoalsAgainstAvg(goalie.getGoalsAgainstAvg());
        player.setSavePctg(goalie.getSavePctg());

        TeamDTO team = new TeamDTO();
        team.setAbbrev(goalie.getTeamAbbrev());

        CommonNameDTO commonName = new CommonNameDTO();
        commonName.setDefault_(goalie.getTeamName());
        team.setCommonName(commonName);

        TeamLogoDTO teamLogo = new TeamLogoDTO();
        teamLogo.setLight(goalie.getTeamLogoLight());
        teamLogo.setDark(goalie.getTeamLogoDark());
        team.setTeamLogo(teamLogo);

        player.setTeam(team);
        dto.setPlayer(player);

        // Shot Location Summary
        dto.setShotLocationSummary(
                goalie.getShotLocationSummary().stream().map(s -> {
                    ShotSummaryDTO sd = new ShotSummaryDTO();
                    sd.setLocationCode(s.getLocationCode());
                    sd.setGoalsAgainst(s.getGoalsAgainst());
                    sd.setGoalsAgainstPercentile(s.getGoalsAgainstPercentile());
                    sd.setGoalsAgainstLeagueAvg(s.getGoalsAgainstLeagueAvg());
                    sd.setSaves(s.getSaves());
                    sd.setSavesPercentile(s.getSavesPercentile());
                    sd.setSavesLeagueAvg(s.getSavesLeagueAvg());
                    sd.setSavePctg(s.getSavePctg());
                    sd.setSavePctgPercentile(s.getSavePctgPercentile());
                    sd.setSavePctgLeagueAvg(s.getSavePctgLeagueAvg());
                    return sd;
                }).collect(Collectors.toList())
        );

        // Shot Location Details
        dto.setShotLocationDetails(
                goalie.getShotLocationDetails().stream().map(d -> {
                    ShotDetailDTO dd = new ShotDetailDTO();
                    dd.setArea(d.getArea());
                    dd.setSaves(d.getSaves());
                    dd.setSavesPercentile(d.getSavesPercentile());
                    dd.setSavePctg(d.getSavePctg());
                    dd.setSavePctgPercentile(d.getSavePctgPercentile());
                    return dd;
                }).collect(Collectors.toList())
        );

        return dto;
    }
}
