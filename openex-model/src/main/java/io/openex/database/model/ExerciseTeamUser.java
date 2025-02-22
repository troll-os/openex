package io.openex.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.openex.helper.MonoIdDeserializer;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "exercises_teams_users")
public class ExerciseTeamUser {
    @EmbeddedId
    @JsonIgnore
    private ExerciseTeamUserId compositeId = new ExerciseTeamUserId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("exerciseId")
    @JoinColumn(name = "exercise_id")
    @JsonProperty("exercise_id")
    @JsonSerialize(using = MonoIdDeserializer.class)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    @JsonProperty("team_id")
    @JsonSerialize(using = MonoIdDeserializer.class)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @JsonProperty("user_id")
    @JsonSerialize(using = MonoIdDeserializer.class)
    private User user;

    public ExerciseTeamUserId getCompositeId() {
        return compositeId;
    }

    public void setCompositeId(ExerciseTeamUserId compositeId) {
        this.compositeId = compositeId;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExerciseTeamUser that = (ExerciseTeamUser) o;
        return compositeId.equals(that.compositeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compositeId);
    }
}
