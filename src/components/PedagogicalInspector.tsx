import { DIFFICULTY_OPTIONS, QuestionItem, getBloomMeta } from '../types/edusoal';
import { ReviewStatusBadge } from './BloomBadge';

export function PedagogicalInspector({ item }: { item: QuestionItem }) {
  const bloomMeta = getBloomMeta(item.bloomLevel);
  const diffLabel =
    DIFFICULTY_OPTIONS.find((d) => d.value === item.difficulty)?.label || item.difficulty;

  const rows: { label: string; value: string }[] = [
    { label: 'Subject & Level', value: `${item.subject} · ${item.gradeLevel}` },
    { label: 'Curriculum Alignment', value: item.curriculumAlignment || 'General Competency' },
    { label: "Bloom's Taxonomy", value: bloomMeta.title },
    { label: 'Difficulty Tier', value: diffLabel },
    { label: 'Est. Completion Time', value: `${item.estimatedMinutes} minutes` },
    { label: 'Discrimination Index', value: `${item.discriminationIndex.toFixed(2)} (Target)` },
  ];

  return (
    <div className="space-y-3">
      <div className="edusoal-card overflow-hidden">
        <div className="flex items-center justify-between border-b border-[#E2E8F0] bg-[#F8FAFC] px-3.5 py-2.5">
          <span className="text-[11px] font-semibold uppercase tracking-[0.05em] text-[#0F172A]">
            Pedagogical Metadata
          </span>
          <ReviewStatusBadge status={item.reviewStatus} />
        </div>
        <div className="divide-y divide-[#F1F5F9]">
          {rows.map((row) => (
            <div key={row.label} className="flex items-start justify-between gap-3 px-3.5 py-2.5 text-xs">
              <span className="text-[#64748B]">{row.label}</span>
              <span className="text-right font-semibold text-[#0F172A]">{row.value}</span>
            </div>
          ))}
        </div>
      </div>

      {item.pedagogicalRationale && (
        <div className="edusoal-card p-3.5">
          <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-[#2563EB]">
            Pedagogical Rationale
          </div>
          <p className="mt-1.5 whitespace-pre-line text-xs leading-relaxed text-[#0F172A]">
            {item.pedagogicalRationale}
          </p>
        </div>
      )}

      {item.scoringRubric && (
        <div className="edusoal-card p-3.5">
          <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-[#059669]">
            Scoring Rubric
          </div>
          <p className="mt-1.5 whitespace-pre-line text-xs leading-relaxed text-[#0F172A]">
            {item.scoringRubric}
          </p>
        </div>
      )}
    </div>
  );
}
