import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { maintenanceApi } from '@/api/endpoints';
import { extractErrorMessage } from '@/api/client';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorAlert } from '@/components/common/ErrorAlert';

const maintenanceSchema = z.object({
  vehicleId: z.coerce.number().min(1, 'Vehicle id is required'),
  description: z.string().min(1, 'Description is required'),
  cost: z.coerce.number().optional(),
  scheduledDate: z.string().min(1, 'Scheduled date is required'),
});

type MaintenanceForm = z.infer<typeof maintenanceSchema>;

const STATUS_COLORS: Record<string, string> = {
  SCHEDULED: 'bg-amber-100 text-amber-700',
  IN_PROGRESS: 'bg-orange-100 text-orange-700',
  COMPLETED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-slate-100 text-slate-500',
};

export function MaintenancePage() {
  const queryClient = useQueryClient();
  const [formError, setFormError] = useState<string | null>(null);
  const [completeDates, setCompleteDates] = useState<Record<number, string>>({});

  const { data: records, isLoading, isError, error } = useQuery({
    queryKey: ['maintenance'],
    queryFn: maintenanceApi.list,
  });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<MaintenanceForm>({
    resolver: zodResolver(maintenanceSchema),
  });

  const createMutation = useMutation({
    mutationFn: (payload: MaintenanceForm) => maintenanceApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['maintenance'] });
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
      reset();
      setFormError(null);
    },
    onError: (err) => setFormError(extractErrorMessage(err)),
  });

  const completeMutation = useMutation({
    mutationFn: ({ id, date }: { id: number; date: string }) => maintenanceApi.complete(id, date),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['maintenance'] });
      queryClient.invalidateQueries({ queryKey: ['vehicles'] });
    },
  });

  if (isLoading) return <LoadingSpinner label="Loading maintenance records..." />;
  if (isError) return <ErrorAlert message={extractErrorMessage(error)} />;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-800">Maintenance</h1>

      <form
        onSubmit={handleSubmit((v) => createMutation.mutate(v))}
        className="grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-4"
      >
        <div>
          <input type="number" placeholder="Vehicle ID" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('vehicleId')} />
          {errors.vehicleId && <p className="text-xs text-red-600">{errors.vehicleId.message}</p>}
        </div>
        <div className="sm:col-span-2">
          <input placeholder="Description" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('description')} />
          {errors.description && <p className="text-xs text-red-600">{errors.description.message}</p>}
        </div>
        <input type="number" step="0.01" placeholder="Cost ($)" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('cost')} />
        <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm sm:col-span-2" {...register('scheduledDate')} />
        <div className="sm:col-span-4">
          <ErrorAlert message={formError} />
        </div>
        <button
          type="submit"
          disabled={createMutation.isPending}
          className="rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60 sm:col-span-4"
        >
          {createMutation.isPending ? 'Scheduling...' : 'Schedule Maintenance'}
        </button>
      </form>

      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-2">Vehicle</th>
              <th className="px-4 py-2">Description</th>
              <th className="px-4 py-2">Scheduled</th>
              <th className="px-4 py-2">Cost</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2" />
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {records?.map((m) => (
              <tr key={m.id}>
                <td className="px-4 py-2 font-mono text-xs">{m.vehiclePlate}</td>
                <td className="px-4 py-2">{m.description}</td>
                <td className="px-4 py-2">{m.scheduledDate}</td>
                <td className="px-4 py-2">{m.cost != null ? `$${m.cost.toFixed(2)}` : '-'}</td>
                <td className="px-4 py-2">
                  <span className={`rounded-full px-2 py-0.5 text-xs ${STATUS_COLORS[m.status]}`}>{m.status}</span>
                </td>
                <td className="space-x-2 px-4 py-2">
                  {(m.status === 'SCHEDULED' || m.status === 'IN_PROGRESS') && (
                    <>
                      <input
                        type="date"
                        value={completeDates[m.id] ?? ''}
                        onChange={(e) => setCompleteDates((prev) => ({ ...prev, [m.id]: e.target.value }))}
                        className="rounded-md border border-slate-300 px-2 py-1 text-xs"
                      />
                      <button
                        onClick={() => completeDates[m.id] && completeMutation.mutate({ id: m.id, date: completeDates[m.id] })}
                        className="text-brand-600 hover:text-brand-700"
                      >
                        Mark Completed
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
