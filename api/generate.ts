// Vercel Serverless Function: POST /api/generate
// Reads GEMINI_API_KEY from Vercel Environment Variables so the key stays server-side.

interface VercelRequest {
  method?: string;
  body?: {
    subject?: string;
    gradeLevel?: string;
    topic?: string;
    bloomLevel?: string;
    bloomDescription?: string;
    format?: string;
    difficulty?: string;
    customStimulus?: string;
  };
}

interface VercelResponse {
  status: (code: number) => VercelResponse;
  json: (data: unknown) => void;
}

const ITEM_RESPONSE_SCHEMA = {
  type: 'OBJECT',
  properties: {
    stimulusTitle: {
      type: 'STRING',
      description: 'Concise title for the context stimulus, reading passage, or empirical scenario',
    },
    stimulusContent: {
      type: 'STRING',
      description: 'Rich academic context stimulus (data excerpt, primary source, or experimental brief) grounding the question',
    },
    questionStem: {
      type: 'STRING',
      description: 'Clear, unambiguous academic question stem aligned to the requested Bloom level',
    },
    options: {
      type: 'ARRAY',
      items: { type: 'STRING' },
      description: 'Exactly 4 plausible academic options (A-D) for MULTIPLE_CHOICE or COMPLEX_MC, or an empty array for ESSAY',
    },
    correctOptionIndex: {
      type: 'INTEGER',
      description: '0-based index (0..3) of the single best answer for objective items, or -1 for ESSAY',
    },
    pedagogicalRationale: {
      type: 'STRING',
      description: 'Detailed pedagogical explanation of the correct reasoning and why distractors fail',
    },
    scoringRubric: {
      type: 'STRING',
      description: 'Structured grading criteria and point allocation',
    },
    curriculumAlignment: {
      type: 'STRING',
      description: 'Relevant curriculum standard code or learning competency descriptor',
    },
    estimatedMinutes: {
      type: 'INTEGER',
      description: 'Realistic student completion time in minutes (1 to 30)',
    },
    discriminationIndex: {
      type: 'NUMBER',
      description: 'Estimated psychometric discrimination index between 0.20 and 0.85',
    },
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

export default async function handler(req: VercelRequest, res: VercelResponse) {
  if (req.method !== 'POST') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    res.status(503).json({
      code: 'MISSING_API_KEY',
      error: 'GEMINI_API_KEY is not configured in Vercel Environment Variables.',
    });
    return;
  }

  const body = req.body || {};
  const subject = (body.subject || 'General Studies').trim().slice(0, 60);
  const gradeLevel = (body.gradeLevel || 'Grade 11').trim().slice(0, 40);
  const topic = (body.topic || '').trim().slice(0, 150);
  const bloomLevel = (body.bloomLevel || 'C4').trim();
  const bloomDescription = (body.bloomDescription || '').trim();
  const format = (body.format || 'MULTIPLE_CHOICE').trim();
  const difficulty = (body.difficulty || 'HOTS').trim();
  const customStimulus = (body.customStimulus || '').trim().slice(0, 2500);

  if (!topic) {
    res.status(400).json({ error: 'Topic or learning objective is required.' });
    return;
  }

  const stimulusInstruction = customStimulus
    ? `Use and refine this educator-provided context stimulus (preserving its core data/facts):\n"""${customStimulus}"""`
    : 'Author an original, rigorous academic stimulus (short reading passage, experimental dataset summary, or primary scenario, 60-140 words) that students must analyze to answer.';

  const formatInstruction =
    format === 'ESSAY'
      ? 'Design an extended-response constructed prompt. Set `options` to an empty array [] and `correctOptionIndex` to -1, and provide a detailed multi-level analytical `scoringRubric`.'
      : format === 'COMPLEX_MC'
      ? 'Design a multi-statement or assertion-reason HOTS item with 4 synthesized analytical options in `options` (without A/B/C/D prefixes) and set `correctOptionIndex` to 0, 1, 2, or 3.'
      : 'Provide exactly 4 distinct, parallel-length options in the `options` array (without A/B/C/D prefixes) and set `correctOptionIndex` to 0, 1, 2, or 3.';

  const prompt = `
You are a senior psychometrician and curriculum assessment designer.
Create one institutional-grade assessment item with these exact specifications:
- Subject: ${subject}
- Grade / Level: ${gradeLevel}
- Topic / Objective: ${topic}
- Bloom's Cognitive Taxonomy: ${bloomLevel} (${bloomDescription})
- Question Format: ${format}
- Difficulty Tier: ${difficulty}

Stimulus requirement:
${stimulusInstruction}

Format requirement:
${formatInstruction}

Ensure high academic precision, zero ambiguity, plausible distractors rooted in common student misconceptions, and accurate pedagogical metadata.
  `.trim();

  const model = process.env.GEMINI_MODEL || 'gemini-2.5-flash';
  const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${encodeURIComponent(
    apiKey
  )}`;

  try {
    const upstream = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ role: 'user', parts: [{ text: prompt }] }],
        generationConfig: {
          temperature: 0.4,
          responseMimeType: 'application/json',
          responseSchema: ITEM_RESPONSE_SCHEMA,
        },
      }),
    });

    const data = (await upstream.json()) as {
      candidates?: Array<{ content?: { parts?: Array<{ text?: string }> } }>;
      error?: { message?: string };
    };

    if (!upstream.ok) {
      res.status(upstream.status).json({
        error: data?.error?.message || 'Upstream Gemini request failed.',
      });
      return;
    }

    const rawText = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!rawText) {
      res.status(502).json({ error: 'Gemini returned an empty response.' });
      return;
    }

    const parsed = JSON.parse(rawText);
    res.status(200).json(parsed);
  } catch (err) {
    const message = err instanceof Error ? err.message : 'Unexpected server error.';
    res.status(500).json({ error: message });
  }
}
