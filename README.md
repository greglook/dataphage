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
_coverage_. The system shouldn't re-fetch data for a period unless the user
explicitly requests it.

```
/archiva/
  sources/
    withings/
      weight/
        2015/...
      <topic>/
        ...
    <source>/...
```

## Processing Steps

Data follows an _ingestion_ procedure, by which it is integrated into the
existing data web.

### Collection

When the system fetches data, the results are _raw records_. These are keyed by
the _source's_ primary identifier, which aids in deduplication. The raw results
are stored as blocks and linked from a job-level entry.

There are two primary usage styles for data collection: complete and
incremental. The first is just as it sounds, downloading all available data in
one job. This will generally only happen when initializing sources or
backfilling data. An incremental collection runs periodically, and attempts to
gather only "new" data to integrate.

### Conversion

Next, the data needs to be converted into a common format - this is probably EDN
or set of values that are candidates for insertion into Vault. For example, take
this line from a Schwab CSV file:

```csv
"10/08/2013","Rev Reinvest Shares","SCHZ","SCH US AGG BND ETF","1.0423","$50.7643","","-$52.91",
```

The entire CSV (or perhaps a subsection of it, e.g. by month) is stored as a
block. Each line is then translated using the CSV headers into records like the
following:

```clojure
{:data/sources #{#data/link "source-csv-file"}
 :date "10/08/2013"
 :description "Rev Reinvest Shares"
 :commodity "SCHZ"
 :title "SCH US AGG BND ETF"
 :amount 1.0423M
 :price 50.7643M
 :fees nil
 :total -52.91M}
```

Finally, this is processed into the following candidate values:

```clojure
{:data/type :finance/posting
 :data/sources #{#data/link "source-map"}
 :finance.posting/date #time/date "2013-10-08"
 :finance.posting/account #data/link "individual-investments"
 :finance.posting/amount #finance/$ [1.0423M SCHZ]
 :finance.posting/price #finance/$ [50.7643M USD]
 :finance.posting/weight #finance/$ [52.91M USD]}

{:data/type :finance/posting
 :data/sources #{#data/link "source-map"}
 :finance.posting/date #time/date "2013-10-08"
 :finance.posting/account #data/link "individual-investments"
 :finance.posting/amount #finance/$ [-52.91M USD]}

{:data/type :finance/transaction
 :time/date #time/date "2013-10-08"
 :title "SCH US AGG BND ETF"
 :description "Rev Reinvest Shares"
 :finance.transaction/id
 :finance.transaction/status :finance.transaction.status/uncleared
 :finance.transaction/entries [#data/link "posting-01"
                               #data/link "posting-02"]}
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
