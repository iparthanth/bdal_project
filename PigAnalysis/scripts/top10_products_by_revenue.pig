-- 2) Top 10 Products by Revenue : returns the ten products earning the most.
retail = LOAD 'D:/Desktop/bdal_project/data/monthly' USING PigStorage(',') AS
   (invoiceno:chararray, stockcode:chararray, description:chararray, quantity:int,
    invoicedate:chararray, unitprice:double, customerid:chararray, country:chararray);
valid = FILTER retail BY quantity > 0 AND unitprice > 0
                      AND NOT (invoiceno MATCHES 'C.*');
lines = FOREACH valid GENERATE stockcode, description, (quantity * unitprice) AS revenue;
grp_product = GROUP lines BY stockcode;
revenue_by_product = FOREACH grp_product GENERATE group AS stockcode,
                     MAX(lines.description) AS description,
                     ROUND(SUM(lines.revenue) * 100) / 100.0 AS total_revenue;
sorted_desc = ORDER revenue_by_product BY total_revenue DESC;
top10_products = LIMIT sorted_desc 10;
STORE top10_products INTO 'D:/Desktop/bdal_project/PigAnalysis/output/Top10ProductsByRevenue' USING PigStorage('|');
