---
name: add-dictionary-words
description: Use when the user asks to add Russian or Greek words/phrases to the LEXplore dictionary, update data/words.json, add lemmas or verb aorists, or run the dictionary resource build. Triggers on "добавь слова", "add Greek words", "words.json", "аорист", "словарь".
---

# Add words to LEXplore dictionary

## User input

The user normally gives only a list of words, for example:

```text
дом, вода, бежать, γάτα, γράφω
```

Do **all** the rest yourself:

- identify Russian vs Greek;
- find the Greek lemma and Russian translation;
- normalize to initial forms;
- add aorists for verbs;
- choose `pos`, `level`, `theme`, `subtheme`;
- resolve frequency/weight;
- edit `data/words.json`;
- run the generator and fix every error.

Do not ask the user to supply translations, levels, themes, aorists, JSON or weights unless there is a real ambiguity that changes the answer.

## Source of truth

- `data/words.json` — only source of dictionary entries.
- `data/word_counts.txt` — SUBTLEX-GR frequency list. Never edit it unless explicitly asked.
- `android/src/main/res/raw/dictionaries.json` — generated output, never edit manually.

## Entry shape

```json
{
  "word": "σπίτι",
  "translation": "дом",
  "pos": "noun",
  "level": "A1",
  "theme": "Быт",
  "subtheme": "Дом",
  "status": "active"
}
```

Optional fields:
- `lemma` — for forms, points to the initial form;
- `weight` — only when the word has no frequency in `word_counts.txt`;
- `notes` — short clarification if needed.

`pos` values:
- plain: `noun`, `verb`, `adjective`, `adverb`, `pronoun`, `numeral`, `phrase`, `particle`, `preposition`, `conjunction`, `interjection`;
- with form: `noun [declension]`, `verb [aorist]`, `verb [conjugation]`, `verb [irregular]`.

`level`: `A1`, `A2`, `B1`, `B2`, `C1`, `C2`.
`status`: `active` or `hidden`.

## Word rules

1. Always use the **initial form**:
   - noun — nominative singular;
   - adjective — masculine nominative singular;
   - verb — 1st person singular present (`γράφω`, not `γράψω`/`γράφεις`);
   - adverb — positive form.
2. No articles, pronouns, particles, parentheses, brackets or explanatory words in `word`.
   - Good: `γράφω`
   - Bad: `[εγώ] γράφω`, `ο γράφω`, `γράφω (να γράψω)`
3. Do not add declensions/conjugations for regular vocabulary. Grammar forms only when explicitly requested.
4. Russian translation must uniquely identify the Greek word:
   - check for duplicate translations globally;
   - if the meaning is taken, use a distinct shade in parentheses (`шум` → `галдёж`);
   - never rely on dictionary/theme context to disambiguate.
5. Check for duplicate Greek words too. If the word already exists, update it instead of adding a duplicate.
6. If the input is an inflected form, normalize it to the lemma and add the lemma, not the form.

## Verbs and aorists

For every new verb, add two entries when the aorist differs from the present:

```json
{
  "word": "σβήνω",
  "translation": "тушить, выключать",
  "pos": "verb",
  "level": "B1",
  "theme": "Быт",
  "subtheme": "Дом",
  "status": "active"
},
{
  "word": "έσβησα",
  "translation": "потушил",
  "pos": "verb [aorist]",
  "level": "B1",
  "theme": "Быт",
  "subtheme": "Дом",
  "lemma": "σβήνω",
  "status": "active"
}
```

Rules:
- aorist is 1st person singular past;
- aorist gets its own real Russian past translation (`поискал`, `написал`), not `(аорист)`;
- `lemma` must exactly match the present form;
- the aorist translation must also be globally unique;
- if aorist equals the present form, add only one entry;
- for verbs without a true aorist (`είμαι`, `έχω`, `ξέρω`) use the past form (`ήμουν`, `είχα`, `ήξερα`) and `pos: "verb [aorist]"`.

## Theme and level

Use existing themes when possible:
`Быт`, `Время`, `Глаголы`, `Город`, `Грамматика`, `Досуг`, `Здоровье`, `Описания`, `Природа`, `Работа, учёба, деньги`, `Разное`, `Религия`, `Фразы`, `Человек`, `Числа`, `Ядро`.

- Pick the closest existing theme; use `Разное` only when nothing fits.
- `subtheme` is optional; keep it consistent with nearby entries.
- Level is a best-effort estimate:
  - A1/A2 — everyday concrete words and very frequent basics;
  - B1 — common abstract and everyday verbs;
  - B2 — less frequent, abstract, formal or specialized vocabulary;
  - C1/C2 — literary, highly specialized or rare words.
- If unsure, choose the closest lower level and mention it in the report.

## Frequency and weights

Leave `weight` out by default. The generator resolves it from `data/word_counts.txt`.

If the build says `Unknown count of word(s)`:
1. look for related inflected forms in `word_counts.txt`;
2. if an obvious related form exists, set a conservative explicit `weight` (for example, the sum of the relevant forms);
3. if not obvious, use a small manual weight and explain it in the report.

Do not invent large weights.

## Autonomy and ambiguity

- Make reasonable choices and report them; do not turn the task into a questionnaire.
- If one Russian word has several common Greek equivalents with different meanings, add all of them with distinct Russian translations.
- If a word is a proper name, slang or highly ambiguous, add the most likely general-language meaning and note the assumption.
- Ask the user only when:
  - the requested word has no clear single meaning and the choice changes the dictionary entry;
  - the user asks for a specific dialect/style/register;
  - the word is offensive/sensitive and inclusion is unclear.
- Prefer a dictionary check or an existing project entry over machine translation when the form is uncertain.

## Workflow

1. Inspect existing entries and frequencies:
   ```bash
   python3 .opencode/skills/add-dictionary-words/scripts/inspect.py "слово" "γράφω"
   ```
   With no arguments it prints themes, levels and counts.

2. Prepare all entries yourself:
   - lemma and translation;
   - aorist and past translation for verbs;
   - `pos`, `level`, `theme`, optional `subtheme`;
   - `status: "active"`.

3. Edit `data/words.json`:
   - keep valid JSON, 2-space indentation;
   - append new objects to the `words` array;
   - do not reformat unrelated entries;
   - keep optional fields only when needed.

4. Run the generator:
   ```bash
   ./gradlew :android:buildDictionariesResource --offline
   ```
   If offline resolution fails, run again without `--offline`.

5. Fix every reported error:
   - duplicate word/translation;
   - missing `lemma`;
   - unknown part of speech/level/status;
   - invalid Greek word;
   - missing frequency without `weight`.

6. Optional final check:
   ```bash
   ./gradlew :android:assembleDebug --offline
   ```

7. Report briefly:
   - added words and aorists;
   - assumptions about ambiguous translations/themes/levels;
   - any explicit weights;
   - that the build passes.

## Do not

- Do not edit `android/src/main/res/raw/dictionaries.json`;
- do not edit `data/word_counts.txt`;
- do not add articles/brackets/pronouns to `word`;
- do not reuse the same Russian translation for different Greek words;
- do not ask the user to prepare metadata or JSON;
- do not commit unless the user asks.
