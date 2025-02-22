import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Button } from '@mui/material';
import { makeStyles } from '@mui/styles';
import { useFormatter } from '../../../components/i18n';
import type { Theme } from '../../../components/Theme';

const useStyles = makeStyles<Theme>((theme) => ({
  button: {
    marginRight: theme.spacing(2),
    padding: '0 5px 0 5px',
    minHeight: 20,
    minWidth: 20,
    textTransform: 'none',
  },
}));

const TopMenuAssets: React.FC = () => {
  const location = useLocation();
  const classes = useStyles();
  const { t } = useFormatter();
  return (
    <>
      <Button
        component={Link}
        to="/admin/assets/endpoints"
        variant={location.pathname === '/admin/assets/endpoints' ? 'contained' : 'text'}
        size="small"
        color="primary"
        classes={{ root: classes.button }}
      >
        {'Endpoints'}
      </Button>
      <Button
        component={Link}
        to="/admin/assets/asset_groups"
        variant={location.pathname === '/admin/assets/asset_groups' ? 'contained' : 'text'}
        size="small"
        color="primary"
        classes={{ root: classes.button }}
      >
        {t('Asset groups')}
      </Button>
    </>
  );
};

export default TopMenuAssets;
