# Test Import Files

All files are in ING CSV format (semicolon-delimited, Windows-1252 encoding).

---

## girokonto_januar_2024.csv

**Account:** DE49500105175425834662 · Girokonto · ING-DiBa AG

| # | Date       | Counterparty          | Purpose                    | Amount      |
|---|------------|-----------------------|----------------------------|-------------|
| 1 | 02.01.2024 | Musterfirma GmbH      | Gehalt Januar 2024         | +3.500,00 € |
| 2 | 03.01.2024 | Thomas Bergmann       | Miete Januar 2024          | −1.100,00 € |
| 3 | 05.01.2024 | REWE Markt            | *(empty)*                  | −52,37 €    |
| 4 | 06.01.2024 | Netflix International | Streaming Abo Januar       | −12,99 €    |
| 5 | 08.01.2024 | Amazon Payments       | Bestellung 305-…           | −64,00 €    |
| 6 | 10.01.2024 | Stadtwerke            | Abschlag Strom und Gas     | −120,00 €   |
| 7 | 06.01.2024 | Netflix International | Streaming Abo Januar       | −12,99 € ⚠️ duplicate of #4 |
| 8 | 12.01.2024 | Sparkasse Transfer    | Umbuchung vom Sparkonto    | +500,00 €   |
| 9 | 14.01.2024 | Ristorante da Marco   | *(empty)*                  | −61,20 €    |
|10 | 15.01.2024 | Spotify AB            | Premium Abo Januar         | −8,99 €     |
|11 | 18.01.2024 | FitnessFirst GmbH     | Mitgliedsbeitrag Januar    | −31,00 €    |
|12 | 31.01.2024 | Musterfirma GmbH      | Praemie Q4 2023            | −1,62 €     |

**Expected result:** `imported: 11, skipped: 1`

**What is tested:**
- Normal income and expense transactions
- Transactions with empty `Verwendungszweck` (stored as `null`)
- Within-file duplicate detection: row #7 is identical to row #4 — first one is saved,
  second is skipped because the first was already persisted before the check runs

---

## tagesgeld_jan_feb_2024.csv

**Account:** DE82200505501234567890 · Tagesgeldkonto · ING-DiBa AG  
*(different IBAN → creates a second account)*

| # | Date       | Content                                    | Type       |
|---|------------|--------------------------------------------|------------|
| 1 | 01.01.2024 | ING Zinsgutschrift 2023                    | valid      |
| 2 | 05.01.2024 | Umbuchung vom Girokonto Januar             | valid      |
| 3 | —          | `FEHLERHAFTE ZEILE OHNE GENUG SPALTEN`     | **malformed** — fewer than 9 columns, silently skipped |
| 4 | 01.02.2024 | ING Zinsgutschrift Januar                  | valid      |
| 5 | 05.02.2024 | Umbuchung — empty `Betrag` field           | **malformed** — `parseDecimal("")` throws, silently skipped |
| 6 | 15.02.2024 | Wrong date format (`FALSCHES DATUM FORMAT`)| **malformed** — `LocalDate.parse` throws, silently skipped |
| 7 | 05.02.2024 | Umbuchung vom Girokonto Februar            | valid      |
| 8 | 05.01.2024 | Umbuchung vom Girokonto Januar             | ⚠️ duplicate of #2 |
| 9 | 29.02.2024 | Betrag = `KEIN_BETRAG` (non-numeric)       | **malformed** — `parseDecimal` throws, silently skipped |
|10 | 28.02.2024 | Einzahlung Gehaltsruecklage                | valid      |

**Expected result:** `imported: 5, skipped: 1` (4 malformed rows are silently ignored — not counted)

**What is tested:**
- Second account auto-created on first import
- Four different kinds of malformed data rows — all silently skipped by the parser
- Within-file duplicate detection

---

## header_fehlerhaft.csv

**Header issues:**
- `IBAN` key replaced by `Kontonummer` → not recognized → `iban = null`
- `Kontoname` line missing entirely → `accountName = null`
- `Zeitraum` value is `"Januar 2024"` instead of `"DD.MM.YYYY - DD.MM.YYYY"` →
  `split(" - ")` produces only one part → condition `period.length == 2` is false →
  `periodFrom` / `periodTo` remain `null` (no exception)
- `Bank` and `Saldo` are present and parsed correctly

**Account behaviour:** `findByIban(null)` returns empty → a new account is created
with `iban = null`, `name = null`, `bankName = "ING-DiBa AG"`.

| # | Date       | Counterparty     | Purpose            | Amount      |
|---|------------|------------------|--------------------|-------------|
| 1 | 10.01.2024 | Musterfirma GmbH | Gehalt Januar 2024 | +3.500,00 € |
| 2 | 15.01.2024 | Thomas Bergmann  | Miete Januar 2024  | −1.100,00 € |
| 3 | 20.01.2024 | REWE Markt       | *(empty)*          | −50,00 €    |

**Expected result:** `imported: 3, skipped: 0`

**What is tested:**
- Parser resilience when header keys are wrong or missing
- `Zeitraum` with unparseable format does not throw — the `if (period.length == 2)` guard handles it
- Transactions are still imported despite broken metadata
