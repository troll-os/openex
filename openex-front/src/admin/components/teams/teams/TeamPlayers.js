import React, { Component } from 'react';
import * as PropTypes from 'prop-types';
import * as R from 'ramda';
import withStyles from '@mui/styles/withStyles';
import { List, ListItem, ListItemIcon, ListItemText, ListItemSecondaryAction, IconButton, Typography } from '@mui/material';
import { connect } from 'react-redux';
import { ArrowDropDownOutlined, ArrowDropUpOutlined, CloseRounded, EmailOutlined, KeyOutlined, PersonOutlined, SmartphoneOutlined } from '@mui/icons-material';
import inject18n from '../../../../components/i18n';
import { fetchTeamPlayers } from '../../../../actions/Team';
import { fetchOrganizations } from '../../../../actions/Organization';
import { addExerciseTeamPlayers, removeExerciseTeamPlayers } from '../../../../actions/Exercise';
import SearchFilter from '../../../../components/SearchFilter';
import TagsFilter from '../../../../components/TagsFilter';
import ItemTags from '../../../../components/ItemTags';
import PlayerPopover from '../players/PlayerPopover';
import { storeHelper } from '../../../../actions/Schema';
import TeamAddPlayers from './TeamAddPlayers';
import ItemBoolean from '../../../../components/ItemBoolean';
import { isExerciseUpdatable } from '../../../../utils/Exercise';

const styles = (theme) => ({
  header: {
    backgroundColor: theme.palette.background.nav,
    padding: '20px 20px 20px 60px',
  },
  closeButton: {
    position: 'absolute',
    top: 12,
    left: 5,
    color: 'inherit',
  },
  title: {
    float: 'left',
  },
  search: {
    float: 'right',
    width: 200,
    marginRight: 20,
  },
  tags: {
    float: 'right',
  },
  parameters: {
    float: 'right',
    marginTop: -8,
  },
  container: {
    marginTop: 10,
  },
  itemHead: {
    textTransform: 'uppercase',
    cursor: 'pointer',
  },
  item: {
    height: 50,
  },
  bodyItem: {
    height: '100%',
    fontSize: 13,
  },
  itemIcon: {
    color: theme.palette.primary.main,
  },
  inputLabel: {
    float: 'left',
  },
  sortIcon: {
    float: 'left',
    margin: '-5px 0 0 15px',
  },
  icon: {
    marginRight: 10,
  },
});

const inlineStylesHeaders = {
  iconSort: {
    position: 'absolute',
    margin: '0 0 0 5px',
    padding: 0,
    top: '0px',
  },
  user_enabled: {
    float: 'left',
    width: '12%',
    fontSize: 12,
    fontWeight: '700',
  },
  user_email: {
    float: 'left',
    width: '30%',
    fontSize: 12,
    fontWeight: '700',
  },
  user_options: {
    float: 'left',
    width: '15%',
    fontSize: 12,
    fontWeight: '700',
  },
  user_organization: {
    float: 'left',
    width: '20%',
    fontSize: 12,
    fontWeight: '700',
  },
  user_tags: {
    float: 'left',
    width: '23%',
    fontSize: 12,
    fontWeight: '700',
  },
};

