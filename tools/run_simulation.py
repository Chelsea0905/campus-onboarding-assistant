"""Exercise the local campus API against the fictional PDF corpus."""

import argparse
import json
import os
from pathlib import Path

import requests


ROOT = Path(__file__).resolve().parents[1]
MANIFEST = json.loads((ROOT / "sample-data" / "simulation-manifest.json").read_text(encoding="utf-8"))
CASES = json.loads((ROOT / "sample-data" / "simulation-cases.json").read_text(encoding="utf-8"))


def call(session, method, url, **kwargs):
    response = session.request(method, url, timeout=45, **kwargs)
    if response.status_code >= 400:
        raise RuntimeError(f"{method} {url}: HTTP {response.status_code}: {response.text[:300]}")
    return response.json() if response.content else None


def upload_corpus(session, base, token):
    records = []
    headers = {"X-Admin-Token": token}
    for metadata in MANIFEST:
        pdf_path = ROOT / "output" / "pdf" / metadata["file"]
        with pdf_path.open("rb") as pdf:
            result = call(session, "POST", base + "/campus/documents",
                          headers=headers, data={key: value for key, value in metadata.items() if key != "file"},
                          files={"file": (pdf_path.name, pdf, "application/pdf")})
        records.append({"file": metadata["file"], "id": result["id"],
                        "version": result["version"], "active": result["active"]})

    pdf_path = ROOT / "output" / "pdf" / MANIFEST[0]["file"]
    with pdf_path.open("rb") as pdf:
        duplicate = session.post(base + "/campus/documents", timeout=45, headers=headers,
                                 data={key: value for key, value in MANIFEST[0].items() if key != "file"},
                                 files={"file": (pdf_path.name, pdf, "application/pdf")})
    listed = call(session, "GET", base + "/campus/documents", headers=headers)
    shuttle = [item for item in listed if item["title"] == "2027级校园接驳通知"]
    checks = {
        "seven_uploads": len(records) == 7 and len(listed) == 7,
        "duplicate_rejected": duplicate.status_code == 400,
        "version_replaced": len(shuttle) == 2 and sorted((item["version"], item["active"]) for item in shuttle) == [(1, False), (2, True)],
        "expired_still_listed": any(item["title"] == "2025级旧版收费方式通知" and item["active"] for item in listed),
    }
    return records, checks


def run_answers(session, base, ai=False):
    selected = (CASES["answerCases"][:2] + CASES["refusalCases"][:1]) if ai else (
        CASES["answerCases"] + CASES["refusalCases"])
    rows = []
    for case in selected:
        result = call(session, "GET", base + "/campus/ask",
                      params={key: case[key] for key in ("q", "campus", "audience")})
        titles = [item["title"] for item in result["citations"]]
        if "expectedTitle" in case:
            passed = (not result["insufficientEvidence"] and case["expectedTitle"] in titles
                      and case["contains"] in result["answer"])
        else:
            passed = result["insufficientEvidence"] and not titles
        rows.append({"id": case["id"], "passed": passed, "answer": result["answer"],
                     "citations": result["citations"], "insufficientEvidence": result["insufficientEvidence"]})
    return rows


def run_checklists(session, base):
    rows = []
    for case in CASES["checklistCases"]:
        result = call(session, "GET", base + "/campus/checklist",
                      params={key: case[key] for key in ("campus", "audience", "housing")})
        titles = [item["title"] for item in result["items"]]
        rows.append({"id": case["id"], "passed": not result["demo"] and titles == case["expectedTitles"],
                     "titles": titles, "items": result["items"]})
    return rows


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://127.0.0.1:8125/api")
    parser.add_argument("--upload", action="store_true")
    parser.add_argument("--ai", action="store_true")
    parser.add_argument("--output", default="tmp/simulation-results.json")
    args = parser.parse_args()
    if args.upload and args.ai:
        parser.error("Upload corpus in extractive mode first, then run --ai against the same store")

    with requests.Session() as session:
        records, upload_checks = (upload_corpus(session, args.base_url, os.environ["CAMPUS_ADMIN_TOKEN"])
                                   if args.upload else ([], {}))
        answers = run_answers(session, args.base_url, args.ai)
        checklists = [] if args.ai else run_checklists(session, args.base_url)

    report = {"mode": "deepseek" if args.ai else "extractive", "uploads": records,
              "uploadChecks": upload_checks, "answerCases": answers, "checklistCases": checklists}
    output = ROOT / args.output
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    summary = {"upload": f"{sum(upload_checks.values())}/{len(upload_checks)}",
               "answers": f"{sum(row['passed'] for row in answers)}/{len(answers)}",
               "checklists": f"{sum(row['passed'] for row in checklists)}/{len(checklists)}",
               "failedIds": [row["id"] for row in answers + checklists if not row["passed"]],
               "output": str(output)}
    print(json.dumps(summary, ensure_ascii=False))


if __name__ == "__main__":
    main()
