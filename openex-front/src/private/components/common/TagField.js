import React, { Component } from 'react';
import * as R from 'ramda';
import { LabelOutlined } from '@mui/icons-material';
import Box from '@mui/material/Box';
import { withStyles } from '@mui/styles';
import { connect } from 'react-redux';
import inject18n from '../../../components/i18n';
import { Autocomplete } from '../../../components/Autocomplete';
import { fetchTags } from '../../../actions/Tag';

const styles = () => ({
  icon: {
    paddingTop: 4,
    display: 'inline-block',
  },
  text: {
    display: 'inline-block',
    flexGrow: 1,
    marginLeft: 10,
  },
  autoCompleteIndicator: {
    display: 'none',
  },
});

class TagField extends Component {
  constructor(props) {
    super(props);
    this.state = { tagCreation: false, tagInput: '' };
  }

  componentDidMount() {
    this.props.fetchTags();
  }

  handleOpenTagCreation() {
    this.setState({ tagCreation: true });
  }

  handleCloseTagCreation() {
    this.setState({ tagCreation: false });
  }

  render() {
    const {
      t, name, tags, classes,
    } = this.props;
    const tagsOptions = R.map(
      (n) => ({
        id: n.tag_id,
        label: n.tag_name,
        color: n.tag_color,
      }),
      tags,
    );
    return (
      <div>
        <Autocomplete
          variant="standard"
          name={name}
          fullWidth={true}
          multiple={true}
          label={t('Tags')}
          options={tagsOptions}
          style={{ marginTop: 20 }}
          openCreate={this.handleOpenTagCreation.bind(this)}
          renderOption={(props, option) => (
            <Box component="li" {...props}>
              <div className={classes.icon} style={{ color: option.color }}>
                <LabelOutlined />
              </div>
              <div className={classes.text}>{option.label}</div>
            </Box>
          )}
        />
      </div>
    );
  }
}

const select = (state) => ({
  tags: R.values(state.referential.entities.tags),
});

export default R.compose(
  connect(select, { fetchTags }),
  inject18n,
  withStyles(styles),
)(TagField);
