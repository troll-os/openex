/* eslint-disable no-underscore-dangle,max-len */
import { schema } from 'normalizr';
import * as R from 'ramda';

export const document = new schema.Entity(
  'documents',
  {},
  { idAttribute: 'document_id' },
);
export const arrayOfDocuments = new schema.Array(document);

export const tag = new schema.Entity('tags', {}, { idAttribute: 'tag_id' });
export const arrayOfTags = new schema.Array(tag);

export const injectType = new schema.Entity(
  'inject_types',
  {},
  { idAttribute: 'contract_id' },
);
export const arrayOfInjectTypes = new schema.Array(injectType);

export const injectStatus = new schema.Entity(
  'inject_statuses',
  {},
  { idAttribute: 'status_id' },
);
export const arrayOfInjectStatuses = new schema.Array(injectStatus);

export const parameters = new schema.Entity(
  'parameters',
  {},
  { idAttribute: 'setting_key' },
);

export const arrayOfParameters = new schema.Array(parameters);

export const token = new schema.Entity(
  'tokens',
  {},
  { idAttribute: 'token_id' },
);
export const arrayOfTokens = new schema.Array(token);

export const organization = new schema.Entity(
  'organizations',
  {},
  { idAttribute: 'organization_id' },
);
export const arrayOfOrganizations = new schema.Array(organization);

export const group = new schema.Entity(
  'groups',
  {},
  { idAttribute: 'group_id' },
);
export const arrayOfGroups = new schema.Array(group);

export const grant = new schema.Entity(
  'grants',
  {},
  { idAttribute: 'grant_id' },
);
export const arrayOfGrants = new schema.Array(grant);

export const user = new schema.Entity('users', {}, { idAttribute: 'user_id' });
export const arrayOfUsers = new schema.Array(user);

export const exercise = new schema.Entity(
  'exercises',
  {},
  { idAttribute: 'exercise_id' },
);
export const arrayOfExercises = new schema.Array(exercise);

export const objective = new schema.Entity(
  'objectives',
  {},
  { idAttribute: 'objective_id' },
);
export const arrayOfObjectives = new schema.Array(objective);

export const evaluation = new schema.Entity(
  'evaluations',
  {},
  { idAttribute: 'evaluation_id' },
);
export const arrayOfEvaluations = new schema.Array(evaluation);

export const comcheck = new schema.Entity(
  'comchecks',
  {},
  { idAttribute: 'comcheck_id' },
);
export const arrayOfComchecks = new schema.Array(comcheck);

export const comcheckStatus = new schema.Entity(
  'comcheckstatuses',
  {},
  { idAttribute: 'comcheckstatus_id' },
);
export const arrayOfComcheckStatuses = new schema.Array(comcheckStatus);

export const dryrun = new schema.Entity(
  'dryruns',
  {},
  { idAttribute: 'dryrun_id' },
);
export const arrayOfDryruns = new schema.Array(dryrun);

export const dryinject = new schema.Entity(
  'dryinjects',
  {},
  { idAttribute: 'dryinject_id' },
);
export const arrayOfDryinjects = new schema.Array(dryinject);

export const team = new schema.Entity(
  'teams',
  {},
  { idAttribute: 'team_id' },
);
export const arrayOfTeams = new schema.Array(team);

export const inject = new schema.Entity(
  'injects',
  {},
  { idAttribute: 'inject_id' },
);
export const arrayOfInjects = new schema.Array(inject);

export const communication = new schema.Entity(
  'communications',
  {},
  { idAttribute: 'communication_id' },
);
export const arrayOfCommunications = new schema.Array(communication);

export const statistics = new schema.Entity(
  'statistics',
  {},
  { idAttribute: 'platform_id' },
);

export const log = new schema.Entity('logs', {}, { idAttribute: 'log_id' });
export const arrayOfLogs = new schema.Array(log);

