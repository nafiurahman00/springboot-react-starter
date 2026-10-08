import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

// Wrap any route that requires login. If there's no user, redirect to /login.
export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) return <p className="container">Loading…</p>;
  if (!user) return <Navigate to="/login" replace />;

  return children;
}