const inlineStyles = {
  user_enabled: {
    float: 'left',
    width: '12%',
    height: 20,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  user_email: {
    float: 'left',
    width: '30%',
    height: 20,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  user_options: {
    float: 'left',
    width: '15%',
    height: 20,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  user_organization: {
    float: 'left',
    width: '20%',
    height: 20,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  user_tags: {
    float: 'left',
    width: '23%',
    height: 20,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
};

class TeamsPlayers extends Component {
  constructor(props) {
    super(props);
    this.state = {
      sortBy: 'user_email',
      orderAsc: true,
      keyword: '',
      tags: [],
    };
  }

  componentDidMount() {
    const { teamId } = this.props;
    this.props.fetchOrganizations();
    this.props.fetchTeamPlayers(teamId);
  }

  handleSearch(value) {
    this.setState({ keyword: value });
  }

  handleAddTag(value) {
    if (value) {
      this.setState({ tags: R.uniq(R.append(value, this.state.tags)) });
    }
  }

  handleRemoveTag(value) {
    this.setState({ tags: R.filter((n) => n.id !== value, this.state.tags) });
  }

  reverseBy(field) {
    this.setState({ sortBy: field, orderAsc: !this.state.orderAsc });
  }

  sortHeader(field, label, isSortable) {
    const { t } = this.props;
    const { orderAsc, sortBy } = this.state;
    const sortComponent = orderAsc ? (
      <ArrowDropDownOutlined style={inlineStylesHeaders.iconSort} />
    ) : (
      <ArrowDropUpOutlined style={inlineStylesHeaders.iconSort} />
    );
    if (isSortable) {
      return (
        <div
          style={inlineStylesHeaders[field]}
          onClick={this.reverseBy.bind(this, field)}
        >
          <span>{t(label)}</span>
          {sortBy === field ? sortComponent : ''}
        </div>
      );
    }
    return (
      <div style={inlineStylesHeaders[field]}>
        <span>{t(label)}</span>
      </div>
    );
  }

  handleToggleUser(userId, enabled) {
    if (!enabled) {
      this.props.addExerciseTeamPlayers(
        this.props.exerciseId,
        this.props.teamId,
        {
          exercise_team_players: [userId],
        },
      );
    } else {
      this.props.removeExerciseTeamPlayers(
        this.props.exerciseId,
        this.props.teamId,
        {
          exercise_team_players: [userId],
        },
      );
    }
  }

  render() {
    const {
      classes,
      users,
      handleClose,
      team,
      organizationsMap,
      exerciseId,
      exercise,
      teamId,
      t,
      isPlanner,
    } = this.props;
    const isWritePermission = exercise ? isExerciseUpdatable(exercise) : isPlanner;
    const { keyword, sortBy, orderAsc, tags } = this.state;
    const filterByKeyword = (n) => keyword === ''
      || (n.user_email || '').toLowerCase().indexOf(keyword.toLowerCase()) !== -1
      || (n.user_firstname || '').toLowerCase().indexOf(keyword.toLowerCase()) !== -1
      || (n.user_lastname || '').toLowerCase().indexOf(keyword.toLowerCase()) !== -1
      || (n.user_phone || '').toLowerCase().indexOf(keyword.toLowerCase()) !== -1
      || (n.user_organization || '').toLowerCase().indexOf(keyword.toLowerCase()) !== -1;
    const sort = R.sortWith(
      orderAsc ? [R.ascend(R.prop(sortBy))] : [R.descend(R.prop(sortBy))],
    );
    const sortedUsers = R.pipe(
      R.filter(
        (n) => tags.length === 0
          || R.any(
            (filter) => R.includes(filter, n.user_tags),
            R.pluck('id', tags),
          ),
      ),
      R.filter(filterByKeyword),
      R.map((n) => ({
        user_enabled: exercise && exercise.exercise_teams_users.filter((o) => o.exercise_id === exerciseId && o.team_id === teamId && n.user_id === o.user_id).length > 0,
        ...n,
      })),
      sort,
    )(users);
    return (
      <>
        <div className={classes.header}>
          <IconButton
            aria-label="Close"
            className={classes.closeButton}
            onClick={handleClose.bind(this)}
            size="large"
            color="primary"
          >
            <CloseRounded fontSize="small" color="primary" />
          </IconButton>
          <Typography variant="h6" classes={{ root: classes.title }}>
            {R.propOr('-', 'team_name', team)}
          </Typography>
          <div className={classes.parameters}>
            <div className={classes.tags}>
              <TagsFilter
                onAddTag={this.handleAddTag.bind(this)}
                onRemoveTag={this.handleRemoveTag.bind(this)}
                currentTags={tags}
                thin={true}
              />
            </div>
            <div className={classes.search}>
              <SearchFilter
                fullWidth={true}
                onChange={this.handleSearch.bind(this)}
                keyword={keyword}
              />
            </div>
          </div>
          <div className="clearfix" />
        </div>
        <List classes={{ root: classes.container }}>
          <ListItem
            classes={{ root: classes.itemHead }}
            divider={false}
            style={{ paddingTop: 0 }}
          >
            <ListItemIcon>
              <span
                style={{
                  padding: '0 8px 0 8px',
                  fontWeight: 700,
                  fontSize: 12,
                }}
              >
                &nbsp;
              </span>
            </ListItemIcon>
            <ListItemText
              primary={
                <>
                  {exerciseId && this.sortHeader('user_enabled', 'Enabled', true)}
                  {this.sortHeader('user_email', 'Email address', true)}
                  {this.sortHeader('user_options', 'Options', false)}
                  {this.sortHeader('user_organization', 'Organization', true)}
                  {this.sortHeader('user_tags', 'Tags', true)}
                </>
              }
            />
            <ListItemSecondaryAction> &nbsp; </ListItemSecondaryAction>
          </ListItem>
          {sortedUsers.map((user) => (
            <ListItem
              key={user.user_id}
              classes={{ root: classes.item }}
              divider={true}
            >
              <ListItemIcon>
                <PersonOutlined />
              </ListItemIcon>
              <ListItemText
                primary={
                  <>
                    {exerciseId && (
                    <div className={classes.bodyItem} style={inlineStyles.user_enabled}>
                      <ItemBoolean
                        status={user.user_enabled}
                        label={user.user_enabled ? t('Enabled') : t('Disabled')}
                        onClick={this.handleToggleUser.bind(this, user.user_id, user.user_enabled)}
                        variant="inList"
                      />
                    </div>
                    )}
                    <div
                      className={classes.bodyItem}
                      style={inlineStyles.user_email}
                    >
                      {user.user_email}
                    </div>
                    <div
                      className={classes.bodyItem}
                      style={inlineStyles.user_options}
                    >
                      {R.isNil(user.user_email)
                      || R.isEmpty(user.user_email) ? (
                        <EmailOutlined
                          color="warning"
                          fontSize="small"
                          className={classes.icon}
                        />
                        ) : (
                          <EmailOutlined
                            color="success"
                            fontSize="small"
                            className={classes.icon}
                          />
                        )}
                      {R.isNil(user.user_pgp_key)
                      || R.isEmpty(user.user_pgp_key) ? (
                        <KeyOutlined
                          color="warning"
                          fontSize="small"
                          className={classes.icon}
                        />
                        ) : (
                          <KeyOutlined
                            color="success"
                            fontSize="small"
                            className={classes.icon}
                          />
                        )}
                      {R.isNil(user.user_phone)
                      || R.isEmpty(user.user_phone) ? (
                        <SmartphoneOutlined
                          color="warning"
                          fontSize="small"
                          className={classes.icon}
                        />
                        ) : (
                          <SmartphoneOutlined
                            color="success"
                            fontSize="small"
                            className={classes.icon}
                          />
                        )}
                    </div>
                    <div
                      className={classes.bodyItem}
                      style={inlineStyles.user_organization}
                    >
                      {
                        organizationsMap[user.user_organization]
                          ?.organization_name
                      }
                    </div>
                    <div
                      className={classes.bodyItem}
                      style={inlineStyles.user_tags}
                    >
                      <ItemTags variant="list" tags={user.user_tags} />
                    </div>
                  </>
                }
              />
              <ListItemSecondaryAction>
                {isWritePermission
                  ? (<PlayerPopover
                      user={user}
                      teamId={teamId}
                      teamUsersIds={users.map((u) => u.user_id)}
                     />)
                  : <span> &nbsp; </span>
                }
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>
        {isWritePermission
            && (<TeamAddPlayers
              teamId={teamId}
              teamUsersIds={users.map((u) => u.user_id)}
                />)
        }
      </>
    );
  }
}

TeamsPlayers.propTypes = {
  t: PropTypes.func,
  nsdt: PropTypes.func,
  teamId: PropTypes.string,
  team: PropTypes.object,
  isPlanner: PropTypes.bool,
  exerciseId: PropTypes.string,
  exercise: PropTypes.object,
  organizations: PropTypes.array,
  users: PropTypes.array,
  fetchTeamPlayers: PropTypes.func,
  fetchOrganizations: PropTypes.func,
  addExerciseTeamPlayers: PropTypes.func,
  removeExerciseTeamPlayers: PropTypes.func,
  handleClose: PropTypes.func,
};

const select = (state, ownProps) => {
  const helper = storeHelper(state);
  const { teamId, exerciseId } = ownProps;
  return {
    isPlanner: helper.getMe().user_is_planner,
    organizationsMap: helper.getOrganizationsMap(),
    exercise: exerciseId && helper.getExercise(exerciseId),
    team: helper.getTeam(teamId),
    users: helper.getTeamUsers(teamId),
  };
};

export default R.compose(
  connect(select, { fetchTeamPlayers, fetchOrganizations, addExerciseTeamPlayers, removeExerciseTeamPlayers }),
  inject18n,
  withStyles(styles),
)(TeamsPlayers);
