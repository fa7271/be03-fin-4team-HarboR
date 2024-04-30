/**
 * axios setup to use mock service
 */

import axios from 'axios';
<<<<<<< HEAD
const token = localStorage.getItem('token');
=======
// const token = localStorage.getItem('token');

// const axiosServices = axios.create({
//   baseURL: 'http://localhost:3000',
//   withCredentials: true,
//   headers: {
//     Authorization: `Bearer ${token}`,
//     'Content-Type': 'application/json'
//   }
// });
>>>>>>> 8034daa62c07085a4831ed2602a75e340b528d21

const axiosServices = axios.create({
  baseURL: 'http://localhost:3000',
  headers: {
    'Authorization': `Bearer ${ token }`,
    'Content-Type': 'application/json'
  },
});


axiosServices.interceptors.request.use()

export const setClientHeaders = (token: string | null) => {
  axiosServices.interceptors.request.use(function (config) {
    config.headers.Authorization = `Bearer ${token}`;
    return config;
  });
};

// interceptor for http
axiosServices.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject((error.response && error.response.data) || 'Wrong Services')
);

export default axiosServices;
