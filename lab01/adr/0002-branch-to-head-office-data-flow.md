# ADR-0002: Branches push one CSV file per day over SFTP

Status: Accepted
Date: 2026-10-20 · Deciders: Rasmey Rithysak, Ouk Puthirith

## Context
Each branch POS exports one CSV file per day, BRANCH-yyyy-MM-dd.csv, of about 33 000 rows, and the file arrives by 23:00 over the branch's internet link; links fail now and then (brief). Head office wants the month M report on the morning of day 2 of M+1. The drivers are QA-3 (REP file about 11 h late, arriving at 10:00 on day 2: PNH and BTB reports out by 07:00, (H,M)), QA-1 (3 M rows in 60 s or less, (H,H)) and QA-5 (a branch manager sees only their own branch, (H,M)). The POS systems already produce files; we cannot change them in release 1.

## Decision
We will let each branch push its daily file to the head-office landing zone over SFTP, one account per branch with write access to its own folder only. The batch's ingest step reads the files from the landing zone, and the report batch runs at 06:00 on day 2 of M+1.
Failure policy (QA-3):
- Wait: head office waits for missing files until 06:00 on day 2; it does not wait beyond that.
- Partial report: at 06:00 the batch publishes the reports of every branch whose files are complete (PNH and BTB by 07:00), and marks a branch with missing days as "partial", listing the missing dates, in its own report and in the chain total.
- Late file: when a missing file arrives (we assume within 24 h, A4), the batch detects it and regenerates that branch's report and the chain report within 10 min. We assume the landing zone is checked every 5 min, so detection plus regeneration fits the 10 min of QA-3. The new report gets a higher version number, and the "partial" mark is removed once no other day of that branch is missing. Later arrivals need a manual re-run.
- Idempotent re-run: the key of a file is its name BRANCH-yyyy-MM-dd.csv plus its SHA-256 checksum. Same name and same checksum is skipped. Same name and a different checksum replaces the old rows of that file, never adds to them. Rows that fail validation are logged and do not stop the run.

## Alternatives considered
- Head office pulls from each branch: attractive because head office controls the timing and retries. We did not choose it because each branch would need an inbound SFTP or HTTPS server reachable from outside, which the branches' unreliable links and, we assume, small IT staff cannot run safely (QA-5, QA-3).
- POS publishes one event per receipt to a message broker: attractive because data arrives continuously and a broker buffers outages (QA-3). We did not choose it because it needs a change to all three POS systems and a broker that someone must operate, and it would turn the daily files into about 1 M messages a month (assuming roughly 3 rows per receipt, which the brief does not state) that each need their own handling and ordering (QA-1).

## Consequences
+ No change to the POS systems: they already export files, so release 1 can start now.
+ A file is a natural unit for checksums, re-runs and replacing data, so a late or repeated upload stays safe (QA-3).
+ Per-branch SFTP accounts and folders give a simple first layer of isolation between the branches' uploads; the managers' view of the reports is still enforced in the web page (QA-5, ADR-0001).
- Data is at most one day old; head office cannot see sales within the day.
- A branch with a failing link sends nothing, so head office sees a gap only at the cutoff; we need a monitor for missing files (QA-3).
- We must run and secure an SFTP server and manage three accounts and keys.
- Replacing rows of a changed file needs the engine to recompute the affected branch, which costs extra run time (QA-1).

Revisit when: head office needs intraday figures, a branch asks for a way to correct data after the cutoff, or more than one file per month arrives later than 24 h.

## Review
Reviewed by: Rasmey Rithysak, 2026-10-20
- Comment 1: the context still said "REP file 2 h late", but QA-3 now has the file arriving at 10:00 on day 2.
- Comment 2: "millions of messages" for per-receipt events was wrong: about 1 M receipts is about 1 M messages, which at 50 000 msg/s is only about 20 s, so the QA-1 argument was weak; the "3 rows per receipt" and "small IT staff" claims are assumptions.
- Comment 3: the policy did not say who detects a late file or how often, so the 10 min could not be checked; the "partial" mark should only go once no other day is missing; and "ingest job" no longer matches the C4 diagram.
Resolution: the context uses the 11 h case; the broker argument now rests on the POS change, the operated broker and the message count, with the assumptions labelled; the policy now has a 5 min check (assumption) and the "no other day missing" condition; the text says "the batch's ingest step".
