export type Role = 'ADMIN' | 'BRANCH_MANAGER' | 'STAFF' | 'CUSTOMER';

export interface AuthResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  email: string;
  fullName: string;
  role: Role;
  branchId: number | null;
  expiresInMs: number;
}

export interface Branch {
  id: number;
  name: string;
  address: string;
  phone: string;
  managerName: string | null;
  openingHours: string | null;
  active: boolean;
  createdAt: string;
}

export interface Vehicle {
  id: number;
  plateNumber: string;
  vin: string;
  type: string;
  brand: string;
  model: string;
  year: number;
  mileage: number;
  fuelType: string;
  transmission: string;
  dailyRate: number;
  status: 'AVAILABLE' | 'RESERVED' | 'RENTED' | 'MAINTENANCE' | 'RETIRED';
  branchId: number;
  branchName: string;
}

export interface Customer {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  driverLicenseNumber: string;
  address: string | null;
  dateOfBirth: string | null;
  blacklisted: boolean;
}

export interface Reservation {
  id: number;
  customerId: number;
  customerName: string;
  vehicleId: number;
  vehiclePlate: string;
  branchId: number;
  startDate: string;
  endDate: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'CONVERTED' | 'EXPIRED';
  estimatedTotal: number | null;
}

export interface Rental {
  id: number;
  reservationId: number | null;
  customerId: number;
  customerName: string;
  vehicleId: number;
  vehiclePlate: string;
  branchId: number;
  startDate: string;
  plannedEndDate: string;
  actualReturnDate: string | null;
  dailyRate: number;
  totalAmount: number | null;
  lateFee: number;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
}

export interface Payment {
  id: number;
  rentalId: number;
  amount: number;
  status: 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';
  method: string | null;
  transactionRef: string | null;
  paidAt: string | null;
}

export interface MaintenanceRecord {
  id: number;
  vehicleId: number;
  vehiclePlate: string;
  branchId: number;
  description: string;
  cost: number | null;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  scheduledDate: string;
  completedDate: string | null;
}

export interface BranchReport {
  branchId: number;
  branchName: string;
  totalVehicles: number;
  availableVehicles: number;
  rentedVehicles: number;
  maintenanceVehicles: number;
  activeRentals: number;
  revenue: number;
}

export interface DashboardReport {
  totalVehicles: number;
  availableVehicles: number;
  rentedVehicles: number;
  maintenanceVehicles: number;
  activeRentals: number;
  upcomingReturns: number;
  totalRevenue: number;
  branchReports: BranchReport[];
}

export interface ChatMessage {
  sessionId: string;
  reply: string;
  intent: string;
  toolsUsed: string[];
}

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}
