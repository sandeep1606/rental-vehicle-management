import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { branchApi, vehicleApi } from '@/api/endpoints';
import { extractErrorMessage } from '@/api/client';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorAlert } from '@/components/common/ErrorAlert';
import { useAuth } from '@/context/AuthContext';

const VEHICLE_TYPES = ['SEDAN', 'SUV', 'HATCHBACK', 'TRUCK', 'VAN', 'LUXURY', 'CONVERTIBLE'];
const FUEL_TYPES = ['PETROL', 'DIESEL', 'ELECTRIC', 'HYBRID'];
const TRANSMISSIONS = ['MANUAL', 'AUTOMATIC'];
const STATUS_COLORS: Record<string, string> = {
  AVAILABLE: 'bg-green-100 text-green-700',
  RESERVED: 'bg-amber-100 text-amber-700',
  RENTED: 'bg-blue-100 text-blue-700',
  MAINTENANCE: 'bg-orange-100 text-orange-700',
  RETIRED: 'bg-slate-100 text-slate-500',
};

const vehicleSchema = z.object({
  plateNumber: z.string().min(1, 'Plate number is required'),
  vin: z.string().min(11, 'VIN must be at least 11 characters').max(17),
  type: z.string().min(1),
  brand: z.string().min(1, 'Brand is required'),
  model: z.string().min(1, 'Model is required'),
  year: z.coerce.number().min(1980).max(2100),
  mileage: z.coerce.number().min(0),
  fuelType: z.string().min(1),
  transmission: z.string().min(1),
  dailyRate: z.coerce.number().positive('Daily rate must be greater than 0'),
  branchId: z.coerce.number().min(1, 'Branch is required'),
});

type VehicleForm = z.infer<typeof vehicleSchema>;

export function VehiclesPage() {
  const { hasAnyRole } = useAuth();
  const canManage = hasAnyRole('ADMIN', 'BRANCH_MANAGER', 'STAFF');
  const canDelete = hasAnyRole('ADMIN', 'BRANCH_MANAGER');
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const { data: vehicles, isLoading, isError, error } = useQuery({ queryKey: ['vehicles'], queryFn: vehicleApi.list });
  const { data: branches } = useQuery({ queryKey: ['branches'], queryFn: branchApi.list });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<VehicleForm>({
    resolver: zodResolver(vehicleSchema),
    defaultValues: { type: 'SEDAN', fuelType: 'PETROL', transmission: 'AUTOMATIC' },
  });

  const createMutation = useMutation({
    mutationFn: (payload: VehicleForm) => vehicleApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
      reset();
      setShowForm(false);
      setFormError(null);
    },
    onError: (err) => setFormError(extractErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => vehicleApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['vehicles'] }),
  });

  if (isLoading) return <LoadingSpinner label="Loading vehicles..." />;
  if (isError) return <ErrorAlert message={extractErrorMessage(error)} />;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Vehicles</h1>
        {canManage && (
          <button
            onClick={() => setShowForm((v) => !v)}
            className="rounded-md bg-brand-500 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-600"
          >
            {showForm ? 'Cancel' : 'Add Vehicle'}
          </button>
        )}
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit((v) => createMutation.mutate(v))}
          className="grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-3"
        >
          <div>
            <input placeholder="Plate number" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('plateNumber')} />
            {errors.plateNumber && <p className="text-xs text-red-600">{errors.plateNumber.message}</p>}
          </div>
          <div>
            <input placeholder="VIN" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('vin')} />
            {errors.vin && <p className="text-xs text-red-600">{errors.vin.message}</p>}
          </div>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('branchId')}>
            <option value="">Select branch</option>
            {branches?.map((b) => (
              <option key={b.id} value={b.id}>{b.name}</option>
            ))}
          </select>
          <div>
            <input placeholder="Brand" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('brand')} />
            {errors.brand && <p className="text-xs text-red-600">{errors.brand.message}</p>}
          </div>
          <div>
            <input placeholder="Model" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('model')} />
            {errors.model && <p className="text-xs text-red-600">{errors.model.message}</p>}
          </div>
          <input type="number" placeholder="Year" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('year')} />
          <input type="number" placeholder="Mileage" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('mileage')} />
          <input type="number" step="0.01" placeholder="Daily rate ($)" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('dailyRate')} />
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('type')}>
            {VEHICLE_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('fuelType')}>
            {FUEL_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('transmission')}>
            {TRANSMISSIONS.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>

          <div className="sm:col-span-3">
            <ErrorAlert message={formError} />
          </div>
          <button
            type="submit"
            disabled={createMutation.isPending}
            className="rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60 sm:col-span-3"
          >
            {createMutation.isPending ? 'Saving...' : 'Save Vehicle'}
          </button>
        </form>
      )}

      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-2">Plate</th>
              <th className="px-4 py-2">Vehicle</th>
              <th className="px-4 py-2">Branch</th>
              <th className="px-4 py-2">Daily Rate</th>
              <th className="px-4 py-2">Status</th>
              {canDelete && <th className="px-4 py-2" />}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {vehicles?.map((v) => (
              <tr key={v.id}>
                <td className="px-4 py-2 font-mono text-xs">{v.plateNumber}</td>
                <td className="px-4 py-2">{v.brand} {v.model} ({v.year}) - {v.type}</td>
                <td className="px-4 py-2">{v.branchName}</td>
                <td className="px-4 py-2">${v.dailyRate.toFixed(2)}/day</td>
                <td className="px-4 py-2">
                  <span className={`rounded-full px-2 py-0.5 text-xs ${STATUS_COLORS[v.status]}`}>{v.status}</span>
                </td>
                {canDelete && (
                  <td className="px-4 py-2">
                    <button onClick={() => deleteMutation.mutate(v.id)} className="text-red-600 hover:text-red-700">
                      Delete
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
