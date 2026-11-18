import { useState } from 'react';
import { Download, KeyRound, X } from 'lucide-react';
import { getStoredClientApiKey, setStoredClientApiKey } from '../services/geminiService';

interface SettingsModalProps {
  onClose: () => void;
  onExportBackup: () => void;
  onSavedKey: (msg: string) => void;
}

export function SettingsModal({ onClose, onExportBackup, onSavedKey }: SettingsModalProps) {
  const [apiKey, setApiKey] = useState(getStoredClientApiKey());

  const handleSave = () => {
    setStoredClientApiKey(apiKey);
    onSavedKey(apiKey.trim() ? 'Browser Gemini API key saved.' : 'Browser Gemini API key cleared.');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0F172A]/30 p-4 backdrop-blur-[1px]">
      <div className="w-full max-w-lg overflow-hidden rounded-[8px] border border-[#E2E8F0] bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#E2E8F0] bg-[#F8FAFC] px-5 py-3.5">
          <h3 className="font-display text-base font-semibold text-[#0F172A]">
            Workspace &amp; Vercel AI Settings
          </h3>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close settings"
            className="rounded-[4px] p-1 text-[#64748B] hover:bg-[#E2E8F0]/60 hover:text-[#0F172A]"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="space-y-5 p-5">
          <div className="rounded-[4px] border border-[#E2E8F0] bg-[#F8FAFC] p-3.5 text-xs leading-relaxed text-[#45464D]">
            <p className="font-semibold text-[#0F172A]">Vercel Serverless Endpoint Active (`/api/generate`)</p>
            <p className="mt-1">
              Set <code className="rounded bg-[#E2E8F0] px-1 py-0.5 font-mono text-[11px]">GEMINI_API_KEY</code> in
              Vercel Project Settings → Environment Variables for zero-config server-side AI generation.
            </p>
          </div>

          <div>
            <label className="mb-1 flex items-center gap-1.5 text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
              <KeyRound className="h-3.5 w-3.5 text-[#2563EB]" />
              <span>Optional Browser Gemini API Key (Local Fallback)</span>
            </label>
            <input
              type="password"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="AIza..."
              className="edusoal-input"
            />
            <p className="mt-1 text-xs text-[#64748B]">
              Stored only in this browser&apos;s localStorage if `/api/generate` does not have a server key configured.
            </p>
          </div>

          <div className="border-t border-[#E2E8F0] pt-4">
            <div className="text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
              Data Portability
            </div>
            <p className="mt-1 text-xs text-[#64748B]">
              Download your full Item Bank and Assessment Matrices as a portable JSON archive.
            </p>
            <button
              type="button"
              onClick={onExportBackup}
              className="mt-2.5 inline-flex items-center gap-2 rounded-[4px] border border-[#E2E8F0] bg-white px-3.5 py-2 text-xs font-semibold text-[#0F172A] hover:bg-[#F1F5F9]"
            >
              <Download className="h-3.5 w-3.5" />
              <span>Export Workspace JSON</span>
            </button>
          </div>
        </div>

        <div className="flex items-center justify-end gap-2 border-t border-[#E2E8F0] bg-[#F8FAFC] px-5 py-3">
          <button
            type="button"
            onClick={onClose}
            className="rounded-[4px] border border-[#E2E8F0] bg-white px-4 py-2 text-xs font-semibold text-[#64748B] hover:bg-[#F1F5F9]"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSave}
            className="rounded-[4px] bg-[#0F172A] px-4 py-2 text-xs font-semibold text-white hover:bg-[#1E293B]"
          >
            Save Settings
          </button>
        </div>
      </div>
    </div>
  );
}
