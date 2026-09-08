package averageinvoicevalue;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Combiner Class : merges the order lines of one invoice on the map side.
 * Line count and amount are both carried forward so the reducer still sees a
 * faithful partial sum.
 */
public class CombinerClass extends Reducer<Text, InvoiceValueTuple, Text, InvoiceValueTuple> {

    private InvoiceValueTuple result = new InvoiceValueTuple();

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
    }
}
