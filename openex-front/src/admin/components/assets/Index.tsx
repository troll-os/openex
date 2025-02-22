import React, { Suspense, lazy } from 'react';
import { makeStyles } from '@mui/styles';
import { Navigate, Route, Routes } from 'react-router-dom';
import { errorWrapper } from '../../../components/Error';
import Loader from '../../../components/Loader';

const Endpoints = lazy(() => import('./endpoints/Endpoints'));
const AssetGroups = lazy(() => import('./asset_groups/AssetGroups'));

const useStyles = makeStyles(() => ({
  root: {
    flexGrow: 1,
  },
}));

const Index = () => {
  const classes = useStyles();
  return (
    <div className={classes.root}>
      <Suspense fallback={<Loader />}>
        <Routes>
          <Route path="" element={<Navigate to="endpoints" replace={true} />} />
          <Route path="endpoints" element={errorWrapper(Endpoints)()} />
          <Route path="asset_groups" element={errorWrapper(AssetGroups)()} />
        </Routes>
      </Suspense>
    </div>
  );
};

export default Index;
