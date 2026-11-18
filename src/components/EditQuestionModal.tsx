import { useState } from 'react';
import { X } from 'lucide-react';
import { QuestionItem } from '../types/edusoal';

interface EditQuestionModalProps {
  initialItem: QuestionItem;
  onClose: () => void;
  onSave: (updated: QuestionItem) => void;
}

export function EditQuestionModal({ initialItem, onClose, onSave }: EditQuestionModalProps) {
  const [stimulusTitle, setStimulusTitle] = useState(initialItem.stimulusTitle);
  const [stimulusContent, setStimulusContent] = useState(initialItem.stimulusContent);
  const [questionStem, setQuestionStem] = useState(initialItem.questionStem);
  const [options, setOptions] = useState<string[]>([...initialItem.options]);
  const [correctIdx, setCorrectIdx] = useState(initialItem.correctOptionIndex);
  const [rationale, setRationale] = useState(initialItem.pedagogicalRationale);
  const [rubric, setRubric] = useState(initialItem.scoringRubric);
  const [alignment, setAlignment] = useState(initialItem.curriculumAlignment);

  const handleSave = () => {
    if (!questionStem.trim()) return;
    const cleanedOptions = options.map((o) => o.trim()).filter(Boolean);
    onSave({
      ...initialItem,
      stimulusTitle: stimulusTitle.trim(),
      stimulusContent: stimulusContent.trim(),
      questionStem: questionStem.trim(),
      options: cleanedOptions,
      correctOptionIndex:
        cleanedOptions.length === 0 ? -1 : Math.max(0, Math.min(cleanedOptions.length - 1, correctIdx)),
      pedagogicalRationale: rationale.trim(),
      scoringRubric: rubric.trim(),
      curriculumAlignment: alignment.trim(),
      updatedAt: new Date().toISOString(),
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0F172A]/30 p-4 backdrop-blur-[1px]">
      <div className="flex max-h-[90vh] w-full max-w-2xl flex-col overflow-hidden rounded-[8px] border border-[#E2E8F0] bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#E2E8F0] bg-[#F8FAFC] px-5 py-3.5">
          <h3 className="font-display text-base font-semibold text-[#0F172A]">
            Edit Assessment Item
          </h3>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close editor"
            className="rounded-[4px] p-1 text-[#64748B] hover:bg-[#E2E8F0]/60 hover:text-[#0F172A]"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="flex-1 space-y-4 overflow-y-auto p-5">
          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
              Stimulus Title
            </label>
            <input
              type="text"
              value={stimulusTitle}
              onChange={(e) => setStimulusTitle(e.target.value)}
              className="edusoal-input"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
              Context Stimulus
            </label>
            <textarea
              rows={3}
              value={stimulusContent}
              onChange={(e) => setStimulusContent(e.target.value)}
              className="edusoal-input"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
              Question Stem
            </label>
            <textarea
              rows={2}
              value={questionStem}
              onChange={(e) => setQuestionStem(e.target.value)}
              className="edusoal-input"
            />
          </div>

          {options.length > 0 && (
            <div className="space-y-2">
              <label className="block text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
                Options (Click letter to set Answer Key)
              </label>
              {options.map((opt, idx) => {
                const isKey = idx === correctIdx;
                const letter = String.fromCharCode(65 + idx);
                return (
                  <div key={letter} className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => setCorrectIdx(idx)}
                      className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-[4px] border text-xs font-semibold ${
                        isKey
                          ? 'border-[#059669] bg-[#059669] text-white'
                          : 'border-[#CBD5E1] bg-[#F8FAFC] text-[#0F172A]'
                      }`}
                    >
                      {letter}
                    </button>
                    <input
                      type="text"
                      value={opt}
                      onChange={(e) => {
                        const next = [...options];
                        next[idx] = e.target.value;
                        setOptions(next);
                      }}
                      className="edusoal-input"
                    />
                  </div>
                );
              })}
            </div>
          )}

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
              Curriculum Alignment
            </label>
            <input
              type="text"
              value={alignment}
              onChange={(e) => setAlignment(e.target.value)}
              className="edusoal-input"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
              Pedagogical Rationale
            </label>
            <textarea
              rows={2}
              value={rationale}
              onChange={(e) => setRationale(e.target.value)}
              className="edusoal-input"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
              Scoring Rubric
            </label>
            <textarea
              rows={2}
              value={rubric}
              onChange={(e) => setRubric(e.target.value)}
              className="edusoal-input"
            />
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
            Save Changes
          </button>
        </div>
      </div>
    </div>
  );
}
