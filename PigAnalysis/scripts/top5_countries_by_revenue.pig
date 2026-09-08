-- 1) Top 5 Countries by Revenue : returns the five countries generating the
--    highest total sales value.
retail = LOAD 'D:/Desktop/bdal_project/data/monthly' USING PigStorage(',') AS
   (invoiceno:chararray, stockcode:chararray, description:chararray, quantity:int,
    invoicedate:chararray, unitprice:double, customerid:chararray, country:chararray);
-- an InvoiceNo starting with C is a cancellation and would subtract from real sales
valid = FILTER retail BY country IS NOT NULL AND quantity > 0 AND unitprice > 0
                      AND NOT (invoiceno MATCHES 'C.*');
lines = FOREACH valid GENERATE country, (quantity * unitprice) AS revenue;
grp_country = GROUP lines BY country;
revenue_by_country = FOREACH grp_country GENERATE group AS country,
                     ROUND(SUM(lines.revenue) * 100) / 100.0 AS total_revenue;
sorted_desc = ORDER revenue_by_country BY total_revenue DESC;
top5_countries = LIMIT sorted_desc 5;
STORE top5_countries INTO 'D:/Desktop/bdal_project/PigAnalysis/output/Top5CountriesByRevenue' USING PigStorage('|');
