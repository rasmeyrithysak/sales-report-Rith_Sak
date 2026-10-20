# Lab-01: Introduction to Software Engineering Concepts

Angkor Mart monthly sales report. Team repository: `sales-report-<team>`, branch `lab-01`.

This lab designs the report system from its qualities first (quality-attribute scenarios), then its context and
containers (C4), then writes the first code: the canonical model, a validated CSV parser, pluggable renderers
(Strategy) and progress events (Observer).

## How to build and run

```bash
mvn -q verify                       # builds and runs all tests; prints nothing on success
java tools/SampleData.java          # writes 15 files, 3 000 rows to data/2026-09 (data/ is git-ignored)
mvn -q compile exec:java            # loads the month and prints the progress lines
```

Expected console result on the sample data: `Loaded 2998 rows, rejected 2`, with the rejected rows
`BTB-2026-09-04.csv:21 bad payment_method 'PAYPAL'` and `REP-2026-09-02.csv:11 bad quantity 'two'`.

## Contents of this folder

| File | What it is |
|---|---|
| `quality-scenarios.md` | Part 1: stakeholders, six quality-attribute scenarios (QA-1 to QA-6), ranks, assumptions, utility tree, architectural drivers |
| `c4-context.mmd`, `c4-context.png` | Part 2: C4 level 1 (system context) |
| `c4-container.mmd`, `c4-container.png` | Part 2: C4 level 2 (containers of release 1) |
| `c4-notes.md` | Part 2: notes under each diagram (what it leaves out, two containers linked to QA ids, one open question) |
| `design-review.md` | Part 4: package responsibilities, Ca, Ce and instability, sealed versus open renderers |

## Record what changed

### Part 1: quality-attribute scenarios
- Wrote six measurable scenarios, one per attribute: performance (QA-1), scalability (QA-2), availability (QA-3),
  modifiability (QA-4), security (QA-5) and testability (QA-6). Every response measure has a number, a unit and a
  way to measure it.
- Architectural drivers are QA-1 (H,H), QA-3 (H,M) and QA-5 (H,M).
- QA-3 was changed so that the REP file arrives at 10:00 on day 2, after the 06:00 report start. The first version
  had the file arrive before the batch started, so nothing had to be handled.
- The numeric targets are our own proposals (assumption A8) and are to be confirmed with the instructor.

### Part 2: C4 context and container diagrams
- Context: three people (analyst, branch manager, finance director) and three external systems (branch POS
  systems, e-mail service, identity provider). The finance director is a separate person box so that the
  recipient of the e-mail is visible.
- Container diagram for release 1 has six containers: landing zone, report batch (ingest, engine and renderers in
  one JVM, see ADR-0001), report store, audit log, desktop viewer and web report page, each with its technology. Every arrow names its mechanism (`[SFTP]`, `[file system]`,
  `[in-process call]`, `[SMTP]`, `[HTTPS]`). No classes or packages appear.
- Containers are linked to quality scenarios in `c4-notes.md` (QA-1, QA-3 and QA-5).

### Part 3: Maven project, model and CSV parser
- Maven project `edu.itc:sales-report` at the repository root, Java 25, JUnit 6.1.3.
- `model`: `PaymentMethod`, `SaleTransaction`, `ProductTotal`, `BranchSummary`, `MonthlyReport`. Money is
  `BigDecimal`, scale 2, `HALF_UP`, never `double` or `float`. `SaleTransaction` rejects nulls, quantity <= 0,
  negative amounts and a discount above quantity x unit price. `BranchSummary` and `MonthlyReport` keep
  unmodifiable copies of their collections.
- `ingest`: `TransactionParser`, the checked `InvalidRowException` and `CsvTransactionParser`, which splits with
  `split(",", -1)`, requires 10 fields, checks branch and category, and rejects invalid rows with a message that
  names the field.
- Tests: the example row (revenue 13.00), a row with a discount, ten kinds of invalid rows, the rounding case
  3 x 0.335 = 1.005 -> 1.01, and an unmodifiable `topProducts()`.

### Part 4: Strategy, Observer and design review
- Strategy (QA-4): sealed `ReportRenderer` with `TextReportRenderer`, `HtmlReportRenderer` (escapes `&`, `<`, `>`)
  and `CsvReportRenderer`, chosen by `forExtension`. An unknown extension such as `pdf` throws. PDF and XLSX
  follow in Lab-08.
- Observer: `MonthLoader` publishes one `FileLoaded` event per file and one `LoadFinished` event; `subscribe`
  returns a handle that unsubscribes the listener. `ConsoleProgress` is the first observer and `App` is the
  composition root.
- `MonthLoaderTest` checks the three events in order with the right counts, and that an unsubscribed listener
  receives nothing. `ReportRendererTest` checks every format, the exact CSV line
  `2026-09,REP,800.50,0.00,50,16.01`, and an exhaustive `switch` without `default`.
- `DependencyRulesTest` makes the build fail if `model` imports another project package or `ingest` imports
  `render`.
- Console run on the sample data: 2 998 rows accepted, 2 rejected, matching the lab's expected output.
- `design-review.md` gives Ca, Ce and I with the arithmetic for each package and argues for the sealed renderer
  hierarchy for QA-4.

## Who did what

Fill in before submitting. Only list what each member really did; `git log --format='%an | %s'` shows the history.

| Member | Work |
|---|---|
| Ouk Puthirith | Part 2: C4 context and container diagrams and notes. Part 3: Maven project, model records, CSV parser and tests. Part 4: Strategy renderers, Observer `MonthLoader`, dependency rules test and design review. |
| Rasmey Rithysak | Part 1: quality-attribute scenarios and utility tree. Part 5: ADR-0001 and ADR-0002 and the Part 5 section of the README. |

## Check before submitting

- [ ] `mvn -q verify` prints nothing and exits with 0, also on a fresh clone of branch `lab-01`
- [ ] `java tools/SampleData.java` then `mvn -q compile exec:java` prints 2 998 accepted and 2 rejected
- [ ] both PNG diagrams are committed next to their `.mmd` sources
- [ ] `git log --format='%an | %s' -- src/` shows both members
