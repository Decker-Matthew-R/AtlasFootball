import axios from 'axios';

/*
 * This is a singleton that will load on all the interceptors.
 *
 * Use this instead of directly importing axios.
 */

const XSRF_TOKEN_COOKIE_NAME = 'XSRF-TOKEN';
const XSRF_TOKEN_HEADER_NAME = 'X-XSRF-TOKEN';

axios.defaults.headers.post[`Content-Type`] = `application/json;charset=utf-8`;
axios.defaults.withXSRFToken = true;
axios.defaults.xsrfCookieName = XSRF_TOKEN_COOKIE_NAME;
axios.defaults.xsrfHeaderName = XSRF_TOKEN_HEADER_NAME;
axios.defaults.withCredentials = true;
export const axiosInstance = () => {
  return axios.create();
};

export default axiosInstance();
