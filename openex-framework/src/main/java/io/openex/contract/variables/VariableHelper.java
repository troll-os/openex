package io.openex.contract.variables;

import io.openex.contract.ContractVariable;
import io.openex.database.model.Variable.VariableType;

import java.util.List;

import static io.openex.contract.ContractCardinality.Multiple;
import static io.openex.contract.ContractCardinality.One;
import static io.openex.contract.ContractVariable.variable;

public class VariableHelper {

  public static final String USER = "user";
  public static final String EXERCISE = "exercise";
  public static final String TEAMS = "teams";
  public static final String COMCHECK = "comcheck";
  public static final String PLAYER_URI = "player_uri";
  public static final String CHALLENGES_URI = "challenges_uri";
  public static final String SCOREBOARD_URI = "scoreboard_uri";
  public static final String LESSONS_URI = "lessons_uri";

  public static final ContractVariable userVariable = variable(USER, "User that will receive the injection",
      VariableType.String, One, List.of(
          variable(USER + ".id", "Id of the user in the platform", VariableType.String, One),
          variable(USER + ".email", "Email of the user", VariableType.String, One),
          variable(USER + ".firstname", "Firstname of the user", VariableType.String, One),
          variable(USER + ".lastname", "Lastname of the user", VariableType.String, One),
          variable(USER + ".lang", "Lang of the user", VariableType.String, One)
      ));

  public static final ContractVariable exerciceVariable = variable(EXERCISE, "Exercise of the current injection",
      VariableType.Object, One, List.of(
          variable(EXERCISE + ".id", "Id of the user in the platform", VariableType.String, One),
          variable(EXERCISE + ".name", "Name of the exercise", VariableType.String, One),
          variable(EXERCISE + ".description", "Description of the exercise", VariableType.String, One)
      ));

  public static final ContractVariable teamVariable = variable(TEAMS, "List of team name for the injection",
      VariableType.String, Multiple);

  public static final List<ContractVariable> uriVariables = List.of(
      variable(PLAYER_URI, "Player interface platform link", VariableType.String, One),
      variable(CHALLENGES_URI, "Challenges interface platform link", VariableType.String, One),
      variable(SCOREBOARD_URI, "Scoreboard interface platform link", VariableType.String, One),
      variable(LESSONS_URI, "Lessons learned interface platform link", VariableType.String, One)
  );

}
