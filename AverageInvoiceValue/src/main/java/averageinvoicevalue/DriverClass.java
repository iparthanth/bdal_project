package averageinvoicevalue;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Average Invoice Value : returns the line count, total value and mean line
 * value of every invoice, and the average invoice value across the whole
 * dataset. Sets up configuration for the Mapper, Combiner, Reducer and Tuple
 * class.
 */
public class DriverClass {

    public static void main(String[] args)
            throws IOException, InterruptedException, ClassNotFoundException {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Average Invoice Value");
        job.setJarByClass(DriverClass.class);

        job.setMapperClass(MapperClass.class);
        job.setCombinerClass(CombinerClass.class);
        job.setReducerClass(ReducerClass.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(InvoiceValueTuple.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(InvoiceValueTuple.class);

        // A single reducer, so the dataset-wide average written from cleanup()
        // is computed once over every invoice rather than once per partition.
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
