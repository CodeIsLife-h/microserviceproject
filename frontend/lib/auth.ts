export const getToken = () =>
  typeof window !== 'undefined' ? localStorage.getItem('token') : null;

export const setToken = (token: string) =>
  localStorage.setItem('token', token);

export const clearToken = () =>
  localStorage.removeItem('token');

export const isAuthenticated = () => !!getToken();
