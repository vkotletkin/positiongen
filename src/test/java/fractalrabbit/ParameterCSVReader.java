package fractalrabbit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Used only for development
 *
 * @author rwrd
 *
 */
public class ParameterCSVReader {

    /**
     * @param args path of the parameter CSV
     */
    static void main(String[] args) {
        try {
            Reader in = new FileReader(args[0]);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
            for (CSVRecord csvRecord : records) {
                System.out.println(csvRecord.get(0) + " has value " + csvRecord.get(1));
            }
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println("Could not find file.");
        }

    }

}
