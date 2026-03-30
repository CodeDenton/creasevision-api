package com.creasevision.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creasevision.api.dto.GoalieDTO;
import com.creasevision.api.service.GoalieService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/goalies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class GoalieController {

    private final GoalieService goalieService;

    @GetMapping
    public List<GoalieDTO> getAllGoalies() {
        return goalieService.getAllGoalies();
    }

    @GetMapping("/{id}")
    public GoalieDTO getGoalieById(@PathVariable Long id) {
        return goalieService.getGoalieById(id);
    }
}