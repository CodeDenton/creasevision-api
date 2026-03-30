package com.creasevision.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.creasevision.api.dto.GoalieDTO;
import com.creasevision.api.repository.GoalieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalieService {

    private final GoalieRepository goalieRepository;

    public List<GoalieDTO> getAllGoalies() {
        return goalieRepository.findAll()
                .stream()
                .map(GoalieDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public GoalieDTO getGoalieById(Long id) {
        return goalieRepository.findById(id)
                .map(GoalieDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Goalie not found with id: " + id));
    }
}