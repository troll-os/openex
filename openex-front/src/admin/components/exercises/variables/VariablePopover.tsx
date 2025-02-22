import React, { useState } from 'react';
import { IconButton, Menu, MenuItem, Dialog, DialogContent, DialogContentText, DialogActions, Button, DialogTitle } from '@mui/material';
import { MoreVert } from '@mui/icons-material';
import { isExerciseReadOnly } from '../../../../utils/Exercise';
import { useFormatter } from '../../../../components/i18n';
import type { Exercise, Variable, VariableInput } from '../../../../utils/api-types';
import VariableForm from './VariableForm';
import { deleteVariable, updateVariable } from '../../../../actions/Variable';
import { useAppDispatch } from '../../../../utils/hooks';
import Transition from '../../../../components/common/Transition';

interface Props {
  disabled?: boolean;
  exercise: Exercise;
  variable: Variable;
  onDeleteVariable?: () => void;
}

const VariablePopover: React.FC<Props> = ({
  disabled,
  exercise,
  variable,
  onDeleteVariable,
}) => {
  // Standard hooks
  const [editVar, setEditVar] = useState(false);
  const [deleteVar, setDeleteVar] = useState(false);
  const [anchorEl, setAnchorEl] = useState<Element | null>(null);
  const { t } = useFormatter();
  const dispatch = useAppDispatch();

  const initialValues = (({
    variable_key,
    variable_description,
    variable_value,
  }) => ({ variable_key, variable_description, variable_value }))(variable);

  const submitDelete = () => {
    dispatch(deleteVariable(exercise.exercise_id, variable.variable_id));
    setDeleteVar(false);
  };

  const submitEdit = (data: VariableInput) => {
    dispatch(updateVariable(exercise.exercise_id, variable.variable_id, data));
    setEditVar(false);
  };

  return (
    <div>
      <IconButton
        onClick={(ev) => {
          ev.stopPropagation();
          setAnchorEl(ev.currentTarget);
        }}
        aria-haspopup="true"
        size="large"
        disabled={disabled}
      >
        <MoreVert />
      </IconButton>
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={() => setAnchorEl(null)}
      >
        <MenuItem
          onClick={() => {
            setEditVar(true);
            setAnchorEl(null);
          }}
          disabled={isExerciseReadOnly(exercise)}
        >
          {t('Update')}
        </MenuItem>
        {!onDeleteVariable && (
          <MenuItem
            onClick={() => {
              setDeleteVar(true);
              setAnchorEl(null);
            }}
            disabled={isExerciseReadOnly(exercise)}
          >
            {t('Delete')}
          </MenuItem>
        )}
      </Menu>
      <Dialog
        open={deleteVar}
        TransitionComponent={Transition}
        onClose={() => setDeleteVar(false)}
        PaperProps={{ elevation: 1 }}
      >
        <DialogContent>
          <DialogContentText>
            {t('Do you want to delete the variable ?')}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteVar(false)}>{t('Cancel')}</Button>
          <Button color="secondary" onClick={submitDelete}>
            {t('Delete')}
          </Button>
        </DialogActions>
      </Dialog>
      <Dialog
        TransitionComponent={Transition}
        open={editVar}
        onClose={() => setEditVar(false)}
        fullWidth={true}
        maxWidth="md"
        PaperProps={{ elevation: 1 }}
      >
        <DialogTitle>{t('Update the variable')}</DialogTitle>
        <DialogContent>
          <VariableForm
            initialValues={initialValues}
            editing={true}
            onSubmit={submitEdit}
            handleClose={() => setEditVar(false)}
          />
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default VariablePopover;
