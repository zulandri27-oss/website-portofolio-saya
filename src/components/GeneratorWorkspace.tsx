import { useState } from 'react';
import {
  AlertCircle,
  ChevronDown,
  ChevronUp,
  Loader2,
  RefreshCw,
  Sparkles,
  X,
} from 'lucide-react';
import {
  BLOOM_LEVELS,
  CURRICULUM_TEMPLATES,
  DIFFICULTY_OPTIONS,
  FORMAT_OPTIONS,
  GeneratorFormState,
  QuestionItem,
  getBloomMeta,
} from '../types/edusoal';
import { QuestionStimulusCard } from './QuestionStimulusCard';
import { PedagogicalInspector } from './PedagogicalInspector';
import { EditQuestionModal } from './EditQuestionModal';

interface GeneratorWorkspaceProps {
  form: GeneratorFormState;
  generatedItem: QuestionItem | null;
  savedItemId: string | null;
  isGenerating: boolean;
  error: string | null;
  onChangeForm: (patch: Partial<GeneratorFormState>) => void;
  onGenerate: () => void;
  onUpdateGeneratedItem: (updated: QuestionItem) => void;
  onSaveToBank: () => void;
}

export function GeneratorWorkspace({
  form,
  generatedItem,
  savedItemId,
  isGenerating,
  error,
  onChangeForm,
  onGenerate,
  onUpdateGeneratedItem,
  onSaveToBank,
}: GeneratorWorkspaceProps) {
  const [showCustomStimulus, setShowCustomStimulus] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showInspectorDrawer, setShowInspectorDrawer] = useState(false);

  const selectedBloomMeta = getBloomMeta(form.bloomLevel);

  return (
    <div className="mx-auto max-w-[1440px] px-4 py-5 sm:px-6 lg:px-8">
      {/* 12-column asymmetric desktop grid: 3 / 6 / 3 */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
        {/* Left Pane: Item Specification (4 cols on lg, 3 cols on xl) */}
        <section className="lg:col-span-5 xl:col-span-3">
          <div className="edusoal-card overflow-hidden">
            <div className="border-b border-[#E2E8F0] bg-[#F8FAFC] px-4 py-3">
              <h2 className="text-xs font-semibold uppercase tracking-[0.05em] text-[#0F172A]">
                Item Specification
              </h2>
              <p className="mt-0.5 text-xs text-[#64748B]">
                Configure cognitive rigor, stimulus, and format
              </p>
            </div>

            <div className="space-y-4 p-4">
              {/* Quick Curriculum Templates */}
              <div>
                <div className="mb-1.5 text-[11px] font-semibold uppercase tracking-[0.05em] text-[#64748B]">
                  Curriculum Templates
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {CURRICULUM_TEMPLATES.map((tpl) => (
                    <button
                      key={tpl.label}
                      type="button"
                      onClick={() =>
                        onChangeForm({
                          subject: tpl.subject,
                          gradeLevel: tpl.gradeLevel,
                          topic: tpl.topic,
                          bloomLevel: tpl.bloomLevel,
                          format: tpl.format,
                          difficulty: tpl.difficulty,
                        })
                      }
                      className="rounded-[4px] border border-[#E2E8F0] bg-[#F8FAFC] px-2.5 py-1 text-[11px] font-semibold text-[#0F172A] transition-colors hover:border-[#CBD5E1] hover:bg-[#EFF4FF]"
                    >
                      {tpl.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Subject & Grade */}
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="mb-1 block text-[11px] font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
                    Subject
                  </label>
                  <input
                    type="text"
                    value={form.subject}
                    onChange={(e) => onChangeForm({ subject: e.target.value })}
                    placeholder="Biology"
                    className="edusoal-input"
                  />
                </div>
                <div>
                  <label className="mb-1 block text-[11px] font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
                    Grade / Level
                  </label>
                  <input
                    type="text"
                    value={form.gradeLevel}
                    onChange={(e) => onChangeForm({ gradeLevel: e.target.value })}
                    placeholder="Grade 11"
                    className="edusoal-input"
                  />
                </div>
              </div>

              {/* Topic */}
              <div>
                <label className="mb-1 block text-[11px] font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
                  Topic / Learning Objective
                </label>
                <textarea
                  rows={2}
                  value={form.topic}
                  onChange={(e) => onChangeForm({ topic: e.target.value })}
                  placeholder="e.g., Enzyme kinetics and thermal denaturation..."
                  className="edusoal-input"
                />
              </div>

              {/* Bloom Taxonomy Selector */}
              <div>
                <label className="mb-1.5 block text-[11px] font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
                  Bloom&apos;s Cognitive Level
                </label>
                <div className="grid grid-cols-2 gap-1.5">
                  {BLOOM_LEVELS.map((b) => {
                    const selected = form.bloomLevel === b.code;
                    const activeStyles =
                      b.tierGroup === 'LOW'
                        ? 'border-[#0F172A] bg-[#F1F5F9] text-[#0F172A]'
                        : b.tierGroup === 'MID'
                        ? 'border-[#F59E0B] bg-[#FFFBEB] text-[#92400E]'
                        : 'border-[#EA580C] bg-[#FFF7ED] text-[#9A3412]';
                    return (
                      <button
                        key={b.code}
                        type="button"
                        onClick={() => onChangeForm({ bloomLevel: b.code })}
                        className={`rounded-[4px] border px-2 py-1.5 text-left text-[11px] font-semibold transition-all ${
                          selected
                            ? activeStyles
                            : 'border-[#E2E8F0] bg-white text-[#64748B] hover:bg-[#F8FAFC]'
                        }`}
                      >
                        {b.title}
                      </button>
                    );
                  })}
                </div>
                <p className="mt-1.5 text-xs text-[#64748B]">{selectedBloomMeta.tierDescription}</p>
              </div>

              {/* Format */}
              <div>
                <label className="mb-1.5 block text-[11px] font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
                  Item Format
                </label>
                <div className="grid grid-cols-3 gap-1.5">
                  {FORMAT_OPTIONS.map((fmt) => {
                    const selected = form.format === fmt.value;
                    return (
                      <button
                        key={fmt.value}
                        type="button"
                        onClick={() => onChangeForm({ format: fmt.value })}
                        className={`rounded-[4px] border px-2 py-1.5 text-center text-[11px] font-semibold transition-all ${
                          selected
                            ? 'border-[#0F172A] bg-[#0F172A] text-white'
                            : 'border-[#E2E8F0] bg-white text-[#0F172A] hover:bg-[#F8FAFC]'
                        }`}
                      >
                        {fmt.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Difficulty */}
              <div>
                <label className="mb-1.5 block text-[11px] font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
                  Difficulty Tier
                </label>
                <div className="grid grid-cols-3 gap-1.5">
                  {DIFFICULTY_OPTIONS.map((diff) => {
                    const selected = form.difficulty === diff.value;
                    return (
                      <button
                        key={diff.value}
                        type="button"
                        onClick={() => onChangeForm({ difficulty: diff.value })}
                        className={`rounded-[4px] border px-2 py-1.5 text-center text-[11px] font-semibold transition-all ${
                          selected
                            ? 'border-[#2563EB] bg-[#EFF6FF] text-[#2563EB]'
                            : 'border-[#E2E8F0] bg-white text-[#64748B] hover:bg-[#F8FAFC]'
                        }`}
                      >
                        {diff.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Optional Custom Stimulus */}
              <div>
                <button
                  type="button"
                  onClick={() => setShowCustomStimulus((s) => !s)}
                  className="flex w-full items-center justify-between text-[11px] font-semibold uppercase tracking-[0.04em] text-[#2563EB]"
                >
                  <span>Optional Source Stimulus</span>
                  {showCustomStimulus ? (
                    <ChevronUp className="h-4 w-4" />
                  ) : (
                    <ChevronDown className="h-4 w-4" />
                  )}
                </button>
                {showCustomStimulus && (
                  <textarea
                    rows={3}
                    value={form.customStimulus}
                    onChange={(e) => onChangeForm({ customStimulus: e.target.value })}
                    placeholder="Paste reading excerpt, dataset, or case notes (leave blank to auto-author)..."
                    className="edusoal-input mt-2"
                  />
                )}
              </div>

              {/* Generate CTA */}
              <button
                type="button"
                disabled={isGenerating}
                onClick={onGenerate}
                className="inline-flex min-h-[44px] w-full items-center justify-center gap-2 rounded-[4px] bg-[#2563EB] px-4 py-2.5 text-xs font-semibold text-white transition-all hover:bg-[#1D4ED8] active:scale-[0.995] disabled:opacity-60"
              >
                {isGenerating ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    <span>Synthesizing Item &amp; Stimulus...</span>
                  </>
                ) : (
                  <>
                    <Sparkles className="h-4 w-4" />
                    <span>Generate Item with AI</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </section>

        {/* Center Pane: Interactive Stimulus & Question Preview Canvas (7 cols lg, 6 cols xl) */}
        <section className="lg:col-span-7 xl:col-span-6">
          {error && (
            <div className="mb-4 rounded-[4px] border border-[#BA1A1A]/40 bg-[#FFDAD6] p-4 text-[#93000A]">
              <div className="flex items-start gap-2.5">
                <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
                <div className="flex-1 text-xs leading-relaxed">
                  <div className="font-semibold uppercase tracking-[0.04em]">Generation Notice</div>
                  <p className="mt-1">{error}</p>
                  <button
                    type="button"
                    onClick={onGenerate}
                    className="mt-2.5 inline-flex items-center gap-1.5 rounded-[4px] border border-[#93000A]/30 bg-white/80 px-3 py-1.5 text-xs font-semibold text-[#93000A] hover:bg-white"
                  >
                    <RefreshCw className="h-3.5 w-3.5" />
                    <span>Retry Generation</span>
                  </button>
                </div>
              </div>
            </div>
          )}

          {isGenerating ? (
            <div className="edusoal-card flex flex-col items-center justify-center px-6 py-16 text-center">
              <Loader2 className="h-8 w-8 animate-spin text-[#2563EB]" />
              <h3 className="mt-4 font-display text-base font-semibold text-[#0F172A]">
                Calibrating Stimulus &amp; Distractors
              </h3>
              <p className="mt-1 max-w-md text-xs text-[#64748B]">
                Aligning stem complexity, answer key rationale, discrimination index, and scoring rubric via Gemini...
              </p>
            </div>
          ) : generatedItem ? (
            <QuestionStimulusCard
              item={generatedItem}
              showAnswerKey={true}
              interactiveKeySelection={true}
              isSavedToBank={savedItemId !== null}
              onSelectOption={(idx) =>
                onUpdateGeneratedItem({ ...generatedItem, correctOptionIndex: idx })
              }
              onSaveToBank={onSaveToBank}
              onEditItem={() => setShowEditModal(true)}
              onOpenInspector={() => setShowInspectorDrawer(true)}
            />
          ) : (
            <div className="edusoal-card flex flex-col items-center justify-center px-6 py-16 text-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-[4px] border border-[#E2E8F0] bg-[#F8FAFC] text-[#2563EB]">
                <Sparkles className="h-5 w-5" />
              </div>
              <h3 className="mt-4 font-display text-base font-semibold text-[#0F172A]">
                Stimulus &amp; Item Canvas Ready
              </h3>
              <p className="mt-1.5 max-w-md text-sm text-[#64748B]">
                Select a curriculum template or enter a learning objective on the left, pick a Bloom&apos;s cognitive level, and generate a calibrated assessment item.
              </p>
            </div>
          )}
        </section>

        {/* Right Pane: Pedagogical Metadata Inspector (3 cols on xl desktop) */}
        <aside className="hidden xl:col-span-3 xl:block">
          {generatedItem ? (
            <PedagogicalInspector item={generatedItem} />
          ) : (
            <div className="edusoal-card p-4 text-xs leading-relaxed text-[#64748B]">
              <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-[#0F172A]">
                Pedagogical Inspector
              </div>
              <p className="mt-2">
                Curriculum alignment, completion time, discrimination index, pedagogical rationale, and scoring rubrics will appear here after item generation.
              </p>
            </div>
          )}
        </aside>
      </div>

      {/* Tablet/Mobile Slide-Over Inspector Drawer */}
      {showInspectorDrawer && generatedItem && (
        <div className="fixed inset-0 z-50 flex justify-end bg-[#0F172A]/30 backdrop-blur-[1px] xl:hidden">
          <div className="flex h-full w-full max-w-md flex-col border-l border-[#E2E8F0] bg-[#F8FAFC] shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#E2E8F0] bg-white px-4 py-3">
              <h3 className="font-display text-sm font-semibold text-[#0F172A]">
                Pedagogical Inspector
              </h3>
              <button
                type="button"
                onClick={() => setShowInspectorDrawer(false)}
                className="rounded-[4px] p-1 text-[#64748B] hover:bg-[#F1F5F9]"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="flex-1 overflow-y-auto p-4">
              <PedagogicalInspector item={generatedItem} />
            </div>
          </div>
        </div>
      )}

      {showEditModal && generatedItem && (
        <EditQuestionModal
          initialItem={generatedItem}
          onClose={() => setShowEditModal(false)}
          onSave={(updated) => {
            onUpdateGeneratedItem(updated);
            setShowEditModal(false);
          }}
        />
      )}
    </div>
  );
}
