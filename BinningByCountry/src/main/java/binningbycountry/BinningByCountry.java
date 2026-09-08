package binningbycountry;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * Binning by Country : splits the transactions into one output file per
 * country using MultipleOutputs. This is a map-only job - no aggregation is
 * required, only routing - so the reduce phase is switched off entirely.
 */
public class BinningByCountry {

    public static class BinMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

        private MultipleOutputs<Text, NullWritable> mos = null;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            mos = new MultipleOutputs<Text, NullWritable>(context);
        }

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String[] col = value.toString().split(",");
            if (col.length < 8) {
                return;
            }
            String country = col[7].trim();
            if (country.isEmpty()) {
                country = "Unspecified";
            }
            // The bin name becomes part of a file name, so anything that is not a
            // letter or digit is folded to an underscore.
            String bin = country.replaceAll("[^A-Za-z0-9]", "_");
            mos.write("bins", value, NullWritable.get(), bin);
            context.getCounter("Country", country).increment(1);
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            mos.close();
        }
    }

    public static void main(String[] args)
            throws IOException, InterruptedException, ClassNotFoundException {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Binning By Country");
        job.setJarByClass(BinningByCountry.class);
        job.setMapperClass(BinMapper.class);
        job.setNumReduceTasks(0);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        MultipleOutputs.addNamedOutput(job, "bins", TextOutputFormat.class,
                Text.class, NullWritable.class);
        MultipleOutputs.setCountersEnabled(job, true);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
