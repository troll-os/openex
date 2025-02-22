import React, { Component } from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as R from 'ramda';
import { Button, Chip, List, ListItem, ListItemText, Dialog, DialogTitle, DialogContent, DialogActions, Box, ListItemIcon, Grid } from '@mui/material';
import { ControlPointOutlined } from '@mui/icons-material';
import withStyles from '@mui/styles/withStyles';
import SearchFilter from '../../../../components/SearchFilter';
import inject18n from '../../../../components/i18n';
import { storeHelper } from '../../../../actions/Schema';
import { fetchChannels, fetchExerciseArticles } from '../../../../actions/Channel';
import CreateArticle from '../articles/CreateArticle';
import { truncate } from '../../../../utils/String';
import { isExerciseReadOnly } from '../../../../utils/Exercise';
import Transition from '../../../../components/common/Transition';
import ChannelIcon from '../../components/channels/ChannelIcon';

const styles = (theme) => ({
  createButton: {
    position: 'fixed',
    bottom: 30,
    right: 30,
  },
  box: {
    width: '100%',
    minHeight: '100%',
    padding: 20,
    border: '1px dashed rgba(255, 255, 255, 0.3)',
  },
  chip: {
    margin: '0 10px 10px 0',
  },
  item: {
    paddingLeft: 10,
    height: 50,
  },
  text: {
    fontSize: 15,
    color: theme.palette.primary.main,
    fontWeight: 500,
  },
});

class InjectAddArticles extends Component {
  constructor(props) {
    super(props);
    this.state = {
      open: false,
      keyword: '',
      articlesIds: [],
    };
  }

  componentDidMount() {
    this.props.fetchChannels();
    this.props.fetchExerciseArticles(this.props.exerciseId);
  }

  handleOpen() {
    this.setState({ open: true });
  }

  handleClose() {
    this.setState({ open: false, keyword: '', articlesIds: [] });
  }

  handleSearchArticles(value) {
    this.setState({ keyword: value });
  }

  addArticle(articleId) {
    this.setState({
      articlesIds: R.append(articleId, this.state.articlesIds),
    });
  }

  removeArticle(articleId) {
    this.setState({
      articlesIds: R.filter((u) => u !== articleId, this.state.articlesIds),
    });
  }

  submitAddArticles() {
    this.props.handleAddArticles(this.state.articlesIds);
    this.handleClose();
  }

  onCreate(result) {
    this.addArticle(result);
  }

  render() {
    const {
      classes,
      t,
      articles,
      injectArticlesIds,
      exerciseId,
      exercise,
      articlesMap,
      channelsMap,
    } = this.props;
    const { keyword, articlesIds } = this.state;
    const filterByKeyword = (n) => keyword === ''
      || (n.article_name || '').toLowerCase().indexOf(keyword.toLowerCase())
        !== -1
      || (n.article_description || '')
        .toLowerCase()
        .indexOf(keyword.toLowerCase()) !== -1
      || (n.article_fullchannel?.channel_name || '')
        .toLowerCase()
        .indexOf(keyword.toLowerCase()) !== -1;
    const fullArticles = articles.map((item) => ({
      ...item,
      article_fullchannel: channelsMap[item.article_channel] || {},
    }));
    const filteredArticles = R.pipe(
      R.filter(filterByKeyword),
      R.take(10),
    )(fullArticles);
    return (
      <div>
        <ListItem
          classes={{ root: classes.item }}
          button={true}
          divider={true}
          onClick={this.handleOpen.bind(this)}
          color="primary"
          disabled={isExerciseReadOnly(exercise)}
        >
          <ListItemIcon color="primary">
            <ControlPointOutlined color="primary" />
          </ListItemIcon>
          <ListItemText
            primary={t('Add channel pressure')}
            classes={{ primary: classes.text }}
          />
        </ListItem>
        <Dialog
          open={this.state.open}
          TransitionComponent={Transition}
          onClose={this.handleClose.bind(this)}
          fullWidth={true}
          maxWidth="lg"
          PaperProps={{
            elevation: 1,
            sx: {
              minHeight: 580,
              maxHeight: 580,
            },
          }}
        >
          <DialogTitle>{t('Add media pressure in this inject')}</DialogTitle>
          <DialogContent>
            <Grid container={true} spacing={3} style={{ marginTop: -15 }}>
              <Grid item={true} xs={8}>
                <Grid container={true} spacing={3}>
                  <Grid item={true} xs={6}>
                    <SearchFilter
                      onChange={this.handleSearchArticles.bind(this)}
                      fullWidth={true}
                    />
                  </Grid>
                </Grid>
                <List>
                  {filteredArticles.map((article) => {
                    const disabled = articlesIds.includes(article.article_id)
                      || injectArticlesIds.includes(article.article_id);
                    return (
                      <ListItem
                        key={article.article_id}
                        disabled={disabled}
                        button={true}
                        divider={true}
                        dense={true}
                        onClick={this.addArticle.bind(this, article.article_id)}
                      >
                        <ListItemIcon>
                          <ChannelIcon
                            type={article.article_fullchannel.channel_type}
                            variant="inline"
                          />
                        </ListItemIcon>
                        <ListItemText
                          primary={article.article_name}
                          secondary={article.article_author}
                        />
                      </ListItem>
                    );
                  })}
                  <CreateArticle
                    exerciseId={exerciseId}
                    inline={true}
                    onCreate={this.onCreate.bind(this)}
                  />
                </List>
              </Grid>
              <Grid item={true} xs={4}>
                <Box className={classes.box}>
                  {this.state.articlesIds.map((articleId) => {
                    const article = articlesMap[articleId];
                    const channel = article
                      ? channelsMap[article.article_channel] || {}
                      : {};
                    return (
                      <Chip
                        key={articleId}
                        onDelete={this.removeArticle.bind(this, articleId)}
                        label={truncate(article?.article_name, 22)}
                        icon={
                          <ChannelIcon type={channel.channel_type} variant="chip" />
                        }
                        classes={{ root: classes.chip }}
                      />
                    );
                  })}
                </Box>
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleClose.bind(this)}>{t('Cancel')}</Button>
            <Button
              color="secondary"
              onClick={this.submitAddArticles.bind(this)}
            >
              {t('Add')}
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  }
}

InjectAddArticles.propTypes = {
  t: PropTypes.func,
  exerciseId: PropTypes.string,
  exercise: PropTypes.object,
  fetchExerciseArticles: PropTypes.func,
  handleAddArticles: PropTypes.func,
  organizations: PropTypes.array,
  articles: PropTypes.array,
  injectArticlesIds: PropTypes.array,
  attachment: PropTypes.bool,
};

const select = (state, ownProps) => {
  const helper = storeHelper(state);
  const { exerciseId } = ownProps;
  const exercise = helper.getExercise(exerciseId);
  const articles = helper.getExerciseArticles(exerciseId);
  const articlesMap = helper.getArticlesMap();
  const channelsMap = helper.getChannelsMap();
  return { exercise, articles, articlesMap, channelsMap };
};

export default R.compose(
  connect(select, { fetchExerciseArticles, fetchChannels }),
  inject18n,
  withStyles(styles),
)(InjectAddArticles);
