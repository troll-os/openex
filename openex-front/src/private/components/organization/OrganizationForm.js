import React, { Component } from 'react';
import * as PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { TextField } from '../../../components/TextField';
import inject18n from '../../../components/i18n';
import TagField from '../../../components/TagField';

class OrganizationForm extends Component {
  validate(values) {
    const { t } = this.props;
    const errors = {};
    const requiredFields = ['organization_name'];
    requiredFields.forEach((field) => {
      if (!values[field]) {
        errors[field] = t('This field is required.');
      }
    });
    return errors;
  }

  render() {
    const { t, onSubmit, initialValues } = this.props;
    return (
      <Form
        keepDirtyOnReinitialize={true}
        initialValues={initialValues}
        onSubmit={onSubmit}
        validate={this.validate.bind(this)}
        mutators={{
          setValue: ([field, value], state, { changeValue }) => {
            changeValue(state, field, () => value);
          },
        }}
      >
        {({ handleSubmit, form, values }) => (
          <form id="organizationForm" onSubmit={handleSubmit}>
            <TextField
              variant="standard"
              name="organization_name"
              fullWidth={true}
              label={t('Name')}
            />
            <TextField
              variant="standard"
              name="organization_description"
              fullWidth={true}
              multiline={true}
              rows={2}
              label={t('Description')}
              style={{ marginTop: 20 }}
            />
            <TagField
              name="organization_tags"
              values={values}
              setFieldValue={form.mutators.setValue}
            />
          </form>
        )}
      </Form>
    );
  }
}

OrganizationForm.propTypes = {
  t: PropTypes.func,
  onSubmit: PropTypes.func.isRequired,
  edit: PropTypes.bool,
  change: PropTypes.func,
};

export default inject18n(OrganizationForm);
