// Tiny wrapper around fetch() so every component doesn't repeat the same
// boilerplate (base URL, JSON headers, Authorization token, error handling).

const BASE_URL = import.meta.env.VITE_API_URL || ''; // '' => use the Vite proxy
const TOKEN_KEY = 'auth_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

/**
 * Make an API call.
 * @param {string} path   e.g. '/api/tasks'
 * @param {object} options { method, body, auth }
 * Throws an Error (with .status and .data) on non-2xx responses.
 */
export async function api(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' };

  if (auth) {
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  // 204 No Content (e.g. after DELETE) has no body.
  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const message = data?.message || `Request failed (${res.status})`;
    const error = new Error(message);
    error.status = res.status;
    error.data = data;
    throw error;
  }

  return data;
}
