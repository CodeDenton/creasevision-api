package com.creasevision.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.creasevision.api.model.Goalie;

@Repository
public interface GoalieRepository extends JpaRepository<Goalie, Long> {
}