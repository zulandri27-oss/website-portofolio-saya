import {
  BloomCode,
  DifficultyLevel,
  QuestionFormat,
  QuestionItem,
  getBloomMeta,
} from '../types/edusoal';

const LOCAL_KEY_STORAGE = 'edusoal_gemini_api_key_v1';

export function getStoredClientApiKey(): string {
  try {
    return localStorage.getItem(LOCAL_KEY_STORAGE) || '';
  } catch {
    return '';
  }
}

export function setStoredClientApiKey(key: string): void {
  try {
    const trimmed = key.trim();
    if (trimmed) {
      localStorage.setItem(LOCAL_KEY_STORAGE, trimmed);
    } else {
      localStorage.removeItem(LOCAL_KEY_STORAGE);
    }
  } catch {
    // Ignore storage unavailability
  }
}

interface GeneratePayload {
  subject: string;
  gradeLevel: string;
  topic: string;
  bloomLevel: BloomCode;
  format: QuestionFormat;
  difficulty: DifficultyLevel;
  customStimulus: string;
}

interface RawAiItem {
  stimulusTitle?: string;
  stimulusContent?: string;
  questionStem?: string;
  options?: string[];
  correctOptionIndex?: number;
  pedagogicalRationale?: string;
  scoringRubric?: string;
  curriculumAlignment?: string;
  estimatedMinutes?: number;
  discriminationIndex?: number;
}

const CLIENT_SCHEMA = {
  type: 'OBJECT',
  properties: {
    stimulusTitle: { type: 'STRING' },
    stimulusContent: { type: 'STRING' },
    questionStem: { type: 'STRING' },
    options: { type: 'ARRAY', items: { type: 'STRING' } },
    correctOptionIndex: { type: 'INTEGER' },
    pedagogicalRationale: { type: 'STRING' },
    scoringRubric: { type: 'STRING' },
    curriculumAlignment: { type: 'STRING' },
    estimatedMinutes: { type: 'INTEGER' },
    discriminationIndex: { type: 'NUMBER' },
  },
  required: [
    'stimulusTitle',
    'stimulusContent',
    'questionStem',
    'options',
    'correctOptionIndex',
    'pedagogicalRationale',
    'scoringRubric',
    'curriculumAlignment',
    'estimatedMinutes',
    'discriminationIndex',
  ],
};

function buildNormalizedItem(raw: RawAiItem, req: GeneratePayload): QuestionItem {
  const now = new Date().toISOString();
  const options =
    req.format === 'ESSAY'
      ? []
      : (raw.options || [])
          .map((o) => String(o).trim())
          .filter(Boolean)
          .slice(0, 6);

  const rawIdx = typeof raw.correctOptionIndex === 'number' ? raw.correctOptionIndex : 0;
  const correctOptionIndex =
    options.length === 0 ? -1 : Math.max(0, Math.min(options.length - 1, Math.round(rawIdx)));

  const estMinutes = Math.max(1, Math.min(120, Math.round(Number(raw.estimatedMinutes) || 3)));
  const discIndex = Math.max(0, Math.min(1, Number(raw.discriminationIndex) || 0.42));

  return {
    id: crypto.randomUUID(),
    subject: req.subject.trim() || 'General Studies',
    gradeLevel: req.gradeLevel.trim() || 'Grade 11',
    topic: req.topic.trim(),
    bloomLevel: req.bloomLevel,
    questionFormat: req.format,
    difficulty: req.difficulty,
    stimulusTitle: (raw.stimulusTitle || 'Context Stimulus').trim().slice(0, 160),
    stimulusContent: (raw.stimulusContent || '').trim().slice(0, 4000),
    questionStem: (raw.questionStem || '').trim().slice(0, 2000),
    options,
    correctOptionIndex,
    pedagogicalRationale: (raw.pedagogicalRationale || '').trim().slice(0, 2000),
    scoringRubric: (raw.scoringRubric || '').trim().slice(0, 2000),
    curriculumAlignment: (raw.curriculumAlignment || `${req.subject} · ${req.gradeLevel}`).trim().slice(0, 200),
    estimatedMinutes: estMinutes,
    discriminationIndex: discIndex,
    reviewStatus: 'DRAFT',
    createdAt: now,
    updatedAt: now,
  };
}

