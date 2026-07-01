import { useQuery } from '@tanstack/react-query';
import { branchApi, reportApi } from '@/api/endpoints';
import { extractErrorMessage } from '@/api/client';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorAlert } from '@/components/common/ErrorAlert';

export function ReportsPage() {
  const { data: dashboard, isLoading, isError, error } = useQuery({
    queryKey: ['reports', 'dashboard'],
    queryFn: reportApi.dashboard,
  });
  const { data: branches } = useQuery({ queryKey: ['branches'], queryFn: branchApi.list });

  if (isLoading) return <LoadingSpinner label="Loading reports..." />;
  if (isError) return <ErrorAlert message={extractErrorMessage(error)} />;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-800">Reports</h1>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {dashboard?.branchReports.map((report) => {
          const branch = branches?.find((b) => b.id === report.branchId);
          return (
            <div key={report.branchId} className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <h3 className="font-semibold text-slate-800">{report.branchName}</h3>
              {branch && <p className="text-xs text-slate-400">{branch.address}</p>}
              <dl className="mt-3 grid grid-cols-2 gap-2 text-sm text-slate-600">
                <div>Total vehicles: <span className="font-medium text-slate-800">{report.totalVehicles}</span></div>
                <div>Available: <span className="font-medium text-slate-800">{report.availableVehicles}</span></div>
                <div>Rented: <span className="font-medium text-slate-800">{report.rentedVehicles}</span></div>
                <div>Maintenance: <span className="font-medium text-slate-800">{report.maintenanceVehicles}</span></div>
                <div>Active rentals: <span className="font-medium text-slate-800">{report.activeRentals}</span></div>
                <div>Revenue: <span className="font-medium text-slate-800">${report.revenue.toFixed(2)}</span></div>
              </dl>
            </div>
          );
        })}
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <h3 className="mb-2 font-semibold text-slate-800">Company-wide Summary</h3>
        <dl className="grid grid-cols-2 gap-2 text-sm text-slate-600 sm:grid-cols-4">
          <div>Total vehicles: <span className="font-medium text-slate-800">{dashboard?.totalVehicles}</span></div>
          <div>Active rentals: <span className="font-medium text-slate-800">{dashboard?.activeRentals}</span></div>
          <div>Upcoming returns: <span className="font-medium text-slate-800">{dashboard?.upcomingReturns}</span></div>
          <div>Total revenue: <span className="font-medium text-slate-800">${dashboard?.totalRevenue.toFixed(2)}</span></div>
        </dl>
      </div>
    </div>
  );
}
