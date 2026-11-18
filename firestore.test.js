const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_educator_1";
const BOB_UID = "bob_educator_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validQuestionPayload(uid, nowMs = Date.now() - 5000) {
  const ts = new Date(nowMs);
  return {
    userId: uid,
    subject: "Biology",
    gradeLevel: "Grade 11",
    topic: "Cellular Respiration & ATP Yield",
    bloomLevel: "C4",
    questionFormat: "MULTIPLE_CHOICE",
    difficulty: "HOTS",
    stimulusTitle: "Respirometer Oxygen Consumption Trial",
    stimulusContent: "Students measured oxygen uptake in germinating peas at 10C and 25C over 20 minutes.",
    questionStem: "Which hypothesis best explains the steeper slope of oxygen consumption at 25C?",
    options: [
      "Higher kinetic energy accelerates dehydrogenase enzyme catalysis in the Krebs cycle.",
      "Peas absorb atmospheric nitrogen faster at elevated temperatures.",
      "Glycolysis ceases completely below 15C.",
      "Oxygen gas dissolves more readily at higher temperatures."
    ],
    correctOptionIndex: 0,
    pedagogicalRationale: "Requires analyzing experimental thermal trends against enzymatic kinetics in aerobic respiration.",
    scoringRubric: "1 pt for identifying enzymatic kinetic acceleration; 0 pts for distractors.",
    curriculumAlignment: "NGSS HS-LS1-7",
    estimatedMinutes: 3,
    discriminationIndex: 0.45,
    reviewStatus: "DRAFT",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validAssessmentPayload(uid, questionIds = ["q_1"], nowMs = Date.now() - 5000) {
  const ts = new Date(nowMs);
  return {
    userId: uid,
    title: "Unit 3 Bioenergetics Formative Matrix",
    subject: "Biology",
    gradeLevel: "Grade 11",
    description: "Assesses C3-C5 mastery of cellular respiration.",
    questionIds,
    totalMinutes: 15,
    status: "DRAFT",
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot list, read, or write questions or assessments", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("questions").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("questions").doc("q_1").set(validQuestionPayload(ALICE_UID))
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("assessments").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("assessments").doc("a_1").set(validAssessmentPayload(ALICE_UID))
  );
});

test("Owner can create, list, update status/key, and delete QuestionItem with Kotlin-aligned payload", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const qCol = aliceDb.collection("users").doc(ALICE_UID).collection("questions");

  await assertSucceeds(qCol.doc("q_1").set(validQuestionPayload(ALICE_UID)));
  await assertSucceeds(qCol.get());

  // Action: reviewStatus transition
  await assertSucceeds(
    qCol.doc("q_1").update({
      reviewStatus: "APPROVED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Action: answer key toggle
  await assertSucceeds(
    qCol.doc("q_1").update({
      correctOptionIndex: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field & immutable createdAt mutation
  await assertFails(
    qCol.doc("q_2").set({
      ...validQuestionPayload(ALICE_UID),
      id: "q_2",
    })
  );
  await assertFails(
    qCol.doc("q_1").update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );
  await assertFails(
    qCol.doc("q_1").update({
      reviewStatus: "INVALID_STATUS",
      updatedAt: new Date(Date.now() - 100),
    })
  );
  await assertFails(
    qCol.doc("q_3").set(validQuestionPayload(ALICE_UID, Date.now() + 60000))
  );

  await assertSucceeds(qCol.doc("q_1").delete());
});

test("Cross-user isolation blocks Bob from reading or writing Alice's questions and assessments", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).collection("questions").doc("q_alice").set(validQuestionPayload(ALICE_UID));
    await db.collection("users").doc(ALICE_UID).collection("assessments").doc("a_alice").set(validAssessmentPayload(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("questions").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("questions").doc("q_alice").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("questions").doc("q_alice").update({
      reviewStatus: "APPROVED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("assessments").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("assessments").doc("a_alice").delete()
  );
});

test("Owner can create, update, and list AssessmentPlan with valid bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aCol = aliceDb.collection("users").doc(ALICE_UID).collection("assessments");

  await assertSucceeds(aCol.doc("a_1").set(validAssessmentPayload(ALICE_UID)));
  await assertSucceeds(aCol.get());

  await assertSucceeds(
    aCol.doc("a_1").update({
      questionIds: ["q_1", "q_2"],
      totalMinutes: 25,
      status: "READY",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field on update
  await assertFails(
    aCol.doc("a_1").update({
      isAdminOverride: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
