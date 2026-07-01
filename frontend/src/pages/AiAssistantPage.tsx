import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { aiApi } from '@/api/endpoints';
import { extractErrorMessage } from '@/api/client';
import { ErrorAlert } from '@/components/common/ErrorAlert';

interface ChatEntry {
  role: 'user' | 'assistant';
  text: string;
  intent?: string;
  toolsUsed?: string[];
}

const SUGGESTIONS = [
  'Find me an SUV in location 2 from July 5 to July 10 under $80/day',
  'What is the late return fee policy?',
  'Look up customer with license DL-100002',
  'How do I book a reservation?',
];

export function AiAssistantPage() {
  const [messages, setMessages] = useState<ChatEntry[]>([]);
  const [input, setInput] = useState('');
  const [sessionId, setSessionId] = useState<string | undefined>(undefined);

  const chatMutation = useMutation({
    mutationFn: (message: string) => aiApi.chat(message, sessionId),
    onSuccess: (data) => {
      setSessionId(data.sessionId);
      setMessages((prev) => [...prev, { role: 'assistant', text: data.reply, intent: data.intent, toolsUsed: data.toolsUsed }]);
    },
    onError: (err) => {
      setMessages((prev) => [...prev, { role: 'assistant', text: `Error: ${extractErrorMessage(err)}` }]);
    },
  });

  const send = (text: string) => {
    const trimmed = text.trim();
    if (!trimmed) return;
    setMessages((prev) => [...prev, { role: 'user', text: trimmed }]);
    setInput('');
    chatMutation.mutate(trimmed);
  };

  return (
    <div className="flex h-[calc(100vh-140px)] flex-col rounded-lg border border-slate-200 bg-white">
      <div className="border-b border-slate-200 p-4">
        <h1 className="text-lg font-semibold text-slate-800">AI Rental Assistant</h1>
        <p className="text-xs text-slate-400">
          Powered by LangChain4j + LangGraph4j. Answers real-time vehicle/customer data via backend tools,
          and rental policy questions via RAG. It never invents prices or booking confirmations.
        </p>
      </div>

      <div className="flex-1 space-y-3 overflow-y-auto p-4">
        {messages.length === 0 && (
          <div className="space-y-2">
            <p className="text-sm text-slate-500">Try asking:</p>
            {SUGGESTIONS.map((s) => (
              <button
                key={s}
                onClick={() => send(s)}
                className="block w-full rounded-md border border-slate-200 px-3 py-2 text-left text-sm text-slate-600 hover:bg-slate-50"
              >
                {s}
              </button>
            ))}
          </div>
        )}

        {messages.map((m, idx) => (
          <div key={idx} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div
              className={`max-w-[80%] whitespace-pre-wrap rounded-lg px-4 py-2 text-sm ${
                m.role === 'user' ? 'bg-brand-500 text-white' : 'bg-slate-100 text-slate-800'
              }`}
            >
              {m.text}
              {m.role === 'assistant' && (m.intent || (m.toolsUsed && m.toolsUsed.length > 0)) && (
                <div className="mt-2 border-t border-slate-200 pt-1 text-[11px] text-slate-400">
                  {m.intent && <div>Intent: {m.intent}</div>}
                  {m.toolsUsed && m.toolsUsed.length > 0 && <div>Tools used: {m.toolsUsed.join(', ')}</div>}
                </div>
              )}
            </div>
          </div>
        ))}

        {chatMutation.isPending && <p className="text-sm text-slate-400">Assistant is thinking...</p>}
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault();
          send(input);
        }}
        className="flex gap-2 border-t border-slate-200 p-4"
      >
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask about vehicles, customers, availability, or rental policy..."
          className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={chatMutation.isPending}
          className="rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
        >
          Send
        </button>
      </form>
      {chatMutation.isError && <ErrorAlert message={extractErrorMessage(chatMutation.error)} />}
    </div>
  );
}
