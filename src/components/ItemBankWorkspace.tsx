import { useMemo, useState } from 'react';
import {
  ArrowLeft,
  FolderOpen,
  ListPlus,
  Plus,
  Search,
  Trash2,
  X,
} from 'lucide-react';
import {
  AssessmentPlan,
  BLOOM_LEVELS,
  BloomCode,
  FORMAT_OPTIONS,
  QuestionItem,
  ReviewStatus,
  getBloomMeta,
} from '../types/edusoal';
import { BloomBadge, ReviewStatusBadge } from './BloomBadge';
import { QuestionStimulusCard } from './QuestionStimulusCard';
import { PedagogicalInspector } from './PedagogicalInspector';
import { EditQuestionModal } from './EditQuestionModal';

interface ItemBankWorkspaceProps {
  questions: QuestionItem[];
  assessments: AssessmentPlan[];
  selectedQuestionId: string | null;
  onSelectQuestion: (id: string | null) => void;
  onCreateManual: (item: QuestionItem) => void;
  onUpdateQuestion: (item: QuestionItem) => void;
  onDeleteQuestion: (id: string) => void;
  onToggleInAssessment: (assessmentId: string, questionId: string) => void;
}

export function ItemBankWorkspace({
  questions,
  assessments,
  selectedQuestionId,
  onSelectQuestion,
  onCreateManual,
  onUpdateQuestion,
  onDeleteQuestion,
  onToggleInAssessment,
}: ItemBankWorkspaceProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [bloomFilter, setBloomFilter] = useState<'ALL' | 'LOW' | 'MID' | 'HIGH'>('ALL');
  const [statusFilter, setStatusFilter] = useState<ReviewStatus | 'ALL'>('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingItem, setEditingItem] = useState<QuestionItem | null>(null);
  const [assignQuestionId, setAssignQuestionId] = useState<string | null>(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  const filteredQuestions = useMemo(() => {
    return questions.filter((item) => {
      const q = searchQuery.trim().toLowerCase();
      const matchesSearch =
        !q ||
        item.questionStem.toLowerCase().includes(q) ||
        item.topic.toLowerCase().includes(q) ||
        item.subject.toLowerCase().includes(q);
      const tier = getBloomMeta(item.bloomLevel).tierGroup;
      const matchesBloom = bloomFilter === 'ALL' || tier === bloomFilter;
      const matchesStatus = statusFilter === 'ALL' || item.reviewStatus === statusFilter;
      return matchesSearch && matchesBloom && matchesStatus;
    });
  }, [questions, searchQuery, bloomFilter, statusFilter]);

  const activeQuestion =
    questions.find((q) => q.id === selectedQuestionId) ||
    filteredQuestions[0] ||
    null;

  return (
    <div className="mx-auto max-w-[1440px] px-4 py-5 sm:px-6 lg:px-8">
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
        {/* Left List Pane (4 cols) */}
        <section
          className={`lg:col-span-4 ${
            selectedQuestionId ? 'hidden lg:block' : 'block'
          }`}
        >
          <div className="mb-3 flex items-center justify-between gap-2">
            <div>
              <h2 className="font-display text-lg font-semibold text-[#0F172A]">Item Bank</h2>
              <p className="text-xs text-[#64748B]">{questions.length} calibrated items saved</p>
            </div>
            <button
              type="button"
              onClick={() => setShowCreateModal(true)}
              className="inline-flex min-h-[38px] items-center gap-1.5 rounded-[4px] border border-[#E2E8F0] bg-white px-3 py-1.5 text-xs font-semibold text-[#0F172A] hover:bg-[#F1F5F9]"
            >
              <Plus className="h-3.5 w-3.5" />
              <span>New Item</span>
            </button>
          </div>

          <div className="relative mb-3">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#64748B]" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search topic, subject, or stem..."
              className="edusoal-input pl-9"
            />
          </div>

          <div className="mb-3 flex flex-wrap gap-1.5">
            {(
              [
                ['ALL', 'All Tiers'],
                ['LOW', 'C1–C2 Recall'],
                ['MID', 'C3–C4 Apply'],
                ['HIGH', 'C5–C6 HOTS'],
              ] as const
            ).map(([key, label]) => (
              <button
                key={key}
                type="button"
                onClick={() => setBloomFilter(key)}
                className={`rounded-[4px] border px-2 py-1 text-[11px] font-semibold ${
                  bloomFilter === key
                    ? 'border-[#0F172A] bg-[#0F172A] text-white'
                    : 'border-[#E2E8F0] bg-white text-[#0F172A] hover:bg-[#F8FAFC]'
                }`}
              >
                {label}
              </button>
            ))}
            {(['DRAFT', 'VALIDATED', 'APPROVED'] as ReviewStatus[]).map((st) => (
              <button
                key={st}
                type="button"
                onClick={() => setStatusFilter(statusFilter === st ? 'ALL' : st)}
                className={`rounded-[4px] border px-2 py-1 text-[11px] font-semibold ${
                  statusFilter === st
                    ? 'border-[#2563EB] bg-[#EFF6FF] text-[#2563EB]'
                    : 'border-[#E2E8F0] bg-white text-[#64748B] hover:bg-[#F8FAFC]'
                }`}
              >
                {st}
              </button>
            ))}
          </div>

          {filteredQuestions.length === 0 ? (
            <div className="edusoal-card flex flex-col items-center px-6 py-12 text-center">
              <FolderOpen className="h-8 w-8 text-[#64748B]" />
              <h3 className="mt-3 font-display text-sm font-semibold text-[#0F172A]">
                {questions.length === 0 ? 'Your Item Bank is Empty' : 'No Matching Items'}
              </h3>
              <p className="mt-1 text-xs text-[#64748B]">
                {questions.length === 0
                  ? 'Generate items in the Generator tab or author one manually using New Item.'
                  : 'Clear your search or filter selections to view saved items.'}
              </p>
            </div>
          ) : (
            <div className="space-y-2.5">
              {filteredQuestions.map((item) => {
                const selected = activeQuestion?.id === item.id;
                const formatLabel =
                  FORMAT_OPTIONS.find((f) => f.value === item.questionFormat)?.label || 'MC';
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => onSelectQuestion(item.id)}
                    className={`w-full rounded-[4px] border bg-white p-3.5 text-left transition-all ${
                      selected
                        ? 'border-[#2563EB] ring-2 ring-[#2563EB]/10'
                        : 'border-[#E2E8F0] hover:border-[#CBD5E1]'
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-[11px] font-semibold uppercase tracking-[0.04em] text-[#2563EB]">
                        {item.subject} · {item.gradeLevel}
                      </span>
                      <ReviewStatusBadge status={item.reviewStatus} />
                    </div>
                    <p className="mt-1.5 line-clamp-2 font-display text-sm font-semibold text-[#0F172A]">
                      {item.questionStem}
                    </p>
                    <div className="mt-2.5 flex items-center justify-between gap-2">
                      <BloomBadge code={item.bloomLevel} />
                      <span className="text-xs text-[#64748B]">
                        {item.estimatedMinutes} min · {formatLabel}
                      </span>
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </section>

        {/* Right Detail & Metadata Inspector (8 cols) */}
        <section
          className={`lg:col-span-8 ${
            selectedQuestionId ? 'block' : 'hidden lg:block'
          }`}
        >
          {activeQuestion ? (
            <div className="space-y-4">
              {/* Detail Header & Status Workflow */}
              <div className="edusoal-card p-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="flex items-start gap-2">
                    <button
                      type="button"
                      onClick={() => onSelectQuestion(null)}
                      className="mt-0.5 rounded-[4px] border border-[#E2E8F0] p-1.5 text-[#0F172A] hover:bg-[#F1F5F9] lg:hidden"
                      aria-label="Back to list"
                    >
                      <ArrowLeft className="h-4 w-4" />
                    </button>
                    <div>
                      <h3 className="font-display text-base font-semibold text-[#0F172A]">
                        {activeQuestion.topic}
                      </h3>
                      <p className="text-xs text-[#64748B]">
                        {activeQuestion.subject} · {activeQuestion.gradeLevel}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => setAssignQuestionId(activeQuestion.id)}
                      className="inline-flex items-center gap-1.5 rounded-[4px] border border-[#E2E8F0] bg-white px-3 py-1.5 text-xs font-semibold text-[#2563EB] hover:bg-[#EFF6FF]"
                    >
                      <ListPlus className="h-3.5 w-3.5" />
                      <span>Add to Matrix</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => setConfirmDeleteId(activeQuestion.id)}
                      aria-label="Delete item"
                      className="rounded-[4px] border border-[#E2E8F0] p-1.5 text-[#BA1A1A] hover:bg-[#FFDAD6]/40"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>

                {/* Review Workflow */}
                <div className="mt-3 grid grid-cols-3 gap-2">
                  {(['DRAFT', 'VALIDATED', 'APPROVED'] as ReviewStatus[]).map((st) => {
                    const active = activeQuestion.reviewStatus === st;
                    return (
                      <button
                        key={st}
                        type="button"
                        onClick={() =>
                          onUpdateQuestion({
                            ...activeQuestion,
                            reviewStatus: st,
                            updatedAt: new Date().toISOString(),
                          })
                        }
                        className={`rounded-[4px] border py-1.5 text-[11px] font-semibold uppercase tracking-[0.05em] transition-colors ${
                          active
                            ? 'border-[#059669] bg-[#ECFDF5] text-[#065F46]'
                            : 'border-[#E2E8F0] bg-[#F8FAFC] text-[#64748B] hover:bg-white'
                        }`}
                      >
                        {st}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 xl:grid-cols-12">
                <div className="xl:col-span-7">
                  <QuestionStimulusCard
                    item={activeQuestion}
                    showAnswerKey={true}
                    interactiveKeySelection={true}
                    onSelectOption={(idx) =>
                      onUpdateQuestion({
                        ...activeQuestion,
                        correctOptionIndex: idx,
                        updatedAt: new Date().toISOString(),
                      })
                    }
                    onEditItem={() => setEditingItem(activeQuestion)}
                  />
                </div>
                <div className="xl:col-span-5">
                  <PedagogicalInspector item={activeQuestion} />
                </div>
              </div>
            </div>
          ) : (
            <div className="edusoal-card flex items-center justify-center px-6 py-16 text-center text-sm text-[#64748B]">
              Select an item from the Item Bank to inspect its stimulus, answer key, and pedagogical metadata.
            </div>
          )}
        </section>
      </div>

      {editingItem && (
        <EditQuestionModal
          initialItem={editingItem}
          onClose={() => setEditingItem(null)}
          onSave={(updated) => {
            onUpdateQuestion(updated);
            setEditingItem(null);
          }}
        />
      )}

      {showCreateModal && (
        <CreateManualItemModal
          onClose={() => setShowCreateModal(false)}
          onCreate={(item) => {
            onCreateManual(item);
            onSelectQuestion(item.id);
            setShowCreateModal(false);
          }}
        />
      )}

      {assignQuestionId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0F172A]/30 p-4 backdrop-blur-[1px]">
          <div className="w-full max-w-md overflow-hidden rounded-[8px] border border-[#E2E8F0] bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#E2E8F0] bg-[#F8FAFC] px-4 py-3">
              <h3 className="font-display text-sm font-semibold text-[#0F172A]">
                Include in Assessment Matrix
              </h3>
              <button
                type="button"
                onClick={() => setAssignQuestionId(null)}
                className="rounded-[4px] p-1 text-[#64748B] hover:bg-[#E2E8F0]/60"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="max-h-[60vh] space-y-2 overflow-y-auto p-4">
              {assessments.length === 0 ? (
                <p className="text-xs text-[#64748B]">
                  No assessment matrices created yet. Open the Assessments tab to create your first matrix.
                </p>
              ) : (
                assessments.map((plan) => {
                  const included = plan.questionIds.includes(assignQuestionId);
                  return (
                    <button
                      key={plan.id}
                      type="button"
                      onClick={() => onToggleInAssessment(plan.id, assignQuestionId)}
                      className={`flex w-full items-center justify-between rounded-[4px] border p-3 text-left ${
                        included
                          ? 'border-[#059669] bg-[#ECFDF5]'
                          : 'border-[#E2E8F0] bg-[#F8FAFC] hover:bg-white'
                      }`}
                    >
                      <div>
                        <div className="text-xs font-semibold text-[#0F172A]">{plan.title}</div>
                        <div className="text-[11px] text-[#64748B]">
                          {plan.questionIds.length} items · {plan.subject}
                        </div>
                      </div>
                      <span
                        className={`text-[11px] font-semibold uppercase ${
                          included ? 'text-[#059669]' : 'text-[#2563EB]'
                        }`}
                      >
                        {included ? 'Included' : 'Add'}
                      </span>
                    </button>
                  );
                })
              )}
            </div>
            <div className="flex justify-end border-t border-[#E2E8F0] bg-[#F8FAFC] px-4 py-3">
              <button
                type="button"
                onClick={() => setAssignQuestionId(null)}
                className="rounded-[4px] bg-[#0F172A] px-4 py-2 text-xs font-semibold text-white"
              >
                Done
              </button>
            </div>
          </div>
        </div>
      )}

      {confirmDeleteId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0F172A]/30 p-4 backdrop-blur-[1px]">
          <div className="w-full max-w-sm rounded-[8px] border border-[#E2E8F0] bg-white p-5 shadow-2xl">
            <h3 className="font-display text-base font-semibold text-[#0F172A]">
              Delete Question Item?
            </h3>
            <p className="mt-1.5 text-xs text-[#64748B]">
              This permanently removes the item from your Item Bank.
            </p>
            <div className="mt-4 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setConfirmDeleteId(null)}
                className="rounded-[4px] border border-[#E2E8F0] px-3.5 py-2 text-xs font-semibold text-[#64748B]"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => {
                  onDeleteQuestion(confirmDeleteId);
                  setConfirmDeleteId(null);
                }}
                className="rounded-[4px] bg-[#BA1A1A] px-3.5 py-2 text-xs font-semibold text-white"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function CreateManualItemModal({
  onClose,
  onCreate,
}: {
  onClose: () => void;
  onCreate: (item: QuestionItem) => void;
}) {
  const [subject, setSubject] = useState('Mathematics');
  const [grade, setGrade] = useState('Grade 11');
  const [topic, setTopic] = useState('');
  const [bloom, setBloom] = useState<BloomCode>('C3');
  const [stimulusTitle, setStimulusTitle] = useState('');
  const [stimulusContent, setStimulusContent] = useState('');
  const [stem, setStem] = useState('');
  const [options, setOptions] = useState(['', '', '', '']);
  const [correctIdx, setCorrectIdx] = useState(0);
  const [rationale, setRationale] = useState('');

  const handleSubmit = () => {
    if (!stem.trim()) return;
    const now = new Date().toISOString();
    const cleaned = options.map((o) => o.trim()).filter(Boolean);
    onCreate({
      id: crypto.randomUUID(),
      subject: subject.trim() || 'General',
      gradeLevel: grade.trim() || 'Grade 11',
      topic: topic.trim() || 'Manual Assessment Item',
      bloomLevel: bloom,
      questionFormat: 'MULTIPLE_CHOICE',
      difficulty: 'STANDARD',
      stimulusTitle: stimulusTitle.trim(),
      stimulusContent: stimulusContent.trim(),
      questionStem: stem.trim(),
      options: cleaned,
      correctOptionIndex: cleaned.length === 0 ? -1 : Math.min(correctIdx, cleaned.length - 1),
      pedagogicalRationale: rationale.trim(),
      scoringRubric: '1 pt for correct selection.',
      curriculumAlignment: `${subject} · ${grade}`,
      estimatedMinutes: 3,
      discriminationIndex: 0.4,
      reviewStatus: 'DRAFT',
      createdAt: now,
      updatedAt: now,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0F172A]/30 p-4 backdrop-blur-[1px]">
      <div className="flex max-h-[90vh] w-full max-w-xl flex-col overflow-hidden rounded-[8px] border border-[#E2E8F0] bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#E2E8F0] bg-[#F8FAFC] px-5 py-3.5">
          <h3 className="font-display text-base font-semibold text-[#0F172A]">
            Author Manual Question Item
          </h3>
          <button type="button" onClick={onClose} className="text-[#64748B] hover:text-[#0F172A]">
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="flex-1 space-y-3.5 overflow-y-auto p-5">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
                Subject
              </label>
              <input
                type="text"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                className="edusoal-input"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
                Grade
              </label>
              <input
                type="text"
                value={grade}
                onChange={(e) => setGrade(e.target.value)}
                className="edusoal-input"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
                Topic
              </label>
              <input
                type="text"
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
                placeholder="Derivatives optimization"
                className="edusoal-input"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
                Bloom Level
              </label>
              <select
                value={bloom}
                onChange={(e) => setBloom(e.target.value as BloomCode)}
                className="edusoal-input"
              >
                {BLOOM_LEVELS.map((b) => (
                  <option key={b.code} value={b.code}>
                    {b.title}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
              Stimulus Title (Optional)
            </label>
            <input
              type="text"
              value={stimulusTitle}
              onChange={(e) => setStimulusTitle(e.target.value)}
              className="edusoal-input"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
              Context Stimulus (Optional)
            </label>
            <textarea
              rows={2}
              value={stimulusContent}
              onChange={(e) => setStimulusContent(e.target.value)}
              className="edusoal-input"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
              Question Stem
            </label>
            <textarea
              rows={2}
              value={stem}
              onChange={(e) => setStem(e.target.value)}
              className="edusoal-input"
            />
          </div>

          <div className="space-y-2">
            <label className="block text-xs font-semibold uppercase tracking-[0.04em]">
              Choices (Click letter for Answer Key)
            </label>
            {options.map((val, idx) => {
              const letter = String.fromCharCode(65 + idx);
              const isKey = idx === correctIdx;
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
                    value={val}
                    onChange={(e) => {
                      const next = [...options];
                      next[idx] = e.target.value;
                      setOptions(next);
                    }}
                    placeholder={`Option ${letter}`}
                    className="edusoal-input"
                  />
                </div>
              );
            })}
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
              Pedagogical Rationale
            </label>
            <textarea
              rows={2}
              value={rationale}
              onChange={(e) => setRationale(e.target.value)}
              className="edusoal-input"
            />
          </div>
        </div>

        <div className="flex justify-end gap-2 border-t border-[#E2E8F0] bg-[#F8FAFC] px-5 py-3">
          <button
            type="button"
            onClick={onClose}
            className="rounded-[4px] border border-[#E2E8F0] bg-white px-4 py-2 text-xs font-semibold text-[#64748B]"
          >
            Cancel
          </button>
          <button
            type="button"
            disabled={!stem.trim()}
            onClick={handleSubmit}
            className="rounded-[4px] bg-[#0F172A] px-4 py-2 text-xs font-semibold text-white disabled:opacity-50"
          >
            Create Item
          </button>
        </div>
      </div>
    </div>
  );
}
