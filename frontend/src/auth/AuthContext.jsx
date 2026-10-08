import { createContext, useContext, useEffect, useState } from 'react';
import { api, getToken, setToken } from '../api/client.js';

// React Context lets any component read the logged-in user and call
// login/register/logout without passing props down through every level.
const AuthContext = createContext(null);

const USER_KEY = 'auth_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    // Restore the user from localStorage on page refresh.
    const saved = localStorage.getItem(USER_KEY);
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // If there's no token, make sure we're logged out.
    if (!getToken()) setUser(null);
    setLoading(false);
  }, []);

  function persist(auth) {
    // The backend returns { token, userId, name, email, role }.
    setToken(auth.token);
    const profile = {
      id: auth.userId,
      name: auth.name,
      email: auth.email,
      role: auth.role,
    };
    localStorage.setItem(USER_KEY, JSON.stringify(profile));
    setUser(profile);
  }

  async function login(email, password) {
    const auth = await api('/api/auth/login', {
      method: 'POST',
      auth: false,
      body: { email, password },
    });
    persist(auth);
  }

  async function register(name, email, password) {
    const auth = await api('/api/auth/register', {
      method: 'POST',
      auth: false,
      body: { name, email, password },
    });
    persist(auth);
  }

  function logout() {
    setToken(null);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }

  const value = { user, loading, login, register, logout };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// Convenience hook: const { user, login, logout } = useAuth();
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
