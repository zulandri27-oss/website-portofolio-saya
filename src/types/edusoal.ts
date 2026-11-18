export type BloomCode = 'C1' | 'C2' | 'C3' | 'C4' | 'C5' | 'C6';

export type QuestionFormat = 'MULTIPLE_CHOICE' | 'COMPLEX_MC' | 'ESSAY';

export type DifficultyLevel = 'FOUNDATIONAL' | 'STANDARD' | 'HOTS';

export type ReviewStatus = 'DRAFT' | 'VALIDATED' | 'APPROVED';

export type AssessmentStatus = 'DRAFT' | 'READY' | 'PUBLISHED';

export interface BloomMeta {
  code: BloomCode;
  title: string;
  shortTitle: string;
  tierDescription: string;
  tierGroup: 'LOW' | 'MID' | 'HIGH';
}

export const BLOOM_LEVELS: BloomMeta[] = [
  {
    code: 'C1',
    title: 'C1 · Recall',
    shortTitle: 'Recall',
    tierDescription: 'Retrieve factual knowledge from memory',
    tierGroup: 'LOW',
  },
  {
    code: 'C2',
    title: 'C2 · Comprehension',
    shortTitle: 'Understand',
    tierDescription: 'Construct meaning and summarize concepts',
    tierGroup: 'LOW',
  },
  {
    code: 'C3',
    title: 'C3 · Application',
    shortTitle: 'Apply',
    tierDescription: 'Execute procedures in novel situations',
    tierGroup: 'MID',
  },
  {
    code: 'C4',
    title: 'C4 · Analysis',
    shortTitle: 'Analyze',
    tierDescription: 'Deconstruct relationships and patterns',
    tierGroup: 'MID',
  },
  {
    code: 'C5',
    title: 'C5 · Evaluation',
    shortTitle: 'Evaluate',
    tierDescription: 'Critique claims against criteria and evidence',
    tierGroup: 'HIGH',
  },
  {
    code: 'C6',
    title: 'C6 · Creation',
    shortTitle: 'Create',
    tierDescription: 'Synthesize original hypotheses or designs',
    tierGroup: 'HIGH',
  },
];

export const FORMAT_OPTIONS: { value: QuestionFormat; label: string }[] = [
  { value: 'MULTIPLE_CHOICE', label: 'Multiple Choice' },
  { value: 'COMPLEX_MC', label: 'Complex Analysis' },
  { value: 'ESSAY', label: 'Essay & Rubric' },
];

export const DIFFICULTY_OPTIONS: { value: DifficultyLevel; label: string }[] = [
  { value: 'FOUNDATIONAL', label: 'Foundational' },
  { value: 'STANDARD', label: 'Standard' },
  { value: 'HOTS', label: 'HOTS' },
];

export interface CurriculumTemplate {
  label: string;
  subject: string;
  gradeLevel: string;
  topic: string;
  bloomLevel: BloomCode;
  format: QuestionFormat;
  difficulty: DifficultyLevel;
}

export const CURRICULUM_TEMPLATES: CurriculumTemplate[] = [
  {
    label: 'Biology · C4',
    subject: 'Biology',
    gradeLevel: 'Grade 11',
    topic: 'Enzyme kinetics and thermal denaturation in cellular respiration',
    bloomLevel: 'C4',
    format: 'MULTIPLE_CHOICE',
    difficulty: 'HOTS',
  },
  {
    label: 'Economics · C5',
    subject: 'Economics',
    gradeLevel: 'Grade 12',
    topic: 'Central bank interest rate policy vs. inflation targeting trade-offs',
    bloomLevel: 'C5',
    format: 'COMPLEX_MC',
    difficulty: 'HOTS',
  },
  {
    label: 'Physics · C3',
    subject: 'Physics',
    gradeLevel: 'Grade 11',
    topic: 'Conservation of mechanical energy on an inclined plane with friction',
    bloomLevel: 'C3',
    format: 'MULTIPLE_CHOICE',
    difficulty: 'STANDARD',
  },
  {
    label: 'Literature · C6',
    subject: 'Literature',
    gradeLevel: 'Grade 12',
    topic: 'Evaluating unreliable narration in modernist short fiction',
    bloomLevel: 'C6',
    format: 'ESSAY',
    difficulty: 'HOTS',
  },
];

export interface QuestionItem {
  id: string;
  subject: string;
  gradeLevel: string;
  topic: string;
  bloomLevel: BloomCode;
  questionFormat: QuestionFormat;
  difficulty: DifficultyLevel;
  stimulusTitle: string;
  stimulusContent: string;
  questionStem: string;
  options: string[];
  correctOptionIndex: number;
  pedagogicalRationale: string;
  scoringRubric: string;
  curriculumAlignment: string;
  estimatedMinutes: number;
  discriminationIndex: number;
  reviewStatus: ReviewStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AssessmentPlan {
  id: string;
  title: string;
  subject: string;
  gradeLevel: string;
  description: string;
  questionIds: string[];
  totalMinutes: number;
  status: AssessmentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface GeneratorFormState {
  subject: string;
  gradeLevel: string;
  topic: string;
  bloomLevel: BloomCode;
  format: QuestionFormat;
  difficulty: DifficultyLevel;
  customStimulus: string;
}

export function getBloomMeta(code: BloomCode): BloomMeta {
  return BLOOM_LEVELS.find((b) => b.code === code) || BLOOM_LEVELS[3];
}