async function generateViaClientKey(req: GeneratePayload, apiKey: string): Promise<RawAiItem> {
  const bloomMeta = getBloomMeta(req.bloomLevel);
  const stimulusInstruction = req.customStimulus.trim()
    ? `Use and refine this educator-provided context stimulus (preserving its core data/facts):\n"""${req.customStimulus.trim()}"""`
    : 'Author an original, rigorous academic stimulus (short reading passage, experimental dataset summary, or primary scenario, 60-140 words) that students must analyze to answer.';

  const formatInstruction =
    req.format === 'ESSAY'
      ? 'Design an extended-response constructed prompt. Set `options` to [] and `correctOptionIndex` to -1, with a detailed analytical `scoringRubric`.'
      : req.format === 'COMPLEX_MC'
      ? 'Design a multi-statement or assertion-reason HOTS item with 4 synthesized analytical options (without A/B/C/D prefixes) and `correctOptionIndex` 0..3.'
      : 'Provide exactly 4 distinct, parallel-length options in `options` (without A/B/C/D prefixes) and `correctOptionIndex` 0..3.';

  const prompt = `
You are a senior psychometrician and curriculum assessment designer.
Create one institutional-grade assessment item with these specifications:
- Subject: ${req.subject}
- Grade / Level: ${req.gradeLevel}
- Topic / Objective: ${req.topic}
- Bloom's Cognitive Taxonomy: ${bloomMeta.code} (${bloomMeta.shortTitle} — ${bloomMeta.tierDescription})
- Question Format: ${req.format}
- Difficulty Tier: ${req.difficulty}

${stimulusInstruction}
${formatInstruction}
  `.trim();

  const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${encodeURIComponent(
    apiKey
  )}`;

  const res = await fetch(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      contents: [{ role: 'user', parts: [{ text: prompt }] }],
      generationConfig: {
        temperature: 0.4,
        responseMimeType: 'application/json',
        responseSchema: CLIENT_SCHEMA,
      },
    }),
  });

  const data = await res.json();
  if (!res.ok) {
    throw new Error(data?.error?.message || 'Gemini API request failed.');
  }
  const text = data?.candidates?.[0]?.content?.parts?.[0]?.text;
  if (!text) {
    throw new Error('Gemini returned an empty response.');
  }
  return JSON.parse(text) as RawAiItem;
}

export async function generateAssessmentItem(req: GeneratePayload): Promise<QuestionItem> {
  if (!req.topic.trim()) {
    throw new Error('Please enter a curriculum topic or learning objective.');
  }

  const bloomMeta = getBloomMeta(req.bloomLevel);
  const localOrEnvKey = getStoredClientApiKey() || (import.meta.env.VITE_GEMINI_API_KEY as string | undefined) || '';

  // 1. Try Vercel Serverless Function (/api/generate) first when deployed on Vercel
  try {
    const serverRes = await fetch('/api/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...req,
        bloomDescription: `${bloomMeta.shortTitle} — ${bloomMeta.tierDescription}`,
      }),
    });

    if (serverRes.ok) {
      const json = (await serverRes.json()) as RawAiItem;
      if (!json.questionStem) {
        throw new Error('Generated item missed a valid question stem.');
      }
      return buildNormalizedItem(json, req);
    }

    const errBody = await serverRes.json().catch(() => ({}));
    if (errBody?.code !== 'MISSING_API_KEY' && serverRes.status !== 404) {
      throw new Error(errBody?.error || 'Server AI generation failed.');
    }
  } catch (err) {
    // Only fall through to client key if /api/generate is unavailable locally or missing server key
    if (err instanceof Error && !err.message.includes('Failed to fetch') && !localOrEnvKey) {
      throw err;
    }
  }

  // 2. Fallback to optional browser/Vite client API key if configured
  if (localOrEnvKey) {
    const json = await generateViaClientKey(req, localOrEnvKey);
    if (!json.questionStem) {
      throw new Error('Generated item missed a valid question stem.');
    }
    return buildNormalizedItem(json, req);
  }

  throw new Error(
    'AI generation requires a Gemini API key. On Vercel, add GEMINI_API_KEY in Project Settings → Environment Variables, or add a key in Settings (gear icon).'
  );
}
