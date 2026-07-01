import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { LoginPage } from './LoginPage';
import { AuthProvider } from '@/context/AuthContext';

vi.mock('@/api/endpoints', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
  },
}));

import { authApi } from '@/api/endpoints';

function renderLoginPage() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('shows validation errors when submitting an empty form', async () => {
    renderLoginPage();
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText(/email is required/i)).toBeInTheDocument();
    expect(await screen.findByText(/password is required/i)).toBeInTheDocument();
    expect(authApi.login).not.toHaveBeenCalled();
  });

  it('logs in and stores the session on success', async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      token: 'abc123',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      userId: 1,
      email: 'admin@rvms.com',
      fullName: 'System Admin',
      role: 'ADMIN',
      branchId: null,
      expiresInMs: 3600000,
    });

    renderLoginPage();
    await userEvent.type(screen.getByLabelText(/email/i), 'admin@rvms.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'Password123!');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => expect(authApi.login).toHaveBeenCalledWith('admin@rvms.com', 'Password123!'));
    await waitFor(() => expect(localStorage.getItem('rvms_token')).toBe('abc123'));
  });

  it('shows the server error message when login fails', async () => {
    vi.mocked(authApi.login).mockRejectedValue({
      isAxiosError: true,
      response: { data: { message: 'Invalid email or password' } },
    });

    renderLoginPage();
    await userEvent.type(screen.getByLabelText(/email/i), 'admin@rvms.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'wrong-password');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/invalid email or password/i);
  });
});
