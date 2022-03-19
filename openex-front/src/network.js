import axios from 'axios';
import { normalize } from 'normalizr';

export const api = (schema) => {
  const instance = axios.create({
    headers: { responseType: 'json' },
  });
  // Intercept to apply schema and test unauthorized users
  instance.interceptors.response.use(
    (response) => {
      if (schema) {
        response.data = normalize(response.data, schema);
      }
      return response;
    },
    (err) => {
      const res = err.response;
      if (
        res
        && res.status === 503
        && err.config
        // eslint-disable-next-line no-underscore-dangle
        && !err.config.__isRetryRequest
      ) {
        // eslint-disable-next-line no-param-reassign,no-underscore-dangle
        err.config.__isRetryRequest = true;
        return axios(err.config);
      }
      if (res) {
        // eslint-disable-next-line prefer-promise-reject-errors
        return Promise.reject({ status: res.status, ...res.data });
      }
      // eslint-disable-next-line prefer-promise-reject-errors
      return Promise.reject(false);
    },
  );
  return instance;
};
