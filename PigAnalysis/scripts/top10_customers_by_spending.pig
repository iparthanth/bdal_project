-- 4) Top 10 Customers by Spending : returns the ten customers who spent most.
--    Rows without a CustomerID are anonymous till sales and cannot be attributed.
retail = LOAD 'D:/Desktop/bdal_project/data/monthly' USING PigStorage(',') AS
   (invoiceno:chararray, stockcode:chararray, description:chararray, quantity:int,
    invoicedate:chararray, unitprice:double, customerid:chararray, country:chararray);
valid = FILTER retail BY customerid IS NOT NULL AND customerid != ''
                      AND quantity > 0 AND unitprice > 0
                      AND NOT (invoiceno MATCHES 'C.*');
lines = FOREACH valid GENERATE customerid, country, (quantity * unitprice) AS revenue;
grp_customer = GROUP lines BY customerid;
spend_by_customer = FOREACH grp_customer GENERATE group AS customerid,
                    MAX(lines.country) AS country,
                    ROUND(SUM(lines.revenue) * 100) / 100.0 AS total_spend;
sorted_desc = ORDER spend_by_customer BY total_spend DESC;
top10_customers = LIMIT sorted_desc 10;
STORE top10_customers INTO 'D:/Desktop/bdal_project/PigAnalysis/output/Top10CustomersBySpending' USING PigStorage('|');