export const channelReader = new schema.Entity(
  'channelreaders',
  {},
  { idAttribute: 'channel_id' },
);
export const challengesReader = new schema.Entity(
  'challengesreaders',
  {},
  { idAttribute: 'exercise_id' },
);
export const injectexpectation = new schema.Entity(
  'injectexpectations',
  {},
  { idAttribute: 'injectexpectation_id' },
);
export const arrayOfInjectexpectations = new schema.Array(injectexpectation);

export const lessonsTemplate = new schema.Entity(
  'lessonstemplates',
  {},
  { idAttribute: 'lessonstemplate_id' },
);
export const arrayOfLessonsTemplates = new schema.Array(lessonsTemplate);

export const lessonsTemplateCategory = new schema.Entity(
  'lessonstemplatecategorys',
  {},
  { idAttribute: 'lessonstemplatecategory_id' },
);
export const arrayOfLessonsTemplateCategories = new schema.Array(
  lessonsTemplateCategory,
);

export const lessonsTemplateQuestion = new schema.Entity(
  'lessonstemplatequestions',
  {},
  { idAttribute: 'lessonstemplatequestion_id' },
);
export const arrayOfLessonsTemplateQuestions = new schema.Array(
  lessonsTemplateQuestion,
);

export const lessonsCategory = new schema.Entity(
  'lessonscategorys',
  {},
  { idAttribute: 'lessonscategory_id' },
);
export const arrayOfLessonsCategories = new schema.Array(lessonsCategory);

export const lessonsQuestion = new schema.Entity(
  'lessonsquestions',
  {},
  { idAttribute: 'lessonsquestion_id' },
);
export const arrayOfLessonsQuestions = new schema.Array(lessonsQuestion);

export const lessonsAnswer = new schema.Entity(
  'lessonsanswers',
  {},
  { idAttribute: 'lessonsanswer_id' },
);
export const arrayOfLessonsAnswers = new schema.Array(lessonsAnswer);

export const report = new schema.Entity(
  'reports',
  {},
  { idAttribute: 'report_id' },
);
export const arrayOfReports = new schema.Array(report);

export const variable = new schema.Entity(
  'variables',
  {},
  { idAttribute: 'variable_id' },
);
export const arrayOfVariables = new schema.Array(variable);

export const killChainPhase = new schema.Entity(
  'killchainphases',
  {},
  { idAttribute: 'phase_id' },
);
export const arrayOfKillChainPhases = new schema.Array(killChainPhase);

export const attackPattern = new schema.Entity(
  'attackpatterns',
  {},
  { idAttribute: 'attack_pattern_id' },
);
export const arrayOfAttackPatterns = new schema.Array(attackPattern);

token.define({ token_user: user });
user.define({ user_organization: organization });

const maps = (key, state) => state.referential.entities[key].asMutable({ deep: true });
const entities = (key, state) => Object.values(maps(key, state));
const entity = (id, key, state) => state.referential.entities[key][id]?.asMutable({ deep: true });
const me = (state) => state.referential.entities.users[R.path(['logged', 'user'], state.app)];
const contractImages = (state) => state.app.contractImages;

