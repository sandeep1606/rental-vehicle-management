import { useQuery } from '@tanstack/react-query';
import { reportApi } from '@/api/endpoints';
import { extractErrorMessage } from '@/api/client';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorAlert } from '@/components/common/ErrorAlert';

function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="text-xs uppercase tracking-wide text-slate-400">{label}</div>
      <div className="mt-1 text-2xl font-semibold text-slate-800">{value}</div>
    </div>
  );
}

export function DashboardPage() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['reports', 'dashboard'],
    queryFn: reportApi.dashboard,
  });

  if (isLoading) return <LoadingSpinner label="Loading dashboard..." />;
  if (isError) return <ErrorAlert message={extractErrorMessage(error)} />;
  if (!data) return null;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-800">Dashboard</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        <StatCard label="Total Vehicles" value={data.totalVehicles} />
        <StatCard label="Available" value={data.availableVehicles} />
        <StatCard label="Rented" value={data.rentedVehicles} />
        <StatCard label="In Maintenance" value={data.maintenanceVehicles} />
        <StatCard label="Active Rentals" value={data.activeRentals} />
        <StatCard label="Upcoming Returns (3d)" value={data.upcomingReturns} />
        <StatCard label="Total Revenue" value={`$${data.totalRevenue.toFixed(2)}`} />
      </div>

      <div>
        <h2 className="mb-2 text-sm font-semibold text-slate-700">By Branch</h2>
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-2">Branch</th>
                <th className="px-4 py-2">Total</th>
                <th className="px-4 py-2">Available</th>
                <th className="px-4 py-2">Rented</th>
                <th className="px-4 py-2">Maintenance</th>
                <th className="px-4 py-2">Active Rentals</th>
                <th className="px-4 py-2">Revenue</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data.branchReports.map((b) => (
                <tr key={b.branchId}>
                  <td className="px-4 py-2 font-medium text-slate-700">{b.branchName}</td>
                  <td className="px-4 py-2">{b.totalVehicles}</td>
                  <td className="px-4 py-2">{b.availableVehicles}</td>
                  <td className="px-4 py-2">{b.rentedVehicles}</td>
                  <td className="px-4 py-2">{b.maintenanceVehicles}</td>
                  <td className="px-4 py-2">{b.activeRentals}</td>
                  <td className="px-4 py-2">${b.revenue.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
