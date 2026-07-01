import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { customerApi } from '@/api/endpoints';
import { extractErrorMessage } from '@/api/client';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorAlert } from '@/components/common/ErrorAlert';
import { useAuth } from '@/context/AuthContext';

const customerSchema = z.object({
  fullName: z.string().min(1, 'Full name is required'),
  email: z.string().email('Enter a valid email'),
  phone: z.string().min(1, 'Phone is required'),
  driverLicenseNumber: z.string().min(1, 'License number is required'),
  address: z.string().optional(),
  dateOfBirth: z.string().optional(),
});

type CustomerForm = z.infer<typeof customerSchema>;

export function CustomersPage() {
  const { hasAnyRole } = useAuth();
  const canManage = hasAnyRole('ADMIN', 'BRANCH_MANAGER', 'STAFF');
  const canDelete = hasAnyRole('ADMIN', 'BRANCH_MANAGER');
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  const isSearching = searchTerm.trim().length > 0;
  const { data: customers, isLoading, isError, error } = useQuery({
    queryKey: isSearching ? ['customers', 'search', searchTerm] : ['customers'],
    queryFn: () => (isSearching ? customerApi.search(searchTerm) : customerApi.list()),
  });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CustomerForm>({
    resolver: zodResolver(customerSchema),
  });

  const createMutation = useMutation({
    mutationFn: (payload: CustomerForm) => customerApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      reset();
      setShowForm(false);
      setFormError(null);
    },
    onError: (err) => setFormError(extractErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => customerApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['customers'] }),
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Customers</h1>
        {canManage && (
          <button
            onClick={() => setShowForm((v) => !v)}
            className="rounded-md bg-brand-500 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-600"
          >
            {showForm ? 'Cancel' : 'Add Customer'}
          </button>
        )}
      </div>

      <input
        placeholder="Search by name, email, phone, or license number (LRU-cached)"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
      />

      {showForm && (
        <form
          onSubmit={handleSubmit((v) => createMutation.mutate(v))}
          className="grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-2"
        >
          <div>
            <input placeholder="Full name" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('fullName')} />
            {errors.fullName && <p className="text-xs text-red-600">{errors.fullName.message}</p>}
          </div>
          <div>
            <input placeholder="Email" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('email')} />
            {errors.email && <p className="text-xs text-red-600">{errors.email.message}</p>}
          </div>
          <div>
            <input placeholder="Phone" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('phone')} />
            {errors.phone && <p className="text-xs text-red-600">{errors.phone.message}</p>}
          </div>
          <div>
            <input placeholder="Driver license number" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('driverLicenseNumber')} />
            {errors.driverLicenseNumber && <p className="text-xs text-red-600">{errors.driverLicenseNumber.message}</p>}
          </div>
          <input placeholder="Address" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm sm:col-span-2" {...register('address')} />
          <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('dateOfBirth')} />

          <div className="sm:col-span-2">
            <ErrorAlert message={formError} />
          </div>
          <button
            type="submit"
            disabled={createMutation.isPending}
            className="rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60 sm:col-span-2"
          >
            {createMutation.isPending ? 'Saving...' : 'Save Customer'}
          </button>
        </form>
      )}

      {isLoading ? (
        <LoadingSpinner label="Loading customers..." />
      ) : isError ? (
        <ErrorAlert message={extractErrorMessage(error)} />
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-2">Name</th>
                <th className="px-4 py-2">Email</th>
                <th className="px-4 py-2">Phone</th>
                <th className="px-4 py-2">License</th>
                <th className="px-4 py-2">Status</th>
                {canDelete && <th className="px-4 py-2" />}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {customers?.map((c) => (
                <tr key={c.id}>
                  <td className="px-4 py-2 font-medium text-slate-700">{c.fullName}</td>
                  <td className="px-4 py-2">{c.email}</td>
                  <td className="px-4 py-2">{c.phone}</td>
                  <td className="px-4 py-2 font-mono text-xs">{c.driverLicenseNumber}</td>
                  <td className="px-4 py-2">
                    {c.blacklisted ? (
                      <span className="rounded-full bg-red-100 px-2 py-0.5 text-xs text-red-700">Blacklisted</span>
                    ) : (
                      <span className="rounded-full bg-green-100 px-2 py-0.5 text-xs text-green-700">Good standing</span>
                    )}
                  </td>
                  {canDelete && (
                    <td className="px-4 py-2">
                      <button onClick={() => deleteMutation.mutate(c.id)} className="text-red-600 hover:text-red-700">
                        Delete
                      </button>
                    </td>
                  )}
                </tr>
              ))}
              {customers?.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-6 text-center text-slate-400">No customers found.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
