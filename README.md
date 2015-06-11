Archiva
=======

This project contains scripts to periodically fetch personal data from a variety
of sources and convert it into a unified format.

This project will consist of a few separate pieces:
- schemas describing the standard format for various kinds of data (possibly prismatic/schema?)
- flat file sources for hand-curated data
- scripts for pulling data from external data sources
- a script to process file data and populate Vault

## Terminology

A _source_ is an entity which holds some original data. Typically, each website
or application will be its own source of data. For example, GMail, Github,
Withings, CardioTrainer, etc are all sources. Each source may have many
_topics_, which represent a specific kind of data. For example, scale readings
for a specific person would be a topic in Withings; run tracks in CardioTrainer
would be another.

Each source has some general configuration (e.g., access credentials) and a list
of registered topics. Each topic can have configuration as well, for example to
specify names, qualifiers, etc.

For each topic, the system keeps track of a set of time ranges indicating what
periods have been retrieved from the source for that topic. This is the topic's
_coverage_. By default, the system shouldn't re-fetch data for a topic unless
the user explicitly requests it.

When topics fetch data, it results in _raw records_. These are keyed by the
primary identifier in the source, which aids in further deduplication.

## Processing Steps

Data follows an _ingestion_ procedure, by which is integrated into the existing
data web.

### Collection

The first step is to collect raw data from a source. This is probably best left
to a manual process to begin with. Ultimately, the goal is obviously to automate
data collection with scripts. In many cases, collecting the data involves
interacting with or scraping web pages.

There are two primary usage styles for data collection: complete and
incremental. The first is just as it sounds, downloading all available data in
one job. This will generally only happen when initializing sources or
backfilling data. An incremental collection runs periodically, and attempts to
gather only "new" data to integrate.

### Conversion

Next, the data needs to be converted into a common format - this is probably an
EDN value or set of values that are candidates for insertion into Vault.  For
example, take this line from a Charles Schwab CSV file:

```csv
"10/08/2013","Rev Reinvest Shares","SCHZ","SCH US AGG BND ETF","1.0423","$50.7643","","-$52.91",
```

It becomes the following candidate posting values:

```clojure
{:vault/type :ledger/posting
 :ledger/account #vault/ref "sha256:blobref" ; Individual Investments
 :ledger/amount #finance/commodity [1.0423 SCHZ]
 :price #finance/commodity [50.7643 USD]
 :data/source #vault/ref "sha256:blobref"    ; csv line blob
 :time/at #inst "2013-10-08T00:00:00Z"}

{:vault/type :ledger/posting
 :ledger/account #vault/ref "sha256:blobref" ; Individual Investments
 :ledger/amount #finance/commodity [USD -52.91]
 :data/source #vault/ref "sha256:blobref"    ; csv line blob
 :time/at #inst "2013-10-08T00:00:00Z"}
```

And this candidate transaction value:

```clojure
{:vault/type :ledger/transaction
 :ledger/postings
 #{#vault/ref "algo:blobref"
   #vault/ref "algo:blobref"}
 :time/at #inst "2013-10-08T00:00:00Z"}

; entity
{:title "Dividend Reinvestment"
 :description nil
 :state nil}
```

### Merging

Next search for matching posting data already stored in the system. This uses
some heuristics to determine whether a given posting matches:
- Search postings with dates within X of the candidate time.
- Match the account
- Match the amount
- If still ambiguities, match the source-desc and source-type fields

If the match is close enough, look up the transaction object(s) containing the
matched posting. If the match is not exact, then there are potentially updates
in the candidate data which can be applied. In this case, prepare a "proposed
update" to the transaction to use the merged posting data.

For example, PayPal occasionally posts two lines for a transaction, one when it
is charged and another when it clears. This should only form one posting with a
'cleared-at' time.

If no existing data matches, insert a new posting blob. New postings form a pool
of 'unmatched' postings. These are candidates for inclusion into new
transactions. Some heuristics:
- if the source data is known to be identical (e.g. multiple postings generated
  from the same line of CSV)
- timestamp is identical (e.g. paypal instant transfers)
- amounts balance exactly (transfers between accounts)

## License

Copyright © 2013 Gregory Look
