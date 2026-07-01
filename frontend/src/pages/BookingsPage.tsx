import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { bookingApi, paymentApi } from '@/api/endpoints';
import { extractErrorMessage } from '@/api/client';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorAlert } from '@/components/common/ErrorAlert';

const reservationSchema = z.object({
  customerId: z.coerce.number().min(1, 'Customer id is required'),
  vehicleId: z.coerce.number().min(1, 'Vehicle id is required'),
  startDate: z.string().min(1, 'Start date is required'),
  endDate: z.string().min(1, 'End date is required'),
});

type ReservationForm = z.infer<typeof reservationSchema>;

export function BookingsPage() {
  const queryClient = useQueryClient();
  const [formError, setFormError] = useState<string | null>(null);
  const [returnDates, setReturnDates] = useState<Record<number, string>>({});

  const { data: reservations, isLoading: loadingReservations } = useQuery({
    queryKey: ['reservations'],
    queryFn: bookingApi.reservations,
  });
  const { data: rentals, isLoading: loadingRentals } = useQuery({
    queryKey: ['rentals'],
    queryFn: bookingApi.rentals,
  });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<ReservationForm>({
    resolver: zodResolver(reservationSchema),
  });

  const invalidateBookings = () => {
    queryClient.invalidateQueries({ queryKey: ['reservations'] });
    queryClient.invalidateQueries({ queryKey: ['rentals'] });
    queryClient.invalidateQueries({ queryKey: ['vehicles'] });
  };

  const createReservation = useMutation({
    mutationFn: (payload: ReservationForm) => bookingApi.createReservation(payload),
    onSuccess: () => {
      invalidateBookings();
      reset();
      setFormError(null);
    },
    onError: (err) => setFormError(extractErrorMessage(err)),
  });

  const cancelReservation = useMutation({
    mutationFn: (id: number) => bookingApi.cancelReservation(id),
    onSuccess: invalidateBookings,
  });

  const convertReservation = useMutation({
    mutationFn: (id: number) => bookingApi.convertReservation(id),
    onSuccess: invalidateBookings,
  });

  const returnVehicle = useMutation({
    mutationFn: ({ id, date }: { id: number; date: string }) => bookingApi.returnVehicle(id, date),
    onSuccess: invalidateBookings,
  });

  const processPayment = useMutation({
    mutationFn: (rentalId: number) => paymentApi.process(rentalId, 'CREDIT_CARD'),
    onSuccess: invalidateBookings,
  });

  return (
    <div className="space-y-8">
      <div>
        <h1 className="mb-3 text-xl font-semibold text-slate-800">Create Reservation</h1>
        <form
          onSubmit={handleSubmit((v) => createReservation.mutate(v))}
          className="grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-4"
        >
          <div>
            <input type="number" placeholder="Customer ID" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('customerId')} />
            {errors.customerId && <p className="text-xs text-red-600">{errors.customerId.message}</p>}
          </div>
          <div>
            <input type="number" placeholder="Vehicle ID" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('vehicleId')} />
            {errors.vehicleId && <p className="text-xs text-red-600">{errors.vehicleId.message}</p>}
          </div>
          <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('startDate')} />
          <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register('endDate')} />
          <div className="sm:col-span-4">
            <ErrorAlert message={formError} />
          </div>
          <button
            type="submit"
            disabled={createReservation.isPending}
            className="rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60 sm:col-span-4"
          >
            {createReservation.isPending ? 'Booking...' : 'Create Reservation'}
          </button>
        </form>
        <p className="mt-1 text-xs text-slate-400">
          Tip: find vehicle and customer IDs from the Vehicles / Customers pages, or ask the AI Assistant.
        </p>
      </div>

      <div>
        <h2 className="mb-3 text-lg font-semibold text-slate-800">Reservations</h2>
        {loadingReservations ? (
          <LoadingSpinner />
        ) : (
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-2">ID</th>
                  <th className="px-4 py-2">Customer</th>
                  <th className="px-4 py-2">Vehicle</th>
                  <th className="px-4 py-2">Dates</th>
                  <th className="px-4 py-2">Est. Total</th>
                  <th className="px-4 py-2">Status</th>
                  <th className="px-4 py-2" />
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {reservations?.map((r) => (
                  <tr key={r.id}>
                    <td className="px-4 py-2">{r.id}</td>
                    <td className="px-4 py-2">{r.customerName}</td>
                    <td className="px-4 py-2 font-mono text-xs">{r.vehiclePlate}</td>
                    <td className="px-4 py-2">{r.startDate} to {r.endDate}</td>
                    <td className="px-4 py-2">{r.estimatedTotal != null ? `$${r.estimatedTotal.toFixed(2)}` : '-'}</td>
                    <td className="px-4 py-2">{r.status}</td>
                    <td className="space-x-2 px-4 py-2">
                      {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
                        <>
                          <button onClick={() => convertReservation.mutate(r.id)} className="text-brand-600 hover:text-brand-700">
                            Convert to Rental
                          </button>
                          <button onClick={() => cancelReservation.mutate(r.id)} className="text-red-600 hover:text-red-700">
                            Cancel
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div>
        <h2 className="mb-3 text-lg font-semibold text-slate-800">Rentals</h2>
        {loadingRentals ? (
          <LoadingSpinner />
        ) : (
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-2">ID</th>
                  <th className="px-4 py-2">Customer</th>
                  <th className="px-4 py-2">Vehicle</th>
                  <th className="px-4 py-2">Planned End</th>
                  <th className="px-4 py-2">Total</th>
                  <th className="px-4 py-2">Late Fee</th>
                  <th className="px-4 py-2">Status</th>
                  <th className="px-4 py-2" />
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {rentals?.map((r) => (
                  <tr key={r.id}>
                    <td className="px-4 py-2">{r.id}</td>
                    <td className="px-4 py-2">{r.customerName}</td>
                    <td className="px-4 py-2 font-mono text-xs">{r.vehiclePlate}</td>
                    <td className="px-4 py-2">{r.plannedEndDate}</td>
                    <td className="px-4 py-2">{r.totalAmount != null ? `$${r.totalAmount.toFixed(2)}` : '-'}</td>
                    <td className="px-4 py-2">${r.lateFee.toFixed(2)}</td>
                    <td className="px-4 py-2">{r.status}</td>
                    <td className="space-x-2 px-4 py-2">
                      {r.status === 'ACTIVE' && (
                        <>
                          <input
                            type="date"
                            value={returnDates[r.id] ?? ''}
                            onChange={(e) => setReturnDates((prev) => ({ ...prev, [r.id]: e.target.value }))}
                            className="rounded-md border border-slate-300 px-2 py-1 text-xs"
                          />
                          <button
                            onClick={() => returnDates[r.id] && returnVehicle.mutate({ id: r.id, date: returnDates[r.id] })}
                            className="text-brand-600 hover:text-brand-700"
                          >
                            Return
                          </button>
                        </>
                      )}
                      {r.status === 'COMPLETED' && (
                        <button onClick={() => processPayment.mutate(r.id)} className="text-green-600 hover:text-green-700">
                          Charge Payment
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
