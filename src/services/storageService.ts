import { AssessmentPlan, QuestionItem } from '../types/edusoal';

const QUESTIONS_KEY = 'edusoal_questions_v1';
const ASSESSMENTS_KEY = 'edusoal_assessments_v1';

export function loadQuestions(): QuestionItem[] {
  try {
    const raw = localStorage.getItem(QUESTIONS_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? (parsed as QuestionItem[]) : [];
  } catch {
    return [];
  }
}

export function saveQuestions(items: QuestionItem[]): void {
  try {
    localStorage.setItem(QUESTIONS_KEY, JSON.stringify(items));
  } catch {
    // Ignore quota/storage errors
  }
}

export function loadAssessments(): AssessmentPlan[] {
  try {
    const raw = localStorage.getItem(ASSESSMENTS_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? (parsed as AssessmentPlan[]) : [];
  } catch {
    return [];
  }
}

export function saveAssessments(plans: AssessmentPlan[]): void {
  try {
    localStorage.setItem(ASSESSMENTS_KEY, JSON.stringify(plans));
  } catch {
    // Ignore quota/storage errors
  }
}

export function downloadJsonBackup(questions: QuestionItem[], assessments: AssessmentPlan[]): void {
  const payload = {
    exportedAt: new Date().toISOString(),
    app: 'EduSoal AI',
    version: 1,
    questions,
    assessments,
  };
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `edusoal-bank-${new Date().toISOString().slice(0, 10)}.json`;
  link.click();
  URL.revokeObjectURL(url);
}
