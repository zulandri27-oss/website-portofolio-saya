import { CheckCircle2 } from 'lucide-react';
import { BloomCode, ReviewStatus, getBloomMeta } from '../types/edusoal';

export function BloomBadge({ code }: { code: BloomCode }) {
  const meta = getBloomMeta(code);
  const styles =
    meta.tierGroup === 'LOW'
      ? 'bg-[#F1F5F9] text-[#334155] border-[#E2E8F0]'
      : meta.tierGroup === 'MID'
      ? 'bg-[#FFFBEB] text-[#92400E] border-[#FDE68A]'
      : 'bg-[#FFF7ED] text-[#9A3412] border-[#FED7AA]';

  return (
    <span
      className={`inline-flex items-center rounded-[4px] border px-1.5 py-0.5 text-[11px] font-semibold uppercase tracking-[0.05em] ${styles}`}
    >
      {meta.code} · {meta.shortTitle}
    </span>
  );
}

export function ReviewStatusBadge({ status }: { status: ReviewStatus }) {
  const styles =
    status === 'APPROVED'
      ? 'bg-[#ECFDF5] text-[#065F46] border-[#A7F3D0]'
      : status === 'VALIDATED'
      ? 'bg-[#EFF6FF] text-[#2563EB] border-[#BFDBFE]'
      : 'bg-[#F1F5F9] text-[#334155] border-[#E2E8F0]';

  return (
    <span
      className={`inline-flex items-center gap-1 rounded-[4px] border px-1.5 py-0.5 text-[11px] font-semibold uppercase tracking-[0.05em] ${styles}`}
    >
      {status === 'APPROVED' && <CheckCircle2 className="h-3 w-3" />}
      {status}
    </span>
  );
}
