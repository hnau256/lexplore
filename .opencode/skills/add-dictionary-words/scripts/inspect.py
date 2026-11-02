#!/usr/bin/env python3
"""Inspect LEXplore dictionary entries and SUBTLEX-GR frequencies.

Usage:
    python3 inspect.py                 # summary
    python3 inspect.py "слово" "γράφω" # search words.json and word_counts.txt
"""

from __future__ import annotations

import json
import sys
import unicodedata
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
WORDS = ROOT / "data" / "words.json"
COUNTS = ROOT / "data" / "word_counts.txt"


def strip_marks(value: str) -> str:
    return "".join(
        char
        for char in unicodedata.normalize("NFD", value)
        if unicodedata.category(char) != "Mn"
    )


def is_greek(value: str) -> bool:
    return any("GREEK" in unicodedata.name(char, "") for char in value)


def load_words() -> list[dict]:
    if not WORDS.exists():
        raise SystemExit(f"File not found: {WORDS}")
    return json.loads(WORDS.read_text(encoding="utf-8"))["words"]


def load_counts() -> list[tuple[str, int]]:
    if not COUNTS.exists():
        return []
    result: list[tuple[str, int]] = []
    for line in COUNTS.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#") or stripped.startswith("//"):
            continue
        parts = [part.strip() for part in stripped.split("|") if part.strip()]
        if len(parts) < 3:
            continue
        try:
            result.append((parts[1].lower(), int(parts[2])))
        except ValueError:
            continue
    return result


def print_summary(words: list[dict]) -> None:
    print(f"entries: {len(words)}")
    print(f"active: {sum(1 for word in words if word.get('status') == 'active')}")
    print(f"hidden: {sum(1 for word in words if word.get('status') == 'hidden')}")
    print("themes:", dict(Counter(word["theme"] for word in words).most_common()))
    print("levels:", dict(Counter(word["level"] for word in words).most_common()))
    print("pos:", dict(Counter(word["pos"] for word in words).most_common()))


def print_json_matches(words: list[dict], query: str) -> None:
    lowered = query.lower()
    matches = [
        (index, word)
        for index, word in enumerate(words)
        if (
            lowered in word["word"].lower()
            or lowered in word["translation"].lower()
            or lowered in word.get("lemma", "").lower()
        )
    ]
    print(f"\nwords.json matches for {query!r}: {len(matches)}")
    for index, word in matches[:40]:
        extra = []
        if word.get("lemma"):
            extra.append(f"lemma={word['lemma']}")
        if word.get("weight") is not None:
            extra.append(f"weight={word['weight']}")
        if word.get("subtheme"):
            extra.append(f"subtheme={word['subtheme']}")
        print(
            f"  [{index}] {word['word']} | {word['translation']} | "
            f"{word['pos']} | {word['level']} | {word['theme']} | "
            f"{word.get('status', 'active')}"
            + (f" | {'; '.join(extra)}" if extra else "")
        )


def print_count_matches(counts: list[tuple[str, int]], query: str) -> None:
    if not is_greek(query):
        return
    lowered = query.lower()
    normalized = strip_marks(lowered)
    exact = [(word, count) for word, count in counts if word == lowered]
    prefix = [
        (word, count)
        for word, count in counts
        if word != lowered
        and (word.startswith(lowered) or strip_marks(word).startswith(normalized))
    ]
    prefix.sort(key=lambda item: item[1], reverse=True)
    print(f"\nword_counts.txt matches for {query!r}: {len(exact) + len(prefix)}")
    for word, count in exact:
        print(f"  {word} = {count}")
    for word, count in prefix[:30]:
        print(f"  ~ {word} = {count}")


def main() -> None:
    words = load_words()
    queries = sys.argv[1:]
    if not queries:
        print_summary(words)
        return
    counts = load_counts()
    for query in queries:
        print_json_matches(words, query)
        print_count_matches(counts, query)


if __name__ == "__main__":
    main()
