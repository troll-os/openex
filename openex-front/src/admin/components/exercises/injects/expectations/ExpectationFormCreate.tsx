import React, { FunctionComponent, SyntheticEvent, useEffect } from 'react';
import { Controller, SubmitHandler, useForm } from 'react-hook-form';
import { Alert, Button, FormControlLabel, FormLabel, InputLabel, MenuItem, Radio, RadioGroup, Select as MUISelect, TextField as MuiTextField } from '@mui/material';
import { makeStyles } from '@mui/styles';
import type { ExpectationInput } from './Expectation';
import { formProps, infoMessage, isTechnicalExpectation } from './ExpectationFormUtils';
import { useFormatter } from '../../../../../components/i18n';
import type { Theme } from '../../../../../components/Theme';

const useStyles = makeStyles((theme: Theme) => ({
  marginTop_2: {
    marginTop: theme.spacing(2),
  },
  buttons: {
    display: 'flex',
    placeContent: 'end',
    gap: theme.spacing(2),
    marginTop: theme.spacing(2),
  },
}));

interface Props {
  predefinedExpectations: ExpectationInput[];
  onSubmit: SubmitHandler<ExpectationInput>;
  handleClose: () => void;
}

const ExpectationFormCreate: FunctionComponent<Props> = ({
  predefinedExpectations = [],
  onSubmit,
  handleClose,
}) => {
  const { t } = useFormatter();
  const classes = useStyles();

  const computeValuesFromType = (type: string) => {
    const predefinedExpectation = predefinedExpectations.filter((pe) => pe.expectation_type === type)[0];
    if (predefinedExpectation) {
      return {
        expectation_type: predefinedExpectation.expectation_type ?? '',
        expectation_name: predefinedExpectation.expectation_name ?? '',
        expectation_description: predefinedExpectation.expectation_description ?? '',
        expectation_score: predefinedExpectation.expectation_score ?? 0,
        expectation_expectation_group: predefinedExpectation.expectation_expectation_group ?? false,
      };
    }
    return {
      expectation_type: 'MANUAL',
      expectation_name: '',
      expectation_description: '',
      expectation_score: 0,
      expectation_expectation_group: false,
    };
  };

  const predefinedTypes = predefinedExpectations.map((e) => e.expectation_type);

  const initialValues = computeValuesFromType(predefinedTypes[0]);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting, isValid },
    watch,
    reset,
    getValues,
    control,
  } = useForm<ExpectationInput>(formProps(initialValues, t));
  const watchType = watch('expectation_type');

  const handleSubmitWithoutPropagation = (e: SyntheticEvent) => {
    e.preventDefault();
    e.stopPropagation();
    handleSubmit(onSubmit)(e);
  };

  useEffect(() => {
    reset(computeValuesFromType(watchType));
  }, [watchType]);

  return (
    <form id="expectationForm" onSubmit={handleSubmitWithoutPropagation}>
      <div>
        <InputLabel id="input-type">{t('Type')}</InputLabel>
        <MUISelect
          labelId="input-type"
          value={getValues().expectation_type}
          variant="standard"
          fullWidth
          error={!!errors.expectation_type}
          inputProps={register('expectation_type')}
        >
          {predefinedTypes.map((type) => (<MenuItem key={type} value={type}>{t(type)}</MenuItem>))}
          <MenuItem key={'MANUAL'} value={'MANUAL'}>{t('MANUAL')}</MenuItem>
        </MUISelect>
      </div>
      {watchType === 'ARTICLE'
        && <Alert
          severity="info"
          className={classes.marginTop_2}
           >
          {infoMessage(getValues().expectation_type, t)}
        </Alert>
      }
      <MuiTextField
        variant="standard"
        fullWidth
        label={t('Name')}
        className={classes.marginTop_2}
        error={!!errors.expectation_name}
        helperText={
          errors.expectation_name && errors.expectation_name?.message
        }
        inputProps={register('expectation_name')}
      />
      <MuiTextField
        variant="standard"
        fullWidth
        label={t('Description')}
        className={classes.marginTop_2}
        multiline
        error={!!errors.expectation_description}
        helperText={
          errors.expectation_description && errors.expectation_description?.message
        }
        inputProps={register('expectation_description')}
      />
      <MuiTextField
        variant="standard"
        fullWidth
        label={t('Score')}
        type="number"
        className={classes.marginTop_2}
        error={!!errors.expectation_score}
        helperText={
          errors.expectation_score && errors.expectation_score?.message
        }
        inputProps={register('expectation_score')}
      />

      {isTechnicalExpectation(initialValues)
        && <Controller
          control={control}
          name="expectation_expectation_group"
          render={({ field: { onChange, value } }) => (
            <div className={classes.marginTop_2}>
              <FormLabel>{t('Validation mode')}</FormLabel>
              <RadioGroup
                defaultValue={false}
                value={value}
                onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                  onChange((event.target as HTMLInputElement).value === 'true');
                }}
              >
                <FormControlLabel value={false} control={<Radio />} label={t('All assets (per group) must validate the expectation')} />
                <FormControlLabel value={true} control={<Radio />} label={t('At least one asset (per group) must validate the expectation')} />
              </RadioGroup>
            </div>
          )}
           />
      }

      <div className={classes.buttons}>
        <Button
          onClick={handleClose}
          disabled={isSubmitting}
        >
          {t('Cancel')}
        </Button>
        <Button
          color="secondary"
          type="submit"
          disabled={!isValid || isSubmitting}
        >
          {t('Create')}
        </Button>
      </div>
    </form>
  );
};

export default ExpectationFormCreate;
