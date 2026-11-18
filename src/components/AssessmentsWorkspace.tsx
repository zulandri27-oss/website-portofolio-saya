import { useMemo, useState } from 'react';
import {
  ArrowLeft,
  ClipboardCheck,
  Eye,
  EyeOff,
  ListPlus,
  Plus,
  Printer,
  Trash2,
  X,
} from 'lucide-react';
import {
  AssessmentPlan,
  AssessmentStatus,
  QuestionItem,
  getBloomMeta,
} from '../types/edusoal';
import { QuestionStimulusCard } from './QuestionStimulusCard';

interface AssessmentsWorkspaceProps {
  assessments: AssessmentPlan[];
  questions: QuestionItem[];
  selectedAssessmentId: string | null;
  onSelectAssessment: (id: string | null) => void;
  onCreateAssessment: (plan: AssessmentPlan) => void;
  onUpdateAssessment: (plan: AssessmentPlan) => void;
  onDeleteAssessment: (id: string) => void;
}

export function AssessmentsWorkspace({
  assessments,
  questions,
  selectedAssessmentId,
  onSelectAssessment,
  onCreateAssessment,
  onUpdateAssessment,
  onDeleteAssessment,
}: AssessmentsWorkspaceProps) {
  const [showAnswerKeys, setShowAnswerKeys] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showManageItemsModal, setShowManageItemsModal] = useState(false);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  const activePlan =
    assessments.find((a) => a.id === selectedAssessmentId) || assessments[0] || null;

  const activeItems = useMemo(() => {
    if (!activePlan) return [];
    return activePlan.questionIds
      .map((id) => questions.find((q) => q.id === id))
      .filter((q): q is QuestionItem => Boolean(q));
  }, [activePlan, questions]);

  const toggleItemInPlan = (plan: AssessmentPlan, questionId: string) => {
    const exists = plan.questionIds.includes(questionId);
    const nextIds = exists
      ? plan.questionIds.filter((id) => id !== questionId)
      : [...plan.questionIds, questionId];
    const nextMinutes = nextIds.reduce((acc, id) => {
      const q = questions.find((item) => item.id === id);
      return acc + (q?.estimatedMinutes || 3);
    }, 0);

    onUpdateAssessment({
      ...plan,
      questionIds: nextIds,
      totalMinutes: nextMinutes,
      updatedAt: new Date().toISOString(),
    });
  };

  return (
    <div className="mx-auto max-w-[1440px] px-4 py-5 sm:px-6 lg:px-8">
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
        {/* Left List Pane */}
        <section
          className={`no-print lg:col-span-4 ${
            selectedAssessmentId ? 'hidden lg:block' : 'block'
          }`}
        >
          <div className="mb-3 flex items-center justify-between gap-2">
            <div>
              <h2 className="font-display text-lg font-semibold text-[#0F172A]">Assessments</h2>
              <p className="text-xs text-[#64748B]">{assessments.length} exam matrices</p>
            </div>
            <button
              type="button"
              onClick={() => setShowCreateModal(true)}
              className="inline-flex min-h-[38px] items-center gap-1.5 rounded-[4px] bg-[#0F172A] px-3 py-1.5 text-xs font-semibold text-white hover:bg-[#1E293B]"
            >
              <Plus className="h-3.5 w-3.5" />
              <span>New Matrix</span>
            </button>
          </div>

          {assessments.length === 0 ? (
            <div className="edusoal-card flex flex-col items-center px-6 py-12 text-center">
              <ClipboardCheck className="h-8 w-8 text-[#64748B]" />
              <h3 className="mt-3 font-display text-sm font-semibold text-[#0F172A]">
                No Assessment Matrices Yet
              </h3>
              <p className="mt-1 text-xs text-[#64748B]">
                Assemble saved questions into balanced quizzes or print-ready exams with Bloom&apos;s cognitive distribution tracking.
              </p>
            </div>
          ) : (
            <div className="space-y-2.5">
              {assessments.map((plan) => {
                const linked = plan.questionIds
                  .map((id) => questions.find((q) => q.id === id))
                  .filter((q): q is QuestionItem => Boolean(q));
                const isSelected = activePlan?.id === plan.id;
                return (
                  <button
                    key={plan.id}
                    type="button"
                    onClick={() => onSelectAssessment(plan.id)}
                    className={`w-full rounded-[4px] border bg-white p-3.5 text-left transition-all ${
                      isSelected
                        ? 'border-[#2563EB] ring-2 ring-[#2563EB]/10'
                        : 'border-[#E2E8F0] hover:border-[#CBD5E1]'
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-[11px] font-semibold uppercase tracking-[0.04em] text-[#2563EB]">
                        {plan.subject} · {plan.gradeLevel}
                      </span>
                      <StatusPill status={plan.status} />
                    </div>
                    <div className="mt-1.5 font-display text-sm font-semibold text-[#0F172A]">
                      {plan.title}
                    </div>
                    <div className="mt-2.5">
                      <BloomDistributionBar items={linked} />
                    </div>
                    <div className="mt-2 text-xs text-[#64748B]">
                      {linked.length} items · {plan.totalMinutes} min total
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </section>

        {/* Right Detail & Exam Preview */}
        <section
          className={`lg:col-span-8 ${
            selectedAssessmentId ? 'block' : 'hidden lg:block'
          }`}
        >
          {activePlan ? (
            <div className="space-y-4">
              <div className="edusoal-card print-container p-4 sm:p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="flex items-start gap-2">
                    <button
                      type="button"
                      onClick={() => onSelectAssessment(null)}
                      className="no-print mt-0.5 rounded-[4px] border border-[#E2E8F0] p-1.5 text-[#0F172A] hover:bg-[#F1F5F9] lg:hidden"
                    >
                      <ArrowLeft className="h-4 w-4" />
                    </button>
                    <div>
                      <h2 className="font-display text-lg font-semibold text-[#0F172A]">
                        {activePlan.title}
                      </h2>
                      <p className="text-xs text-[#64748B]">
                        {activePlan.subject} · {activePlan.gradeLevel} · {activeItems.length} items ·{' '}
                        {activePlan.totalMinutes} min
                      </p>
                    </div>
                  </div>

                  <div className="no-print flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => window.print()}
                      className="inline-flex items-center gap-1.5 rounded-[4px] border border-[#E2E8F0] bg-white px-3 py-1.5 text-xs font-semibold text-[#0F172A] hover:bg-[#F1F5F9]"
                    >
                      <Printer className="h-3.5 w-3.5" />
                      <span>Print / PDF</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => setConfirmDeleteId(activePlan.id)}
                      aria-label="Delete assessment"
                      className="rounded-[4px] border border-[#E2E8F0] p-1.5 text-[#BA1A1A] hover:bg-[#FFDAD6]/40"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>

                {activePlan.description && (
                  <p className="mt-2.5 text-sm text-[#45464D]">{activePlan.description}</p>
                )}

                <div className="no-print mt-4">
                  <div className="mb-1.5 text-[11px] font-semibold uppercase tracking-[0.05em] text-[#64748B]">
                    Bloom&apos;s Cognitive Balance
                  </div>
                  <BloomDistributionBar items={activeItems} />
                </div>

                <div className="no-print mt-4 grid grid-cols-3 gap-2">
                  {(['DRAFT', 'READY', 'PUBLISHED'] as AssessmentStatus[]).map((st) => {
                    const active = activePlan.status === st;
                    return (
                      <button
                        key={st}
                        type="button"
                        onClick={() =>
                          onUpdateAssessment({
                            ...activePlan,
                            status: st,
                            updatedAt: new Date().toISOString(),
                          })
                        }
                        className={`rounded-[4px] border py-1.5 text-[11px] font-semibold uppercase tracking-[0.05em] ${
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

                <div className="no-print mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-[#E2E8F0] pt-3.5">
                  <button
                    type="button"
                    onClick={() => setShowAnswerKeys((v) => !v)}
                    className="inline-flex items-center gap-2 rounded-[4px] border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-1.5 text-xs font-semibold text-[#0F172A] hover:bg-[#F1F5F9]"
                  >
                    {showAnswerKeys ? (
                      <>
                        <Eye className="h-3.5 w-3.5 text-[#059669]" />
                        <span>Answer Key Mode</span>
                      </>
                    ) : (
                      <>
                        <EyeOff className="h-3.5 w-3.5 text-[#64748B]" />
                        <span>Student Exam Mode</span>
                      </>
                    )}
                  </button>

                  <button
                    type="button"
                    onClick={() => setShowManageItemsModal(true)}
                    className="inline-flex items-center gap-1.5 rounded-[4px] border border-[#E2E8F0] bg-white px-3 py-1.5 text-xs font-semibold text-[#2563EB] hover:bg-[#EFF6FF]"
                  >
                    <ListPlus className="h-3.5 w-3.5" />
                    <span>Manage Items</span>
                  </button>
                </div>
              </div>

              {activeItems.length === 0 ? (
                <div className="edusoal-card px-6 py-12 text-center">
                  <h3 className="font-display text-sm font-semibold text-[#0F172A]">
                    No Questions Linked Yet
                  </h3>
                  <p className="mt-1 text-xs text-[#64748B]">
                    Click Manage Items above to attach calibrated items from your Item Bank.
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {activeItems.map((item, idx) => (
                    <div key={item.id} className="space-y-1.5">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold uppercase tracking-[0.04em] text-[#0F172A]">
                          Question {idx + 1}
                        </span>
                        <button
                          type="button"
                          onClick={() => toggleItemInPlan(activePlan, item.id)}
                          className="no-print text-xs font-semibold text-[#BA1A1A] hover:underline"
                        >
                          Remove
                        </button>
                      </div>
                      <QuestionStimulusCard
                        item={item}
                        showAnswerKey={showAnswerKeys}
                        interactiveKeySelection={false}
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div className="edusoal-card flex items-center justify-center px-6 py-16 text-center text-sm text-[#64748B]">
              Select or create an assessment matrix to view its Bloom&apos;s cognitive balance and exam preview.
            </div>
          )}
        </section>
      </div>

      {showCreateModal && (
        <CreateMatrixModal
          questions={questions}
          onClose={() => setShowCreateModal(false)}
          onCreate={(plan) => {
            onCreateAssessment(plan);
            onSelectAssessment(plan.id);
            setShowCreateModal(false);
          }}
        />
      )}

      {showManageItemsModal && activePlan && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0F172A]/30 p-4 backdrop-blur-[1px]">
          <div className="flex max-h-[80vh] w-full max-w-lg flex-col overflow-hidden rounded-[8px] border border-[#E2E8F0] bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#E2E8F0] bg-[#F8FAFC] px-4 py-3">
              <h3 className="font-display text-sm font-semibold text-[#0F172A]">
                Manage Matrix Items
              </h3>
              <button
                type="button"
                onClick={() => setShowManageItemsModal(false)}
                className="text-[#64748B] hover:text-[#0F172A]"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="flex-1 space-y-2 overflow-y-auto p-4">
              {questions.length === 0 ? (
                <p className="text-xs text-[#64748B]">
                  Your Item Bank is empty. Generate or author questions first.
                </p>
              ) : (
                questions.map((q) => {
                  const included = activePlan.questionIds.includes(q.id);
                  return (
                    <button
                      key={q.id}
                      type="button"
                      onClick={() => toggleItemInPlan(activePlan, q.id)}
                      className={`flex w-full items-start gap-3 rounded-[4px] border p-3 text-left ${
                        included
                          ? 'border-[#059669] bg-[#ECFDF5]'
                          : 'border-[#E2E8F0] bg-[#F8FAFC] hover:bg-white'
                      }`}
                    >
                      <input
                        type="checkbox"
                        checked={included}
                        readOnly
                        className="mt-1 h-4 w-4 accent-[#059669]"
                      />
                      <div className="flex-1">
                        <div className="line-clamp-2 text-xs font-semibold text-[#0F172A]">
                          {q.questionStem}
                        </div>
                        <div className="mt-1 text-[11px] text-[#64748B]">
                          {q.bloomLevel} · {q.subject} · {q.estimatedMinutes} min
                        </div>
                      </div>
                    </button>
                  );
                })
              )}
            </div>
            <div className="flex justify-end border-t border-[#E2E8F0] bg-[#F8FAFC] px-4 py-3">
              <button
                type="button"
                onClick={() => setShowManageItemsModal(false)}
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
              Delete Assessment Matrix?
            </h3>
            <p className="mt-1.5 text-xs text-[#64748B]">
              Underlying questions in your Item Bank remain preserved.
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
                  onDeleteAssessment(confirmDeleteId);
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

function BloomDistributionBar({ items }: { items: QuestionItem[] }) {
  const total = Math.max(1, items.length);
  const low = items.filter((i) => getBloomMeta(i.bloomLevel).tierGroup === 'LOW').length;
  const mid = items.filter((i) => getBloomMeta(i.bloomLevel).tierGroup === 'MID').length;
  const high = items.filter((i) => getBloomMeta(i.bloomLevel).tierGroup === 'HIGH').length;

  return (
    <div>
      <div className="flex h-2 w-full overflow-hidden rounded-[4px] bg-[#E2E8F0]">
        {items.length > 0 && (
          <>
            {low > 0 && (
              <div style={{ width: `${(low / total) * 100}%` }} className="bg-[#475569]" />
            )}
            {mid > 0 && (
              <div style={{ width: `${(mid / total) * 100}%` }} className="bg-[#F59E0B]" />
            )}
            {high > 0 && (
              <div style={{ width: `${(high / total) * 100}%` }} className="bg-[#EA580C]" />
            )}
          </>
        )}
      </div>
      <div className="mt-1.5 flex flex-wrap gap-3 text-[11px] font-semibold">
        <span className="text-[#334155]">C1–C2: {low}</span>
        <span className="text-[#92400E]">C3–C4: {mid}</span>
        <span className="text-[#9A3412]">C5–C6: {high}</span>
      </div>
    </div>
  );
}

function StatusPill({ status }: { status: AssessmentStatus }) {
  const cls =
    status === 'PUBLISHED'
      ? 'bg-[#ECFDF5] text-[#065F46] border-[#A7F3D0]'
      : status === 'READY'
      ? 'bg-[#EFF6FF] text-[#2563EB] border-[#BFDBFE]'
      : 'bg-[#F8FAFC] text-[#64748B] border-[#E2E8F0]';
  return (
    <span
      className={`inline-flex rounded-[4px] border px-1.5 py-0.5 text-[11px] font-semibold uppercase tracking-[0.05em] ${cls}`}
    >
      {status}
    </span>
  );
}

function CreateMatrixModal({
  questions,
  onClose,
  onCreate,
}: {
  questions: QuestionItem[];
  onClose: () => void;
  onCreate: (plan: AssessmentPlan) => void;
}) {
  const [title, setTitle] = useState('');
  const [subject, setSubject] = useState('Biology');
  const [grade, setGrade] = useState('Grade 11');
  const [description, setDescription] = useState('');
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const handleSubmit = () => {
    if (!title.trim()) return;
    const now = new Date().toISOString();
    const totalMinutes = selectedIds.reduce((sum, id) => {
      const q = questions.find((item) => item.id === id);
      return sum + (q?.estimatedMinutes || 3);
    }, 0);
    onCreate({
      id: crypto.randomUUID(),
      title: title.trim(),
      subject: subject.trim() || 'General',
      gradeLevel: grade.trim() || 'Grade 11',
      description: description.trim(),
      questionIds: selectedIds,
      totalMinutes,
      status: 'DRAFT',
      createdAt: now,
      updatedAt: now,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0F172A]/30 p-4 backdrop-blur-[1px]">
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-[8px] border border-[#E2E8F0] bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#E2E8F0] bg-[#F8FAFC] px-5 py-3.5">
          <h3 className="font-display text-base font-semibold text-[#0F172A]">
            New Assessment Matrix
          </h3>
          <button type="button" onClick={onClose} className="text-[#64748B] hover:text-[#0F172A]">
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="flex-1 space-y-3.5 overflow-y-auto p-5">
          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
              Assessment Title
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Midterm Bioenergetics Matrix"
              className="edusoal-input"
            />
          </div>

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
                Grade / Level
              </label>
              <input
                type="text"
                value={grade}
                onChange={(e) => setGrade(e.target.value)}
                className="edusoal-input"
              />
            </div>
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-[0.04em]">
              Instructions / Scope
            </label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="edusoal-input"
            />
          </div>

          {questions.length > 0 && (
            <div className="space-y-2">
              <label className="block text-xs font-semibold uppercase tracking-[0.04em]">
                Include Items ({selectedIds.length} Selected)
              </label>
              <div className="max-h-48 space-y-1.5 overflow-y-auto">
                {questions.map((q) => {
                  const checked = selectedIds.includes(q.id);
                  return (
                    <button
                      key={q.id}
                      type="button"
                      onClick={() =>
                        setSelectedIds((prev) =>
                          checked ? prev.filter((id) => id !== q.id) : [...prev, q.id]
                        )
                      }
                      className={`flex w-full items-center gap-2.5 rounded-[4px] border p-2.5 text-left ${
                        checked
                          ? 'border-[#2563EB] bg-[#EFF6FF]/60'
                          : 'border-[#E2E8F0] bg-white hover:bg-[#F8FAFC]'
                      }`}
                    >
                      <input
                        type="checkbox"
                        checked={checked}
                        readOnly
                        className="h-4 w-4 accent-[#2563EB]"
                      />
                      <div className="flex-1 truncate text-xs font-medium text-[#0F172A]">
                        {q.questionStem}
                      </div>
                      <span className="text-[11px] font-semibold text-[#64748B]">
                        {q.bloomLevel}
                      </span>
                    </button>
                  );
                })}
              </div>
            </div>
          )}
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
            disabled={!title.trim()}
            onClick={handleSubmit}
            className="rounded-[4px] bg-[#0F172A] px-4 py-2 text-xs font-semibold text-white disabled:opacity-50"
          >
            Create Matrix
          </button>
        </div>
      </div>
    </div>
  );
}
