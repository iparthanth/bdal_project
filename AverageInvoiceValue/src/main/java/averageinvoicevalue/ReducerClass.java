package averageinvoicevalue;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Emits InvoiceNo as key of type Text and a Tuple of custom type
 * InvoiceValueTuple giving the line count, invoice total and mean line value.
 *
 * The overall average invoice value cannot be produced per key, so the running
 * totals are held across reduce() calls and written once from cleanup().
 */
public class ReducerClass extends Reducer<Text, InvoiceValueTuple, Text, InvoiceValueTuple> {

    private InvoiceValueTuple result = new InvoiceValueTuple();
    private int invoiceCount = 0;
    private double grandTotal = 0;

    @Override
    protected void reduce(Text key, Iterable<InvoiceValueTuple> values, Context context)
            throws IOException, InterruptedException {

        int lines = 0;
        double amount = 0;
        for (InvoiceValueTuple val : values) {
            lines += val.getLineCount();
            amount += val.getTotalAmount();
        }
        result.setLineCount(lines);
        result.setTotalAmount(amount);
        context.write(key, result);

        invoiceCount++;
        grandTotal += amount;
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        if (invoiceCount == 0) {
            return;
        }
        InvoiceValueTuple overall = new InvoiceValueTuple();
        overall.setLineCount(invoiceCount);
        overall.setTotalAmount(grandTotal);
        context.write(new Text("ZZZ_AVERAGE_INVOICE_VALUE"), overall);
    }
}
