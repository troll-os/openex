import React, { Component } from 'react';
import * as PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { Button } from '@mui/material';
import TextField from '../../../../components/TextField';
import inject18n from '../../../../components/i18n';

class KillChainPhaseForm extends Component {
  validate(values) {
    const { t } = this.props;
    const errors = {};
    const requiredFields = ['phase_name', 'phase_kill_chain_name', 'phase_order'];
    requiredFields.forEach((field) => {
      if (!values[field]) {
        errors[field] = t('This field is required.');
      }
    });
    return errors;
  }

  render() {
    const { t, onSubmit, initialValues, handleClose, editing } = this.props;
    return (
      <Form
        keepDirtyOnReinitialize={true}
        initialValues={initialValues}
        onSubmit={onSubmit}
        validate={this.validate.bind(this)}
      >
        {({ handleSubmit, pristine, submitting }) => (
          <form id="killChainPhaseForm" onSubmit={handleSubmit}>
            <TextField
              name="phase_name"
              fullWidth={true}
              label={t('Phase name')}
            />
            <TextField
              variant="standard"
              name="phase_kill_chain_name"
              fullWidth={true}
              label={t('Kill chain name')}
              style={{ marginTop: 20 }}
            />
            <TextField
              variant="standard"
              name="phase_order"
              fullWidth={true}
              type="number"
              label={t('Order')}
              style={{ marginTop: 20 }}
            />
            <div style={{ float: 'right', marginTop: 20 }}>
              <Button
                onClick={handleClose.bind(this)}
                style={{ marginRight: 10 }}
                disabled={submitting}
              >
                {t('Cancel')}
              </Button>
              <Button
                color="secondary"
                type="submit"
                disabled={pristine || submitting}
              >
                {editing ? t('Update') : t('Create')}
              </Button>
            </div>
          </form>
        )}
      </Form>
    );
  }
}

KillChainPhaseForm.propTypes = {
  t: PropTypes.func,
  onSubmit: PropTypes.func.isRequired,
  handleClose: PropTypes.func,
  editing: PropTypes.bool,
};

export default inject18n(KillChainPhaseForm);
