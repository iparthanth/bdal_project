package averageinvoicevalue;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Emits InvoiceNo of type Text and a Tuple of custom type InvoiceValueTuple
 * holding one order line and its value.
 */
public class MapperClass extends Mapper<LongWritable, Text, Text, InvoiceValueTuple> {

    private Text invoiceNo = new Text();
    private InvoiceValueTuple outTuple = new InvoiceValueTuple();

    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String[] col = value.toString().split(",");
        if (col.length < 8) {
            return;
        }
        if (col[0].startsWith("C") || col[0].startsWith("c")) {
            context.getCounter("Retail", "CancelledRowsSkipped").increment(1);
            return;
        }

        try {
            double qty = Double.parseDouble(col[3]);
            double price = Double.parseDouble(col[5]);
            if (qty <= 0 || price <= 0) {
                return;
            }
            invoiceNo.set(col[0]);
            outTuple.setLineCount(1);
            outTuple.setTotalAmount(qty * price);
            context.write(invoiceNo, outTuple);
        } catch (NumberFormatException e) {
            context.getCounter("Retail", "UnparseableRowsSkipped").increment(1);
        }
    }
}
