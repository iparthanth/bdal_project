-- 3) Top 10 Products by Country : returns, for every country, its ten
--    best-selling products by revenue. Uses a nested FOREACH so the ordering
--    and limiting happen inside each country's group.
retail = LOAD 'D:/Desktop/bdal_project/data/monthly' USING PigStorage(',') AS
   (invoiceno:chararray, stockcode:chararray, description:chararray, quantity:int,
    invoicedate:chararray, unitprice:double, customerid:chararray, country:chararray);
valid = FILTER retail BY country IS NOT NULL AND quantity > 0 AND unitprice > 0
                      AND NOT (invoiceno MATCHES 'C.*');
lines = FOREACH valid GENERATE country, stockcode, (quantity * unitprice) AS revenue;
grp_cp = GROUP lines BY (country, stockcode);
product_totals = FOREACH grp_cp GENERATE group.country AS country,
                 group.stockcode AS stockcode,
                 ROUND(SUM(lines.revenue) * 100) / 100.0 AS total_revenue;
grp_country = GROUP product_totals BY country;
top10_per_country = FOREACH grp_country {
                        sorted = ORDER product_totals BY total_revenue DESC;
                        top10  = LIMIT sorted 10;
                        GENERATE FLATTEN(top10);
                    };
STORE top10_per_country INTO 'D:/Desktop/bdal_project/PigAnalysis/output/Top10ProductsByCountry' USING PigStorage('|');
