package io.openex.rest.team;

import io.openex.config.OpenexPrincipal;
import io.openex.database.model.Organization;
import io.openex.database.model.Team;
import io.openex.database.model.User;
import io.openex.database.repository.*;
import io.openex.rest.helper.RestBehavior;
import io.openex.rest.team.form.TeamCreateInput;
import io.openex.rest.team.form.TeamUpdateInput;
import io.openex.rest.team.form.UpdateUsersTeamInput;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.openex.config.SessionHelper.currentUser;
import static io.openex.database.model.User.ROLE_USER;
import static io.openex.helper.DatabaseHelper.updateRelation;
import static io.openex.helper.StreamHelper.fromIterable;
import static java.time.Instant.now;

@RestController
@Secured(ROLE_USER)
public class TeamApi extends RestBehavior {
    private ExerciseRepository exerciseRepository;
    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;
    private TagRepository tagRepository;

    @Autowired
    public void setExerciseRepository(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Autowired
    public void setTeamRepository(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setOrganizationRepository(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Autowired
    public void setTagRepository(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping("/api/teams")
    @PreAuthorize("isObserver()")
    public Iterable<Team> getTeams() {
        List<Team> teams;
        OpenexPrincipal currentUser = currentUser();
        if (currentUser.isAdmin()) {
            teams = fromIterable(teamRepository.findAll());
        } else {
            User local = userRepository.findById(currentUser.getId()).orElseThrow();
            List<String> organizationIds = local.getGroups().stream()
                    .flatMap(group -> group.getOrganizations().stream())
                    .map(Organization::getId)
                    .toList();
            teams = teamRepository.teamsAccessibleFromOrganizations(organizationIds);
        }
        return teams;
    }

    @GetMapping("/api/teams/{teamId}")
    @PreAuthorize("isObserver()")
    public Team getTeam(@PathVariable String teamId) {
        return teamRepository.findById(teamId).orElseThrow();
    }

    @GetMapping("/api/teams/{teamId}/players")
    @PreAuthorize("isObserver()")
    public Iterable<User> getTeamPlayers(@PathVariable String teamId) {
        return teamRepository.findById(teamId).orElseThrow().getUsers();
    }

    @PostMapping("/api/teams")
    @PreAuthorize("isPlanner()")
    public Team createTeam(@Valid @RequestBody TeamCreateInput input) {
        if (input.getContextual() && input.getExerciseIds().toArray().length > 1) {
            throw new UnsupportedOperationException("Contextual team can only be associated to one exercise");
        }
        Team team = new Team();
        team.setUpdateAttributes(input);
        team.setOrganization(updateRelation(input.getOrganizationId(), team.getOrganization(), organizationRepository));
        team.setTags(fromIterable(tagRepository.findAllById(input.getTagIds())));
        team.setExercises(fromIterable(exerciseRepository.findAllById(input.getExerciseIds())));
        return teamRepository.save(team);
    }

    @DeleteMapping("/api/teams/{teamId}")
    @PreAuthorize("isPlanner()")
    public void deleteTeam(@PathVariable String teamId) {
        teamRepository.deleteById(teamId);
    }

    @PutMapping("/api/teams/{teamId}")
    @PreAuthorize("isPlanner()")
    public Team updateTeam(@PathVariable String teamId, @Valid @RequestBody TeamUpdateInput input) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        team.setUpdateAttributes(input);
        team.setUpdatedAt(now());
        team.setTags(fromIterable(tagRepository.findAllById(input.getTagIds())));
        team.setOrganization(updateRelation(input.getOrganizationId(), team.getOrganization(), organizationRepository));
        return teamRepository.save(team);
    }

    @PutMapping("/api/teams/{teamId}/players")
    @PreAuthorize("isPlanner()")
    public Team updateTeamUsers(@PathVariable String teamId, @Valid @RequestBody UpdateUsersTeamInput input) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        Iterable<User> teamUsers = userRepository.findAllById(input.getUserIds());
        team.setUsers(fromIterable(teamUsers));
        return teamRepository.save(team);
    }
}
