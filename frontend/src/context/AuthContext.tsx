import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { authApi } from '@/api/endpoints';
import type { AuthResponse, Role } from '@/types';

interface StoredUser {
  userId: number;
  email: string;
  fullName: string;
  role: Role;
  branchId: number | null;
}

interface AuthContextValue {
  user: StoredUser | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (payload: { email: string; password: string; fullName: string; phone?: string; role: string; branchId?: number }) => Promise<void>;
  logout: () => void;
  hasAnyRole: (...roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function persistAuth(auth: AuthResponse): StoredUser {
  const user: StoredUser = {
    userId: auth.userId,
    email: auth.email,
    fullName: auth.fullName,
    role: auth.role,
    branchId: auth.branchId,
  };
  localStorage.setItem('rvms_token', auth.token);
  localStorage.setItem('rvms_user', JSON.stringify(user));
  return user;
}

function loadStoredUser(): StoredUser | null {
  const raw = localStorage.getItem('rvms_user');
  if (!raw) return null;
  try {
    return JSON.parse(raw) as StoredUser;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<StoredUser | null>(loadStoredUser);

  const login = async (email: string, password: string) => {
    const auth = await authApi.login(email, password);
    setUser(persistAuth(auth));
  };

  const register = async (payload: { email: string; password: string; fullName: string; phone?: string; role: string; branchId?: number }) => {
    const auth = await authApi.register(payload);
    setUser(persistAuth(auth));
  };

  const logout = () => {
    localStorage.removeItem('rvms_token');
    localStorage.removeItem('rvms_user');
    setUser(null);
  };

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      login,
      register,
      logout,
      hasAnyRole: (...roles: Role[]) => (user ? roles.includes(user.role) : false),
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
