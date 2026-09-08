package totalsalesbyproduct;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Emits StockCode of type Text and the line revenue (Quantity * UnitPrice) of
 * type DoubleWritable.
 *
 * Dataset columns: 0 InvoiceNo, 1 StockCode, 2 Description, 3 Quantity,
 * 4 InvoiceDate, 5 UnitPrice, 6 CustomerID, 7 Country.
 */
public class MapperClass extends Mapper<LongWritable, Text, Text, DoubleWritable> {

    private Text stockCode = new Text();
    private DoubleWritable revenue = new DoubleWritable();

    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String[] col = value.toString().split(",");
        if (col.length < 8) {
            return;
        }

        // An InvoiceNo beginning with C marks a cancelled order; those rows carry
        // negative quantities and would subtract from genuine sales.
        if (col[0].startsWith("C") || col[0].startsWith("c")) {
            context.getCounter("Retail", "CancelledRowsSkipped").increment(1);
            return;
        }

        try {
            double qty = Double.parseDouble(col[3]);
            double price = Double.parseDouble(col[5]);
            if (qty <= 0 || price <= 0) {
                context.getCounter("Retail", "NonPositiveRowsSkipped").increment(1);
                return;
            }
            stockCode.set(col[1]);
            revenue.set(qty * price);
            context.write(stockCode, revenue);
        } catch (NumberFormatException e) {
            context.getCounter("Retail", "UnparseableRowsSkipped").increment(1);
        }
    }
}
