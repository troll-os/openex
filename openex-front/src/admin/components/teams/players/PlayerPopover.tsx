import React, { FunctionComponent, useState } from 'react';
import { Dialog as MuiDialog, DialogContent, DialogContentText, DialogActions, Button, IconButton, Menu, MenuItem } from '@mui/material';
import { MoreVert } from '@mui/icons-material';
import Dialog from '../../../../components/common/Dialog';
import { updateTeamPlayers } from '../../../../actions/Team';
import { deletePlayer, updatePlayer } from '../../../../actions/User';
import PlayerForm from './PlayerForm';
import { useFormatter } from '../../../../components/i18n';
import { useAppDispatch } from '../../../../utils/hooks';
import Transition from '../../../../components/common/Transition';
import type { UpdatePlayerInput } from '../../../../utils/api-types';
import { countryOption, Option, organizationOption, tagOptions } from '../../../../utils/Option';
import { useHelper } from '../../../../store';
import type { ExercicesHelper, OrganizationsHelper, TagsHelper, UsersHelper } from '../../../../actions/helper';
import type { PlayerInputForm, UserStore } from './Player';

interface PlayerPopoverProps {
  user: UserStore;
  teamId?: string;
  teamUsersIds?: string[];
  anchorEl?: string[];
}

const PlayerPopover: FunctionComponent<PlayerPopoverProps> = ({
  user,
  teamId,
  teamUsersIds,
}) => {
  const { t } = useFormatter();
  const dispatch = useAppDispatch();
  const { userAdmin, organizationsMap, tagsMap } = useHelper(
    (
      helper: ExercicesHelper & UsersHelper & OrganizationsHelper & TagsHelper,
    ) => {
      return {
        userAdmin: helper.getMe()?.user_admin,
        organizationsMap: helper.getOrganizationsMap(),
        tagsMap: helper.getTagsMap(),
      };
    },
  );

  const [openDelete, setOpenDelete] = useState(false);
  const [openEdit, setOpenEdit] = useState(false);
  const [openRemove, setOpenRemove] = useState(false);
  const [anchorEl, setAnchorEl] = useState<Element | null>(null);

  // Popover
  const handlePopoverOpen = (event: React.MouseEvent) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
  };

  const handlePopoverClose = () => setAnchorEl(null);

  // Edition
  const handleOpenEdit = () => {
    setOpenEdit(true);
    handlePopoverClose();
  };

  const handleCloseEdit = () => setOpenEdit(false);

  const onSubmitEdit = (data: PlayerInputForm) => {
    const inputValues: UpdatePlayerInput = {
      ...data,
      user_organization: data.user_organization?.id,
      user_country: data.user_country?.id,
      user_tags: data.user_tags?.map((tag: Option) => tag.id),
    };
    return dispatch(updatePlayer(user.user_id, inputValues)).then(() => handleCloseEdit());
  };

  // Deletion
  const handleOpenDelete = () => {
    setOpenDelete(true);
    handlePopoverClose();
  };

  const handleCloseDelete = () => setOpenDelete(false);

  const submitDelete = () => {
    dispatch(deletePlayer(user.user_id)).then(() => handleCloseDelete());
  };

  // Remove
  const handleOpenRemove = () => {
    setOpenRemove(true);
    handlePopoverClose();
  };

  const handleCloseRemove = () => setOpenRemove(false);

  const submitRemove = () => {
    return dispatch(
      updateTeamPlayers(teamId, {
        team_users: teamUsersIds?.filter((id) => id !== user.user_id),
      }),
    ).then(() => handleCloseRemove());
  };

  const initialValues: PlayerInputForm = {
    ...user,
    user_organization: organizationOption(
      user.user_organization,
      organizationsMap,
    ),
    user_country: countryOption(user.user_country),
    user_tags: tagOptions(user.user_tags, tagsMap),
  };
  const canDelete = user.user_email !== 'admin@openex.io' && (userAdmin || !user.user_admin);
  const canUpdateEmail = user.user_email !== 'admin@openex.io' && (userAdmin || !user.user_admin);
  return (
    <div>
      <IconButton onClick={handlePopoverOpen} aria-haspopup="true" size="large" color="primary">
        <MoreVert />
      </IconButton>
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handlePopoverClose}
      >
        <MenuItem onClick={handleOpenEdit}>{t('Update')}</MenuItem>
        {teamId && (
          <MenuItem onClick={handleOpenRemove}>
            {t('Remove from the team')}
          </MenuItem>
        )}
        {canDelete && (
          <MenuItem onClick={handleOpenDelete}>{t('Delete')}</MenuItem>
        )}
      </Menu>
      <MuiDialog
        open={openDelete}
        TransitionComponent={Transition}
        onClose={handleCloseDelete}
        PaperProps={{ elevation: 1 }}
      >
        <DialogContent>
          <DialogContentText>
            {t('Do you want to delete this player?')}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDelete}>{t('Cancel')}</Button>
          <Button color="secondary" onClick={submitDelete}>
            {t('Delete')}
          </Button>
        </DialogActions>
      </MuiDialog>
      <Dialog
        open={openEdit}
        handleClose={handleCloseEdit}
        title={t('Update the player')}
      >
        <PlayerForm
          initialValues={initialValues}
          handleClose={handleCloseEdit}
          onSubmit={onSubmitEdit}
          editing={true}
          canUpdateEmail={canUpdateEmail}
        />
      </Dialog>
      <MuiDialog
        open={openRemove}
        TransitionComponent={Transition}
        onClose={handleCloseRemove}
        PaperProps={{ elevation: 1 }}
      >
        <DialogContent>
          <DialogContentText>
            {t('Do you want to remove the player from the team?')}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseRemove}>{t('Cancel')}</Button>
          <Button color="secondary" onClick={submitRemove}>
            {t('Remove')}
          </Button>
        </DialogActions>
      </MuiDialog>
    </div>
  );
};

export default PlayerPopover;
