package com.creasevision.api.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "goalies")
public class Goalie {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private String headshot;
    private Integer sweaterNumber;
    private String teamAbbrev;
    private String teamName;
    private String teamLogoLight;
    private String teamLogoDark;

    private Integer wins;
    private Integer losses;
    private Integer overtimeLosses;
    private Integer gamesPlayed;
    private Double goalsAgainstAvg;
    private Double savePctg;

    @OneToMany(mappedBy = "goalie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShotLocationSummary> shotLocationSummary;

    @OneToMany(mappedBy = "goalie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShotLocationDetail> shotLocationDetails;
}