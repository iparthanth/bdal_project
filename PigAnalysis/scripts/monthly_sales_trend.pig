-- 5) Monthly Sales Trend : returns total revenue, order-line count and number
--    of distinct invoices for each month of trading.
retail = LOAD 'D:/Desktop/bdal_project/data/monthly' USING PigStorage(',') AS
   (invoiceno:chararray, stockcode:chararray, description:chararray, quantity:int,
    invoicedate:chararray, unitprice:double, customerid:chararray, country:chararray);
valid = FILTER retail BY quantity > 0 AND unitprice > 0
                      AND NOT (invoiceno MATCHES 'C.*');
-- InvoiceDate is 'YYYY-MM-DD HH:MM', so the first seven characters are the month
lines = FOREACH valid GENERATE SUBSTRING(invoicedate, 0, 7) AS month,
                               invoiceno,
                               (quantity * unitprice) AS revenue;
grp_month = GROUP lines BY month;
monthly = FOREACH grp_month {
              invoices = DISTINCT lines.invoiceno;
              GENERATE group AS month,
                       ROUND(SUM(lines.revenue) * 100) / 100.0 AS total_revenue,
                       COUNT(lines) AS order_lines,
                       COUNT(invoices) AS invoice_count;
          };
monthly_sorted = ORDER monthly BY month ASC;
STORE monthly_sorted INTO 'D:/Desktop/bdal_project/PigAnalysis/output/MonthlySalesTrend' USING PigStorage('|');
