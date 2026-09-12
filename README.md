# Online Retail Data Analysis using Hadoop

Four MapReduce programs and five Apache Pig
scripts run over the UCI *Online Retail* transaction dataset on a single-node
Hadoop cluster.

## Dataset

| | |
|---|---|
| Name | Online Retail |
| Publisher | UCI Machine Learning Repository |
| Source | https://archive.ics.uci.edu/dataset/352/online+retail |
| Period | 1 December 2010 - 9 December 2011 |
| Records | **541,909 transactions** |
| Description | Transactions of a UK-based online gift retailer |

The publisher distributes the data as a single Excel workbook. Hadoop reads
plain text, so the workbook is converted to CSV and partitioned by invoice
month into 13 files. Splitting it this way makes HDFS produce 13 input splits,
so the jobs run 13 map tasks in parallel rather than one.

### Schema

| # | Column | Type | Notes |
|---|--------|------|-------|
| 0 | InvoiceNo | string | a leading `C` marks a cancelled order |
| 1 | StockCode | string | product identifier |
| 2 | Description | string | product name |
| 3 | Quantity | int | negative on returns |
| 4 | InvoiceDate | string | `yyyy-MM-dd HH:mm` |
| 5 | UnitPrice | double | price per unit, GBP |
| 6 | CustomerID | string | empty on 24.9% of rows |
| 7 | Country | string | 38 countries |

### Preparing the data

```
Quantity and UnitPrice are used as-is.
Description: commas replaced with spaces (4,796 rows) and quotes removed
             (1,239 rows), so every field splits cleanly on a comma.
Country:     contains no commas, left untouched.
```

Verified after conversion: all 541,909 rows split into exactly 8 fields, with
no quote characters remaining.

## MapReduce analyses

| Module | Analysis | Pattern |
|--------|----------|---------|
| `TotalSalesByProduct` | Revenue per product | filtering, summation, Combiner |
| `AverageInvoiceValue` | Line count, total and mean value per invoice, plus the dataset-wide average | custom `Writable`, Combiner, `cleanup()` |
| `TopCustomersBySpending` | Ten highest-spending customers | top-N held in `cleanup()` |
| `BinningByCountry` | One output file per country | `MultipleOutputs`, map-only |

Cancelled invoices are excluded by every mapper, which is where the filtering
pattern is applied.

## Pig analyses

| Script | Analysis |
|--------|----------|
| `top5_countries_by_revenue.pig` | Five countries with the highest sales |
| `top10_products_by_revenue.pig` | Ten best-earning products |
| `top10_products_by_country.pig` | Ten best-earning products per country |
| `top10_customers_by_spending.pig` | Ten highest-spending customers |
| `monthly_sales_trend.pig` | Revenue, order lines and invoices per month |

## Results

| Measure | Value |
|---------|-------|
| Distinct products sold | 3,922 |
| Invoices | 19,960 |
| Average invoice value | GBP 534.40 |
| Total revenue | GBP 10,666,684.54 |
| Highest-earning product | `DOT` DOTCOM POSTAGE, GBP 206,248.77 |
| Highest-spending customer | 14646 (Netherlands), GBP 280,206.02 |
| Largest market | United Kingdom, GBP 9,025,222.08 |

The MapReduce and Pig implementations were written independently and agree on
every shared figure.

## Building and running

Java 8 and Hadoop 3.2.4. Each module is compiled directly with `javac`:

```
javac -classpath C:\hadoop\share\hadoop\common\*;C:\hadoop\share\hadoop\mapreduce\*;C:\hadoop\share\hadoop\hdfs\*;C:\hadoop\share\hadoop\common\lib\* ^
      -d D:\Desktop\bdal_project\TotalSalesByProduct\target\classes ^
      D:\Desktop\bdal_project\TotalSalesByProduct\src\main\java\totalsalesbyproduct\*.java

jar -cvf D:\Desktop\bdal_project\TotalSalesByProduct\target\TotalSalesByProduct.jar ^
    -C D:\Desktop\bdal_project\TotalSalesByProduct\target\classes .

hadoop jar D:\Desktop\bdal_project\TotalSalesByProduct\target\TotalSalesByProduct.jar ^
       totalsalesbyproduct.DriverClass /BdalProject/data /BdalProject/output/totalsales
```



Pig runs in local mode:

```
pig -x local D:\Desktop\bdal_project\PigAnalysis\scripts\top5_countries_by_revenue.pig
```

## Repository layout

```
TotalSalesByProduct/     src/main/java/totalsalesbyproduct/
AverageInvoiceValue/     src/main/java/averageinvoicevalue/
TopCustomersBySpending/  src/main/java/topcustomersbyspending/
BinningByCountry/        src/main/java/binningbycountry/
PigAnalysis/scripts/     five .pig scripts
```


