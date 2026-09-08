package totalsalesbyproduct;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Emits StockCode as key of type Text and its total revenue of type
 * DoubleWritable. Also usable as the Combiner, since summing is associative.
 */
public class ReducerClass extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

    private DoubleWritable total = new DoubleWritable();

    @Override
    public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {

        double sum = 0;
        for (DoubleWritable value : values) {
            sum += value.get();
        }
        total.set(Math.round(sum * 100.0) / 100.0);
        context.write(key, total);
    }
}
