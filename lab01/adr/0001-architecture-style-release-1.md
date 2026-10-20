# ADR-0001: Build release 1 as a modular monolith batch job

Status: Accepted
Date: 2026-10-20 · Deciders: Rasmey Rithysak, Ouk Puthirith

## Context
Head office needs the monthly report on the morning of day 2 of M+1 from about 3 M rows (brief). Our architectural drivers from lab01/quality-scenarios.md are QA-1 (3 M rows in 60 s or less, (H,H)), QA-3 (a late REP file must not block PNH and BTB, (H,M)) and QA-5 (a branch manager sees only their own branch, (H,M)). The team has two members, about eight weeks of labs, and Java 25 with Maven. The job runs once a night and once a month, not continuously.

## Decision
We will build release 1 as one Java application (a modular monolith) that runs as a batch job. Ingest, engine and render are separate packages with enforced dependency rules (DependencyRulesTest), but they run in one JVM and call each other in-process; the C4 container diagram therefore shows them as one container, the report batch. The ingest step will parse files in parallel on the 8 cores, and finished reports are written to a report store on disk.

## Alternatives considered
- Process pipeline (a coordinator plus worker processes over pipes or local sockets): attractive for isolating failures and for scaling over several machines. We did not choose it because for 3 M rows one JVM should already meet QA-1 by a rough estimate (assumption, to be confirmed by measuring in a later lab: 5 us per row on one core for parsing, so 15 s, divided over 8 cores, before aggregation and rendering are added), and the extra processes add serialization and startup cost with no QA-3 benefit.
- Event-driven microservices (ingest, aggregation, report and notification services behind a message broker): attractive because a broker buffers late files (QA-3) and services scale separately. We did not choose it because one event per row (3 M events) would need about 60 s of broker time alone at an assumed 50 000 msg/s, which leaves no margin against the 60 s of QA-1, and a two-person team cannot realistically operate a broker and four services.

## Consequences
+ One JVM with 8 parser threads is expected to leave a large margin against the 60 s target (QA-1), to be confirmed by measurement.
+ One deployable and no network hops make the engine testable on in-memory fixtures in under 30 s (QA-6).
+ DependencyRulesTest keeps model independent of the other packages and ingest unaware of rendering, and the sealed ReportRenderer makes a new export format a change in few files (QA-4).
- A crash in one branch's parsing can take down the whole run, so ingest must catch per-file errors itself and QA-3 depends on that discipline.
- Scaling is limited to one machine; a fourth branch fits QA-2, but 10 times the volume does not.
- Access control for managers (QA-5) must be built in the web page, since there is no service boundary that enforces it.

Revisit when: a month exceeds 10 M rows, the run time of QA-1 goes above 30 s, or head office asks for reports within minutes of each branch upload.

## Review
Reviewed by: Ouk Puthirith, 2026-10-20
- Comment 1: the estimate of 5 us per row covers parsing only, but QA-1 also includes aggregation and the PDF, Excel and web output, and it is an assumption, not a measurement.
- Comment 2: "fails QA-1" for 60 s of broker time is borderline, because the QA-1 target is "60 s or less"; the real point is that there is no margin left.
- Comment 3: the C4 container diagram showed the ingest job and the report batch as two containers linked in-process, which contradicts "one JVM".
Resolution: the estimate is now labelled as an assumption for parsing only, to be measured later; the wording is "leaves no margin"; the diagram now shows one report batch container, and the Decision says so.
