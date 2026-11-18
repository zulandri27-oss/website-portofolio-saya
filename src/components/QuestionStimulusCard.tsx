import { BookmarkCheck, BookmarkPlus, BookOpen, CheckCircle2, Clock, Edit3, SlidersHorizontal } from 'lucide-react';
import { FORMAT_OPTIONS, QuestionItem } from '../types/edusoal';
import { BloomBadge } from './BloomBadge';

interface QuestionStimulusCardProps {
  item: QuestionItem;
  showAnswerKey?: boolean;
  interactiveKeySelection?: boolean;
  isSavedToBank?: boolean;
  onSelectOption?: (index: number) => void;
  onSaveToBank?: () => void;
  onEditItem?: () => void;
  onOpenInspector?: () => void;
}

export function QuestionStimulusCard({
  item,
  showAnswerKey = true,
  interactiveKeySelection = true,
  isSavedToBank = false,
  onSelectOption,
  onSaveToBank,
  onEditItem,
  onOpenInspector,
}: QuestionStimulusCardProps) {
  const formatLabel =
    FORMAT_OPTIONS.find((f) => f.value === item.questionFormat)?.label || 'Multiple Choice';

  return (
    <div className="edusoal-card-elevated overflow-hidden">
      {/* Header Bar */}
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[#E2E8F0] bg-[#F8FAFC] px-4 py-2.5">
        <div className="flex flex-wrap items-center gap-2">
          <BloomBadge code={item.bloomLevel} />
          <span className="inline-flex items-center rounded-[4px] border border-[#E2E8F0] bg-white px-2 py-0.5 text-[11px] font-semibold uppercase tracking-[0.05em] text-[#64748B]">
            {formatLabel}
          </span>
        </div>
        <div className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-[0.04em] text-[#64748B]">
          <Clock className="h-3.5 w-3.5" />
          <span>{item.estimatedMinutes} min</span>
        </div>
      </div>

      {/* Upper Quadrant: Context Stimulus */}
      {(item.stimulusTitle || item.stimulusContent) && (
        <div className="flex border-b border-[#E2E8F0] bg-[#F8FAFC]/70">
          <div className="w-1 shrink-0 bg-[#2563EB]" />
          <div className="flex-1 p-4 sm:p-5">
            <div className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-[0.05em] text-[#2563EB]">
              <BookOpen className="h-3.5 w-3.5" />
              <span>Context Stimulus</span>
            </div>
            {item.stimulusTitle && (
              <h4 className="mt-1.5 font-display text-[15px] font-semibold text-[#0F172A]">
                {item.stimulusTitle}
              </h4>
            )}
            {item.stimulusContent && (
              <p className="mt-1.5 whitespace-pre-line text-sm leading-relaxed text-[#45464D]">
                {item.stimulusContent}
              </p>
            )}
          </div>
        </div>
      )}

      {/* Lower Quadrant: Question Stem & Options */}
      <div className="p-4 sm:p-5">
        <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-[#64748B]">
          Item Stem
        </div>
        <p className="mt-1.5 font-display text-[15px] font-semibold leading-snug text-[#0F172A]">
          {item.questionStem}
        </p>

        {item.questionFormat !== 'ESSAY' && item.options.length > 0 ? (
          <div className="mt-4 space-y-2">
            {item.options.map((optionText, idx) => {
              const letter = String.fromCharCode(65 + idx);
              const isKey = showAnswerKey && idx === item.correctOptionIndex;
              return (
                <button
                  key={`${idx}-${letter}`}
                  type="button"
                  disabled={!interactiveKeySelection}
                  onClick={() => interactiveKeySelection && onSelectOption?.(idx)}
                  className={`flex min-h-[46px] w-full items-center gap-3 rounded-[4px] border px-3 py-2.5 text-left transition-all ${
                    isKey
                      ? 'border-[#059669] bg-[#ECFDF5] text-[#065F46]'
                      : 'border-[#E2E8F0] bg-white text-[#0F172A]'
                  } ${
                    interactiveKeySelection && !isKey
                      ? 'hover:border-[#CBD5E1] hover:bg-[#F8FAFC]'
                      : ''
                  }`}
                >
                  <span
                    className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-[4px] border text-xs font-semibold ${
                      isKey
                        ? 'border-[#059669] bg-[#059669] text-white'
                        : 'border-[#E2E8F0] bg-[#F8FAFC] text-[#0F172A]'
                    }`}
                  >
                    {letter}
                  </span>
                  <span className={`flex-1 text-sm ${isKey ? 'font-semibold' : 'font-normal'}`}>
                    {optionText}
                  </span>
                  {isKey ? (
                    <CheckCircle2 className="h-4 w-4 shrink-0 text-[#059669]" />
                  ) : (
                    <span className="h-4 w-4 shrink-0 rounded-full border border-[#CBD5E1]" />
                  )}
                </button>
              );
            })}
            {interactiveKeySelection && showAnswerKey && (
              <p className="pt-1 text-xs text-[#64748B]">
                Click any option above to reassign the verified answer key.
              </p>
            )}
          </div>
        ) : (
          item.scoringRubric &&
          showAnswerKey && (
            <div className="mt-4 rounded-[4px] border border-[#A7F3D0] bg-[#ECFDF5] p-3">
              <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-[#065F46]">
                Constructed Response Rubric
              </div>
              <p className="mt-1 whitespace-pre-line text-xs leading-relaxed text-[#065F46]">
                {item.scoringRubric}
              </p>
            </div>
          )
        )}
      </div>

      {/* Action Strip */}
      {(onSaveToBank || onEditItem || onOpenInspector) && (
        <div className="no-print flex flex-wrap items-center justify-between gap-2 border-t border-[#E2E8F0] bg-[#F8FAFC] px-4 py-2.5">
          <div className="flex flex-wrap items-center gap-2">
            {onOpenInspector && (
              <button
                type="button"
                onClick={onOpenInspector}
                className="inline-flex min-h-[38px] items-center gap-1.5 rounded-[4px] border border-[#E2E8F0] bg-white px-3 py-1.5 text-xs font-semibold text-[#0F172A] transition-colors hover:bg-[#F1F5F9] xl:hidden"
              >
                <SlidersHorizontal className="h-3.5 w-3.5" />
                <span>Metadata</span>
              </button>
            )}
            {onEditItem && (
              <button
                type="button"
                onClick={onEditItem}
                className="inline-flex min-h-[38px] items-center gap-1.5 rounded-[4px] border border-[#E2E8F0] bg-white px-3 py-1.5 text-xs font-semibold text-[#0F172A] transition-colors hover:bg-[#F1F5F9]"
              >
                <Edit3 className="h-3.5 w-3.5" />
                <span>Edit</span>
              </button>
            )}
          </div>
          {onSaveToBank && (
            <button
              type="button"
              disabled={isSavedToBank}
              onClick={onSaveToBank}
              className={`inline-flex min-h-[38px] items-center gap-1.5 rounded-[4px] px-3.5 py-1.5 text-xs font-semibold transition-all ${
                isSavedToBank
                  ? 'border border-[#A7F3D0] bg-[#ECFDF5] text-[#065F46]'
                  : 'bg-[#0F172A] text-white hover:bg-[#1E293B] active:scale-[0.995]'
              }`}
            >
              {isSavedToBank ? (
                <>
                  <BookmarkCheck className="h-3.5 w-3.5" />
                  <span>Saved to Bank</span>
                </>
              ) : (
                <>
                  <BookmarkPlus className="h-3.5 w-3.5" />
                  <span>Save to Item Bank</span>
                </>
              )}
            </button>
          )}
        </div>
      )}
    </div>
  );
}
