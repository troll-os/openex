package io.openex.helper;

import io.openex.database.model.*;
import io.openex.database.repository.*;
import io.openex.execution.ExecutableInject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.openex.database.model.Exercise.STATUS.RUNNING;
import static io.openex.injects.email.EmailContract.EMAIL_DEFAULT;
import static io.openex.injects.email.EmailContract.TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class InjectHelperTest {

    public static final String USER_EMAIL = "test@gmail.com";
    @Autowired
    private InjectHelper injectHelper;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private ExerciseTeamUserRepository exerciseTeamUserRepository;

    @Autowired
    private InjectRepository injectRepository;

    @Autowired
    private UserRepository userRepository;

    @Disabled
    @DisplayName("Retrieve simple inject to run")
    @Test
    void injectsToRunTest() {
        // -- PREPARE --
        Exercise exercise = new Exercise();
        exercise.setName("Exercice name");
        exercise.setStart(Instant.now());
        exercise.setReplyTo("test@test.com");
        exercise.setStatus(RUNNING);
        Exercise exerciseSaved = this.exerciseRepository.save(exercise);
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(exerciseSaved);
        User user = new User();
        user.setEmail(USER_EMAIL);
        this.userRepository.save(user);

        Team team = new Team();
        team.setName("My team");
        team.setExercises(exercises);
        team.setUsers(List.of(user));
        this.teamRepository.save(team);

        ExerciseTeamUser exerciseTeamUser = new ExerciseTeamUser();
        exerciseTeamUser.setExercise(exercise);
        exerciseTeamUser.setTeam(team);
        exerciseTeamUser.setUser(user);
        this.exerciseTeamUserRepository.save(exerciseTeamUser);

        // Executable Inject
        Inject inject = new Inject();
        inject.setTitle("Test inject");
        inject.setType(TYPE);
        inject.setContract(EMAIL_DEFAULT);
        inject.setEnabled(true);
        inject.setExercise(exerciseSaved);
        inject.setTeams(List.of(team));
        inject.setDependsDuration(0L);
        this.injectRepository.save(inject);

        // -- EXECUTE --
        List<ExecutableInject> executableInjects = this.injectHelper.getInjectsToRun();

        // -- ASSERT --
        assertFalse(executableInjects.isEmpty());
        ExecutableInject executableInject = executableInjects.get(0);
        assertEquals(1, executableInject.getTeamSize());
        assertEquals(1, executableInject.getContextUser().size());
        assertEquals(USER_EMAIL, executableInject.getContextUser().get(0).getUser().getEmail());

        // -- CLEAN -
        this.exerciseRepository.delete(exercise);
        this.teamRepository.delete(team);
        this.userRepository.delete(user);
        this.exerciseTeamUserRepository.delete(exerciseTeamUser);
        this.injectRepository.delete(inject);
    }

}
