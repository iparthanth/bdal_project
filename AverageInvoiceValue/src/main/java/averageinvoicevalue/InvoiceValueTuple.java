package averageinvoicevalue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * Tuple Class : POJO carrying the number of order lines and their summed value.
 * Both parts must travel together, otherwise the Combiner cannot merge partial
 * results without distorting the average.
 */
public class InvoiceValueTuple implements Writable {

    private int lineCount = 0;
    private double totalAmount = 0;

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void write(DataOutput d) throws IOException {
        d.writeInt(lineCount);
        d.writeDouble(totalAmount);
    }

    public void readFields(DataInput di) throws IOException {
        lineCount = di.readInt();
        totalAmount = di.readDouble();
    }

    @Override
    public String toString() {
        double avg = (lineCount == 0) ? 0 : totalAmount / lineCount;
        return lineCount + "\t"
                + (Math.round(totalAmount * 100.0) / 100.0) + "\t"
                + (Math.round(avg * 100.0) / 100.0);
    }
}
