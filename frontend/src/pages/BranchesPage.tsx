import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { branchApi } from '@/api/endpoints';
import { extractErrorMessage } from '@/api/client';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorAlert } from '@/components/common/ErrorAlert';
import { useAuth } from '@/context/AuthContext';
import type { Branch } from '@/types';

const branchSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  address: z.string().min(1, 'Address is required'),
  phone: z.string().min(1, 'Phone is required'),
  managerName: z.string().optional(),
  openingHours: z.string().optional(),
});

type BranchForm = z.infer<typeof branchSchema>;

export function BranchesPage() {
  const { hasAnyRole } = useAuth();
  const canManage = hasAnyRole('ADMIN', 'BRANCH_MANAGER');
  const queryClient = useQueryClient();
  const [formError, setFormError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const { data: branches, isLoading, isError, error } = useQuery({
    queryKey: ['branches'],
    queryFn: branchApi.list,
  });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<BranchForm>({
    resolver: zodResolver(branchSchema),
  });

  const createMutation = useMutation({
    mutationFn: (payload: BranchForm) => branchApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['branches'] });
      reset();
      setShowForm(false);
      setFormError(null);
    },
    onError: (err) => setFormError(extractErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => branchApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['branches'] }),
  });

  if (isLoading) return <LoadingSpinner label="Loading branches..." />;
  if (isError) return <ErrorAlert message={extractErrorMessage(error)} />;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Branches</h1>
        {canManage && (
          <button
            onClick={() => setShowForm((v) => !v)}
            className="rounded-md bg-brand-500 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-600"
          >
            {showForm ? 'Cancel' : 'Add Branch'}
          </button>
        )}
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit((v) => createMutation.mutate(v))}
          className="grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-2"
        >
          <div>
            <input placeholder="Name" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('name')} />
            {errors.name && <p className="text-xs text-red-600">{errors.name.message}</p>}
          </div>
          <div>
            <input placeholder="Phone" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('phone')} />
            {errors.phone && <p className="text-xs text-red-600">{errors.phone.message}</p>}
          </div>
          <div className="sm:col-span-2">
            <input placeholder="Address" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('address')} />
            {errors.address && <p className="text-xs text-red-600">{errors.address.message}</p>}
          </div>
          <input placeholder="Manager name" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('managerName')} />
          <input placeholder="Opening hours" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('openingHours')} />
          <div className="sm:col-span-2">
            <ErrorAlert message={formError} />
          </div>
          <button
            type="submit"
            disabled={createMutation.isPending}
            className="rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60 sm:col-span-2"
          >
            {createMutation.isPending ? 'Saving...' : 'Save Branch'}
          </button>
        </form>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-2">
        {branches?.map((b: Branch) => (
          <div key={b.id} className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="font-semibold text-slate-800">{b.name}</h3>
                <p className="text-sm text-slate-500">{b.address}</p>
              </div>
              <span className={`rounded-full px-2 py-0.5 text-xs ${b.active ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-500'}`}>
                {b.active ? 'Active' : 'Inactive'}
              </span>
            </div>
            <dl className="mt-3 space-y-1 text-sm text-slate-600">
              <div>Phone: {b.phone}</div>
              {b.managerName && <div>Manager: {b.managerName}</div>}
              {b.openingHours && <div>Hours: {b.openingHours}</div>}
            </dl>
            {canManage && (
              <button
                onClick={() => deleteMutation.mutate(b.id)}
                className="mt-3 text-sm font-medium text-red-600 hover:text-red-700"
              >
                Delete
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
