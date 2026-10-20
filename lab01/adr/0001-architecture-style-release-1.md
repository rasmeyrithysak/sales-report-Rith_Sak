# ADR-0001: Build release 1 as a modular monolith batch job

Status: Accepted
Date: 2026-10-20 · Deciders: Rasmey Rithysak, Ouk Puthirith

## Context
Head office needs the monthly report on the morning of day 2 of M+1 from about 3 M rows (brief). Our architectural drivers from lab01/quality-scenarios.md are QA-1 (3 M rows in 60 s or less, (H,H)), QA-3 (a late REP file must not block PNH and BTB, (H,M)) and QA-5 (a branch manager sees only their own branch, (H,M)). The team has two members, about eight weeks of labs, and Java 25 with Maven. The job runs once a night and once a month, not continuously.

## Decision
We will build release 1 as one Java application (a modular monolith) that runs as a batch job. Ingest, engine and render are separate packages with enforced dependency rules (DependencyRulesTest), but they run in one JVM and call each other in-process. The ingest step parses files in parallel on the 8 cores, and finished reports are written to a report store on disk.

## Alternatives considered
- Process pipeline (a coordinator plus worker processes over pipes or local sockets): attractive for isolating failures and for scaling over several machines. We did not choose it because for 3 M rows one JVM already meets QA-1 by a rough estimate (assuming 5 us per row on one core, so 15 s, divided over 8 cores), and the extra processes add serialization and startup cost with no QA-3 benefit.
- Event-driven microservices (ingest, aggregation, report and notification services behind a message broker): attractive because a broker buffers late files (QA-3) and services scale separately. We did not choose it because one event per row (3 M events) would need about 60 s of broker time alone at 50 000 msg/s, which fails QA-1, and a two-person team cannot operate a broker and four services.

## Consequences
+ One JVM with 8 parser threads leaves a large margin against the 60 s target (QA-1).
+ One deployable and no network hops make the engine testable on in-memory fixtures in under 30 s (QA-6).
+ Package boundaries checked by DependencyRulesTest keep render replaceable, so a new export format touches few files (QA-4).
- A crash in one branch's parsing can take down the whole run, so ingest must catch per-file errors itself and QA-3 depends on that discipline.
- Scaling is limited to one machine; a fourth or fifth branch is fine, but 10 times the volume is not (QA-2).
- Access control for managers (QA-5) must be built in the web page, since there is no service boundary that enforces it.

Revisit when: a month exceeds 10 M rows, the run time of QA-1 goes above 30 s, or head office asks for reports within minutes of each branch upload.
