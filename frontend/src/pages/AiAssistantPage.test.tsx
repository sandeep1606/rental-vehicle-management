import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AiAssistantPage } from './AiAssistantPage';

vi.mock('@/api/endpoints', () => ({
  aiApi: { chat: vi.fn() },
}));

import { aiApi } from '@/api/endpoints';

function renderPage() {
  const client = new QueryClient();
  return render(
    <QueryClientProvider client={client}>
      <AiAssistantPage />
    </QueryClientProvider>,
  );
}

describe('AiAssistantPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders suggested prompts before any message is sent', () => {
    renderPage();
    expect(screen.getByText(/find me an suv in location 2/i)).toBeInTheDocument();
  });

  it('sends a message and renders the assistant reply with intent and tools used', async () => {
    vi.mocked(aiApi.chat).mockResolvedValue({
      sessionId: 'session-1',
      reply: 'Here is what is available: Toyota Camry at Downtown Central.',
      intent: 'VEHICLE_SEARCH',
      toolsUsed: ['VehicleSearchTool.searchAvailableVehicles'],
    });

    renderPage();
    await userEvent.type(screen.getByPlaceholderText(/ask about vehicles/i), 'Find an SUV');
    await userEvent.click(screen.getByRole('button', { name: /send/i }));

    expect(await screen.findByText(/here is what is available/i)).toBeInTheDocument();
    expect(screen.getByText(/intent: vehicle_search/i)).toBeInTheDocument();
    expect(aiApi.chat).toHaveBeenCalledWith('Find an SUV', undefined);
  });
});
