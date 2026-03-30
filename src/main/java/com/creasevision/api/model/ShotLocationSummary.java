package com.creasevision.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "shot_location_summary")
public class ShotLocationSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "goalie_id")
    private Goalie goalie;

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