export const storeHelper = (state) => ({
  logged: () => state.app.logged,
  getMe: () => me(state),
  getMeTokens: () => entities('tokens', state).filter(
    (t) => t.token_user === me(state)?.user_id,
  ),
  getStatistics: () => state.referential.entities.statistics?.openex,
  // exercises
  getExercises: () => entities('exercises', state),
  getExercisesMap: () => maps('exercises', state),
  getExercise: (id) => entity(id, 'exercises', state),
  getExerciseDryruns: (id) => entities('dryruns', state).filter((i) => i.dryrun_exercise === id),
  getExerciseComchecks: (id) => entities('comchecks', state).filter((i) => i.comcheck_exercise === id),
  getExerciseTeams: (id) => entities('teams', state).filter((i) => i.team_exercises.includes(id)),
  getExerciseVariables: (id) => entities('variables', state).filter((i) => i.variable_exercise === id),
  getExerciseArticles: (id) => entities('articles', state).filter((i) => i.article_exercise === id),
  getExerciseInjects: (id) => entities('injects', state).filter((i) => i.inject_exercise === id),
  getExerciseCommunications: (id) => entities('communications', state).filter(
    (i) => i.communication_exercise === id,
  ),
  getExerciseTechnicalInjectsPerType: (id) => {
    const typesWithNoTeams = R.uniq(
      entities('inject_types', state)
        .map((t) => ({
          type: t.config.type,
          hasTeams:
            t.fields.filter((f) => f.name === 'teams').length > 0,
        }))
        .filter((t) => !t.hasTeams)
        .map((t) => t.type),
    );
    return R.mergeAll(
      typesWithNoTeams.map((t) => ({
        [t]: entities('injects', state).filter(
          (i) => i.inject_type === t && i.inject_exercise === id,
        ),
      })),
    );
  },
  getExerciseObjectives: (id) => entities('objectives', state).filter((o) => o.objective_exercise === id),
  getExerciseLogs: (id) => entities('logs', state).filter((l) => l.log_exercise === id),
  getExerciseLessonsCategories: (id) => entities('lessonscategorys', state).filter(
    (l) => l.lessons_category_exercise === id,
  ),
  getExerciseLessonsQuestions: (id) => entities('lessonsquestions', state).filter(
    (l) => l.lessons_question_exercise === id,
  ),
  getExerciseLessonsAnswers: (exerciseId) => entities('lessonsanswers', state).filter(
    (l) => l.lessons_answer_exercise === exerciseId,
  ),
  getExerciseUserLessonsAnswers: (exerciseId, userId) => entities('lessonsanswers', state).filter(
    (l) => l.lessons_answer_exercise === exerciseId
      && l.lessons_answer_user === userId,
  ),
  getExerciseReports: (exerciseId) => entities('reports', state).filter((l) => l.report_exercise === exerciseId),
  // report
  getReport: (id) => entity(id, 'reports', state),
  // dryrun
  getDryrun: (id) => entity(id, 'dryruns', state),
  getDryrunInjects: (id) => entities('dryinjects', state).filter((i) => i.dryinject_dryrun === id),
  getDryrunUsers: (id) => entities('users', state).filter((i) => (entity(id, 'dryruns', state) || {}).dryrun_users?.includes(i.user_id)),
  // comcheck
  getComcheck: (id) => entity(id, 'comchecks', state),
  getComcheckStatus: (id) => entity(id, 'comcheckstatuses', state),
  getComcheckStatuses: (id) => entities('comcheckstatuses', state).filter(
    (i) => i.comcheckstatus_comcheck === id,
  ),
  getChannelReader: (id) => entity(id, 'channelreaders', state),
  getChallengesReader: (id) => entity(id, 'challengesreaders', state),
  // users
  getUsers: () => entities('users', state),
  getGroups: () => entities('groups', state),
  getUsersMap: () => maps('users', state),
  getOrganizations: () => entities('organizations', state),
  getOrganizationsMap: () => maps('organizations', state),
  // objectives
  getObjective: (id) => entity(id, 'objectives', state),
  getObjectiveEvaluations: (id) => entities('evaluations', state).filter((e) => e.evaluation_objective === id),
  // tags
  getTag: (id) => entity(id, 'tags', state),
  getTags: () => entities('tags', state),
  getTagsMap: () => maps('tags', state),
  // injects
  getInject: (id) => entity(id, 'injects', state),
  getInjectsMap: () => maps('injects', state),
  getInjectTypes: () => entities('inject_types', state),
  getInjectTypesMap: () => maps('inject_types', state),
  getInjectTypesMapByType: () => R.indexBy(R.path(['config', 'type']), entities('inject_types', state)),
  getInjectTypesWithNoTeams: () => R.uniq(
    entities('inject_types', state)
      .map((t) => ({
        hasTeams:
          t.fields.filter((f) => f.key === 'teams').length > 0,
        ...t,
      }))
      .filter((t) => !t.hasTeams)
      .map((t) => t.config.type),
  ),
  getNextInjects: () => {
    const sortFn = (a, b) => new Date(a.inject_date).getTime() - new Date(b.inject_date).getTime();
    const injects = entities('injects', state).filter(
      (i) => i.inject_date !== null && i.inject_status === null,
    );
    return R.take(6, R.sort(sortFn, injects));
  },
  getInjectCommunications: (id) => entities('communications', state).filter(
    (i) => i.communication_inject === id,
  ),
  // injectexpectation
  getInjectExpectations: () => entities('injectexpectations', state),
  getExerciseInjectExpectations: (id) => entities('injectexpectations', state).filter(
    (i) => i.inject_expectation_exercise === id,
  ),
  getInjectExpectationsMap: () => maps('injectexpectations', state),
  // documents
  getDocuments: () => entities('documents', state),
  getDocumentsMap: () => maps('documents', state),
  // teams
  getTeam: (id) => entity(id, 'teams', state),
  getTeamUsers: (id) => entities('users', state).filter((u) => (entity(id, 'teams', state) || {}).team_users?.includes(
    u.user_id,
  )),
  getTeamInjects: (id) => entities('injects', state).filter((i) => (entity(id, 'teams', state) || {}).team_injects?.includes(
    i.inject_id,
  )),
  getTeams: () => entities('teams', state).filter((i) => !i.team_contextual),
  getTeamsMap: () => maps('teams', state),
  getSettings: () => {
    return R.mergeAll(
      Object.entries(state.referential.entities.parameters ?? {}).map(
        ([k, v]) => ({ [k]: v.setting_value }),
      ),
    );
  },
  // kill chain phases
  getKillChainPhase: (id) => entity(id, 'killchainphases', state),
  getKillChainPhases: () => entities('killchainphases', state),
  getKillChainPhasesMap: () => maps('killchainphases', state),
  // attack patterns
  getAttackPattern: (id) => entity(id, 'attackpatterns', state),
  getAttackPatterns: () => entities('attackpatterns', state),
  // channels
  getChannels: () => entities('channels', state),
  getChannel: (id) => entity(id, 'channels', state),
  getChannelsMap: () => maps('channels', state),
  // articles
  getArticles: () => entities('articles', state),
  getArticle: (id) => entity(id, 'articles', state),
  getArticlesMap: () => maps('articles', state),
  // challenges
  getChallenges: () => entities('challenges', state),
  getExerciseChallenges: (id) => entities('challenges', state).filter((c) => c.challenge_exercises.includes(id)),
  getChallengesMap: () => maps('challenges', state),
  // lessons templates
  getLessonsTemplate: (id) => entity(id, 'lessonstemplates', state),
  getLessonsTemplates: () => entities('lessonstemplates', state),
  getLessonsTemplatesMap: () => maps('lessonstemplates', state),
  getLessonsTemplateCategories: (id) => entities('lessonstemplatecategorys', state).filter(
    (c) => c.lessons_template_category_template === id,
  ),
  getLessonsTemplateQuestions: () => entities('lessonstemplatequestions', state),
  getLessonsTemplateQuestionsMap: () => maps('lessonstemplatequestions', state),
  getLessonsTemplateCategoryQuestions: (id) => entities('lessonstemplatequestions', state).filter(
    (c) => c.lessons_template_question_category === id,
  ),
  // assets
  getEndpoints: () => entities('endpoints', state),
  getEndpointsMap: () => maps('endpoints', state),
  // asset groups
  getAssetGroups: () => entities('asset_groups', state),
  getAssetGroupMaps: () => maps('asset_groups', state),
  getAssetGroup: (id) => entity(id, 'asset_groups', state),
  // contracts
  getContractImages: () => contractImages(state),
});
