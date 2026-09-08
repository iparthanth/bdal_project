package topcustomersbyspending;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Top 10 Customers by Spending : returns the ten customers with the highest
 * total spend, in descending order.
 *
 * 135,080 of the 541,909 rows carry no CustomerID; those are anonymous
 * till transactions and are excluded, since they cannot be attributed.
 */
public class TopCustomersBySpending {

    public static final int TOP_N = 10;

    public static class Map1 extends Mapper<LongWritable, Text, Text, DoubleWritable> {

        private Text customer = new Text();
        private DoubleWritable spend = new DoubleWritable();

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
            if (col[6] == null || col[6].trim().isEmpty()) {
                context.getCounter("Retail", "NoCustomerIdSkipped").increment(1);
                return;
            }

            try {
                double qty = Double.parseDouble(col[3]);
                double price = Double.parseDouble(col[5]);
                if (qty <= 0 || price <= 0) {
                    return;
                }
                customer.set(col[6].trim());
                spend.set(qty * price);
                context.write(customer, spend);
            } catch (NumberFormatException e) {
                context.getCounter("Retail", "UnparseableRowsSkipped").increment(1);
            }
        }
    }

    public static class Reduce1 extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        // Ordered by spend; only the top N are ever retained, so memory stays bounded
        // no matter how many customers the dataset holds.
        private TreeMap<Double, String> ranked = new TreeMap<Double, String>();

        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException {

            double sum = 0;
            for (DoubleWritable value : values) {
                sum += value.get();
            }
            ranked.put(sum, key.toString());
            if (ranked.size() > TOP_N) {
                ranked.remove(ranked.firstKey());
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for (Map.Entry<Double, String> e : ranked.descendingMap().entrySet()) {
                context.write(new Text(e.getValue()),
                        new DoubleWritable(Math.round(e.getKey() * 100.0) / 100.0));
            }
        }
    }

    public static void main(String[] args)
            throws IOException, InterruptedException, ClassNotFoundException {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Top 10 Customers By Spending");
        job.setJarByClass(TopCustomersBySpending.class);

        job.setMapperClass(Map1.class);
        job.setReducerClass(Reduce1.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        // One reducer, so a single ranking is produced rather than one per partition.
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
