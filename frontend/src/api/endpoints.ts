import { apiClient } from './client';
import type {
  AuthResponse,
  Branch,
  ChatMessage,
  Customer,
  DashboardReport,
  MaintenanceRecord,
  Payment,
  Rental,
  Reservation,
  Vehicle,
} from '@/types';

// ---- Auth ----
export const authApi = {
  login: (email: string, password: string) =>
    apiClient.post<AuthResponse>('/auth/login', { email, password }).then((r) => r.data),
  register: (payload: { email: string; password: string; fullName: string; phone?: string; role: string; branchId?: number }) =>
    apiClient.post<AuthResponse>('/auth/register', payload).then((r) => r.data),
};

// ---- Branches ----
export const branchApi = {
  list: () => apiClient.get<Branch[]>('/branches').then((r) => r.data),
  get: (id: number) => apiClient.get<Branch>(`/branches/${id}`).then((r) => r.data),
  create: (payload: Partial<Branch>) => apiClient.post<Branch>('/branches', payload).then((r) => r.data),
  update: (id: number, payload: Partial<Branch>) => apiClient.put<Branch>(`/branches/${id}`, payload).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/branches/${id}`),
};

// ---- Vehicles ----
export const vehicleApi = {
  list: () => apiClient.get<Vehicle[]>('/vehicles').then((r) => r.data),
  byBranch: (branchId: number) => apiClient.get<Vehicle[]>(`/vehicles/branch/${branchId}`).then((r) => r.data),
  search: (params: Record<string, string | number | undefined>) =>
    apiClient.get<Vehicle[]>('/vehicles/search', { params }).then((r) => r.data),
  create: (payload: Partial<Vehicle>) => apiClient.post<Vehicle>('/vehicles', payload).then((r) => r.data),
  update: (id: number, payload: Partial<Vehicle>) => apiClient.put<Vehicle>(`/vehicles/${id}`, payload).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/vehicles/${id}`),
};

// ---- Customers ----
export const customerApi = {
  list: () => apiClient.get<Customer[]>('/customers').then((r) => r.data),
  search: (term: string) => apiClient.get<Customer[]>('/customers/search', { params: { term } }).then((r) => r.data),
  create: (payload: Partial<Customer>) => apiClient.post<Customer>('/customers', payload).then((r) => r.data),
  update: (id: number, payload: Partial<Customer>) => apiClient.put<Customer>(`/customers/${id}`, payload).then((r) => r.data),
  remove: (id: number) => apiClient.delete(`/customers/${id}`),
};

// ---- Bookings (reservations + rentals) ----
export const bookingApi = {
  reservations: () => apiClient.get<Reservation[]>('/reservations').then((r) => r.data),
  createReservation: (payload: { customerId: number; vehicleId: number; startDate: string; endDate: string }) =>
    apiClient.post<Reservation>('/reservations', payload).then((r) => r.data),
  cancelReservation: (id: number) => apiClient.post<Reservation>(`/reservations/${id}/cancel`).then((r) => r.data),
  convertReservation: (id: number) => apiClient.post<Rental>(`/reservations/${id}/convert`).then((r) => r.data),
  rentals: () => apiClient.get<Rental[]>('/rentals').then((r) => r.data),
  activeRentals: () => apiClient.get<Rental[]>('/rentals/active').then((r) => r.data),
  createDirectRental: (payload: { customerId: number; vehicleId: number; startDate: string; endDate: string }) =>
    apiClient.post<Rental>('/rentals/direct', payload).then((r) => r.data),
  returnVehicle: (id: number, actualReturnDate: string) =>
    apiClient.post<Rental>(`/rentals/${id}/return`, { actualReturnDate }).then((r) => r.data),
};

// ---- Payments ----
export const paymentApi = {
  byRental: (rentalId: number) => apiClient.get<Payment[]>(`/payments/rental/${rentalId}`).then((r) => r.data),
  process: (rentalId: number, method: string) =>
    apiClient.post<Payment>('/payments', { rentalId, method }).then((r) => r.data),
  refund: (id: number) => apiClient.post<Payment>(`/payments/${id}/refund`).then((r) => r.data),
};

// ---- Maintenance ----
export const maintenanceApi = {
  list: () => apiClient.get<MaintenanceRecord[]>('/maintenance').then((r) => r.data),
  byVehicle: (vehicleId: number) => apiClient.get<MaintenanceRecord[]>(`/maintenance/vehicle/${vehicleId}`).then((r) => r.data),
  create: (payload: { vehicleId: number; description: string; cost?: number; scheduledDate: string }) =>
    apiClient.post<MaintenanceRecord>('/maintenance', payload).then((r) => r.data),
  complete: (id: number, completedDate: string) =>
    apiClient.post<MaintenanceRecord>(`/maintenance/${id}/complete`, { completedDate }).then((r) => r.data),
};

// ---- Reports ----
export const reportApi = {
  dashboard: () => apiClient.get<DashboardReport>('/reports/dashboard').then((r) => r.data),
  branch: (branchId: number) => apiClient.get(`/reports/branch/${branchId}`).then((r) => r.data),
};

// ---- AI assistant ----
export const aiApi = {
  chat: (message: string, sessionId?: string) =>
    apiClient.post<ChatMessage>('/ai/chat', { message, sessionId }).then((r) => r.data),
};
