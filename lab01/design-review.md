# Design review: Lab-01 packages

Counting rule (from the `import edu.itc.salesreport...` lines, other packages only):
**Ce** = distinct classes outside the package that its classes import;
**Ca** = distinct classes outside the package that import one of its classes;
**I = Ce / (Ca + Ce)**.

## Responsibility (cohesion) and coupling

| Package | Responsibility in one sentence | Ce | Ca | I |
|---|---|---|---|---|
| `model` | Holds the canonical immutable records and their money and invariant rules, and nothing else. | 0 | 7 | 0 / (7 + 0) = **0.00** |
| `ingest` | Turns POS lines and month folders into validated `SaleTransaction`s and announces load progress to observers. | 2 | 1 | 2 / (1 + 2) = **0.67** |
| `render` | Turns a `MonthlyReport` into bytes of one output format (Strategy). | 2 | 0 | 2 / (0 + 2) = **1.00** |
| root (`App`) | Composition root: creates the objects and wires them together. | 3 | 0 | 3 / (0 + 3) = **1.00** |

Details: `ingest` depends on `PaymentMethod` and `SaleTransaction` and is used only by `App`.
`render` depends on `BranchSummary` and `MonthlyReport` and nothing uses it yet.
`model` is used by 7 classes in `ingest` and `render`, and imports no project package.

## One coupling I accept

`ingest -> model`. The parser must create `SaleTransaction` objects, so it cannot avoid knowing them.
The arrow points from a less stable package (I = 0.67) to the most stable one (I = 0.00), which follows the
stable-dependencies principle, and `model` can change only for business reasons. `DependencyRulesTest` keeps
the opposite direction (`model -> ingest`) and `ingest -> render` out of the build.

## Sealed renderer hierarchy or open interface for QA-4?

QA-4 asks for a new export format in at most 2 person-days and at most 2 existing files changed.
With the **sealed** `ReportRenderer`, adding JSON means one new class, one edit in `ReportRenderer.java`
(the `permits` clause and the `forExtension` switch are in the same file), and the exhaustive `switch` in
`ReportRendererTest` stops compiling until the new case is added: that is at most 2 changed files, and the
compiler finds every place that forgot the new format. An **open** interface needs one new class and one factory
line, but nothing warns about a switch that misses the new class, and anyone could add renderers we do not control.
I choose the sealed hierarchy: the formats are a small closed set owned by head office (txt, html, csv now;
PDF and XLSX in Lab-08), and the compile-time check protects QA-4 better than extra openness. If outside plug-in
formats ever become a requirement, the interface should be opened and the `permits` clause dropped.