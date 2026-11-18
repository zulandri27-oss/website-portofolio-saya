import { useEffect, useState } from 'react';
import {
  CheckCircle2,
  ClipboardCheck,
  Library,
  Settings,
  Sparkles,
} from 'lucide-react';
import {
  AssessmentPlan,
  GeneratorFormState,
  QuestionItem,
} from './types/edusoal';
import { generateAssessmentItem } from './services/geminiService';
import {
  downloadJsonBackup,
  loadAssessments,
  loadQuestions,
  saveAssessments,
  saveQuestions,
} from './services/storageService';
import { GeneratorWorkspace } from './components/GeneratorWorkspace';
import { ItemBankWorkspace } from './components/ItemBankWorkspace';
import { AssessmentsWorkspace } from './components/AssessmentsWorkspace';
import { SettingsModal } from './components/SettingsModal';

type TabKey = 'generator' | 'bank' | 'assessments';

export function App() {
  const [activeTab, setActiveTab] = useState<TabKey>('generator');
  const [questions, setQuestions] = useState<QuestionItem[]>(() => loadQuestions());
  const [assessments, setAssessments] = useState<AssessmentPlan[]>(() => loadAssessments());

  const [form, setForm] = useState<GeneratorFormState>({
    subject: 'Biology',
    gradeLevel: 'Grade 11',
    topic: '',
    bloomLevel: 'C4',
    format: 'MULTIPLE_CHOICE',
    difficulty: 'HOTS',
    customStimulus: '',
  });

  const [generatedItem, setGeneratedItem] = useState<QuestionItem | null>(null);
  const [savedItemId, setSavedItemId] = useState<string | null>(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [genError, setGenError] = useState<string | null>(null);

  const [selectedBankId, setSelectedBankId] = useState<string | null>(null);
  const [selectedAssessmentId, setSelectedAssessmentId] = useState<string | null>(null);
  const [showSettings, setShowSettings] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    saveQuestions(questions);
  }, [questions]);

  useEffect(() => {
    saveAssessments(assessments);
  }, [assessments]);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 3200);
    return () => clearTimeout(t);
  }, [toast]);

  const handleGenerate = async () => {
    setGenError(null);
    setIsGenerating(true);
    try {
      const item = await generateAssessmentItem(form);
      setGeneratedItem(item);
      setSavedItemId(null);
    } catch (err) {
      setGenError(err instanceof Error ? err.message : 'Unable to generate assessment item.');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSaveGeneratedToBank = () => {
    if (!generatedItem || savedItemId) return;
    setQuestions((prev) => [generatedItem, ...prev]);
    setSavedItemId(generatedItem.id);
    setToast('Item saved to Item Bank');
  };

  const handleUpdateGenerated = (updated: QuestionItem) => {
    setGeneratedItem(updated);
    if (savedItemId) {
      setQuestions((prev) => prev.map((q) => (q.id === savedItemId ? updated : q)));
    }
  };

  const handleToggleQuestionInAssessment = (assessmentId: string, questionId: string) => {
    setAssessments((prev) =>
      prev.map((plan) => {
        if (plan.id !== assessmentId) return plan;
        const exists = plan.questionIds.includes(questionId);
        const nextIds = exists
          ? plan.questionIds.filter((id) => id !== questionId)
          : [...plan.questionIds, questionId];
        const totalMinutes = nextIds.reduce((sum, id) => {
          const item = questions.find((q) => q.id === id);
          return sum + (item?.estimatedMinutes || 3);
        }, 0);
        setToast(exists ? `Removed from ${plan.title}` : `Added to ${plan.title}`);
        return {
          ...plan,
          questionIds: nextIds,
          totalMinutes,
          updatedAt: new Date().toISOString(),
        };
      })
    );
  };

  const tabs: { key: TabKey; label: string; icon: typeof Sparkles; badge?: number }[] = [
    { key: 'generator', label: 'Generator', icon: Sparkles },
    { key: 'bank', label: 'Item Bank', icon: Library, badge: questions.length },
    { key: 'assessments', label: 'Assessments', icon: ClipboardCheck, badge: assessments.length },
  ];

  return (
    <div className="flex min-h-screen flex-col bg-[#F8FAFC] text-[#0F172A]">
      {/* Institutional Top Navigation Bar */}
      <header className="no-print sticky top-0 z-30 border-b border-[#E2E8F0] bg-white">
        <div className="mx-auto flex h-14 max-w-[1440px] items-center justify-between gap-4 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <img src="/favicon.svg" alt="EduSoal AI" className="h-8 w-8 rounded-[4px]" />
            <div>
              <div className="font-display text-base font-bold leading-tight text-[#0F172A]">
                EduSoal AI
              </div>
              <div className="text-[10px] font-semibold uppercase tracking-[0.06em] text-[#2563EB]">
                Assessment Design Suite
              </div>
            </div>
          </div>

          {/* Desktop Navigation Tabs */}
          <nav className="hidden items-center gap-1 md:flex">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              const active = activeTab === tab.key;
              return (
                <button
                  key={tab.key}
                  type="button"
                  onClick={() => setActiveTab(tab.key)}
                  className={`inline-flex items-center gap-2 rounded-[4px] px-3.5 py-2 text-xs font-semibold transition-colors ${
                    active
                      ? 'bg-[#0F172A] text-white'
                      : 'text-[#64748B] hover:bg-[#F1F5F9] hover:text-[#0F172A]'
                  }`}
                >
                  <Icon className="h-3.5 w-3.5" />
                  <span>{tab.label}</span>
                  {typeof tab.badge === 'number' && (
                    <span
                      className={`rounded-[4px] px-1.5 py-0.5 text-[10px] ${
                        active
                          ? 'bg-white/20 text-white'
                          : 'bg-[#F1F5F9] text-[#45464D]'
                      }`}
                    >
                      {tab.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>

          <button
            type="button"
            onClick={() => setShowSettings(true)}
            aria-label="Workspace settings"
            className="inline-flex items-center gap-1.5 rounded-[4px] border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-1.5 text-xs font-semibold text-[#0F172A] hover:bg-[#F1F5F9]"
          >
            <Settings className="h-3.5 w-3.5 text-[#64748B]" />
            <span className="hidden sm:inline">Settings</span>
          </button>
        </div>
      </header>

      {/* Main Workspace */}
      <main className="flex-1 pb-20 md:pb-8">
        {activeTab === 'generator' && (
          <GeneratorWorkspace
            form={form}
            generatedItem={generatedItem}
            savedItemId={savedItemId}
            isGenerating={isGenerating}
            error={genError}
            onChangeForm={(patch) => setForm((prev) => ({ ...prev, ...patch }))}
            onGenerate={handleGenerate}
            onUpdateGeneratedItem={handleUpdateGenerated}
            onSaveToBank={handleSaveGeneratedToBank}
          />
        )}

        {activeTab === 'bank' && (
          <ItemBankWorkspace
            questions={questions}
            assessments={assessments}
            selectedQuestionId={selectedBankId}
            onSelectQuestion={setSelectedBankId}
            onCreateManual={(item) => {
              setQuestions((prev) => [item, ...prev]);
              setToast('New item added to Item Bank');
            }}
            onUpdateQuestion={(updated) => {
              setQuestions((prev) => prev.map((q) => (q.id === updated.id ? updated : q)));
              setToast('Item updated');
            }}
            onDeleteQuestion={(id) => {
              setQuestions((prev) => prev.filter((q) => q.id !== id));
              if (selectedBankId === id) setSelectedBankId(null);
              setToast('Item removed');
            }}
            onToggleInAssessment={handleToggleQuestionInAssessment}
          />
        )}

        {activeTab === 'assessments' && (
          <AssessmentsWorkspace
            assessments={assessments}
            questions={questions}
            selectedAssessmentId={selectedAssessmentId}
            onSelectAssessment={setSelectedAssessmentId}
            onCreateAssessment={(plan) => {
              setAssessments((prev) => [plan, ...prev]);
              setToast('Assessment matrix created');
            }}
            onUpdateAssessment={(updated) => {
              setAssessments((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
            }}
            onDeleteAssessment={(id) => {
              setAssessments((prev) => prev.filter((p) => p.id !== id));
              if (selectedAssessmentId === id) setSelectedAssessmentId(null);
              setToast('Assessment deleted');
            }}
          />
        )}
      </main>

      {/* Mobile Fixed Bottom Utility Navigation */}
      <nav className="no-print fixed bottom-0 left-0 right-0 z-30 grid grid-cols-3 border-t border-[#E2E8F0] bg-white md:hidden">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const active = activeTab === tab.key;
          return (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`flex flex-col items-center justify-center gap-1 py-2.5 text-[11px] font-semibold ${
                active ? 'text-[#2563EB]' : 'text-[#64748B]'
              }`}
            >
              <Icon className="h-4 w-4" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </nav>

      {/* Toast Notification */}
      {toast && (
        <div className="no-print fixed bottom-16 right-4 z-50 flex items-center gap-2 rounded-[4px] border border-[#059669]/30 bg-[#0F172A] px-3.5 py-2.5 text-xs font-semibold text-white shadow-lg md:bottom-5">
          <CheckCircle2 className="h-4 w-4 text-[#10B981]" />
          <span>{toast}</span>
        </div>
      )}

      {showSettings && (
        <SettingsModal
          onClose={() => setShowSettings(false)}
          onExportBackup={() => downloadJsonBackup(questions, assessments)}
          onSavedKey={(msg) => setToast(msg)}
        />
      )}
    </div>
  );
}

export default App;
