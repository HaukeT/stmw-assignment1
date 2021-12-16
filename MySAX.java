/* Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * Instructions:
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 */

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;


public class MySAX extends DefaultHandler {

    public static void main(String args[])
            throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        MySAX handler = new MySAX();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        // Parse each file provided on the
        // command line.
        for (int i = 0; i < args.length; i++) {
            FileReader r = new FileReader(args[i]);
            xr.parse(new InputSource(r));
        }
    }


    public MySAX() {
        super();
    }

    /* Returns the amount (in XXXXX.xx format) denoted by a money-string
     * like $3,453.23. Returns the input if the input is an empty string.
     */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double am = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try {
                am = nf.parse(money).doubleValue();
            } catch (ParseException e) {
                System.out.println("This method should work for all " +
                        "money values you find in our data.");
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(am).substring(1);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////

    private String Characters = "";
    private HashMap<String, Integer> categoryTable = new HashMap<>();
    private HashMap<ArrayList<String>, Integer> locationTable = new HashMap<>();
    private ArrayList<String> currentLocation = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> itemTable = new HashMap<>();
    private int currentItemID;
    private String currentItemName;

    private ArrayList<String> currentAtts = new ArrayList<>();

    public void startDocument() {
        System.out.println("Start document");
    }


    public void endDocument() {

        writeCategoriesToCSV("categories.csv");
        writeLocationToCSV("location.csv");
        System.out.println("End document");
    }

    public void startElement(String uri, String name,
                             String qName, Attributes atts) {
        /*
        if ("".equals(uri))
            System.out.println("Start element: " + qName);
        else
            System.out.println("Start element: {" + uri + "}" + name);
        for (int i = 0; i < atts.getLength(); i++) {
            System.out.println("Attribute: " + atts.getLocalName(i) + "=" + atts.getValue(i));
        }
         */
        for (int i = 0; i < atts.getLength(); i++) {
            currentAtts.add(atts.getValue(i));
        }
        Characters = "";
    }


    public void endElement(String uri, String name, String qName) {
        /*
        if ("".equals(uri))
            System.out.println("End element: " + qName);
        else
            System.out.println("End element:   {" + uri + "}" + name);
         */

        switch (qName) {
            case "Category":
                if (!categoryTable.containsKey(Characters)) {
                    categoryTable.put(Characters, categoryTable.size() + 1);
                }
                break;
            case "Location":

                currentLocation.add(Characters);
                if (!currentAtts.isEmpty()) {
                    currentLocation.addAll(currentAtts);
                }
                else {
                    currentLocation.addAll(Arrays.asList("", ""));
                }
                break;
            case "Country":
                currentLocation.add(Characters);
                if (!locationTable.containsKey(currentLocation)) {
                    ArrayList<String> tempArrayList = new ArrayList<>(currentLocation);
                    locationTable.put(tempArrayList, locationTable.size() + 1);
                }
                currentLocation.clear();
                break;
            case "Item":
                currentItemID = Integer.parseInt(currentAtts.get(0));
            case "Name":
                if (!itemTable.containsKey(currentItemID)) {
                    ArrayList<String> tempArrayList = new ArrayList<>();
                }

        }
        currentAtts.clear();
        Characters = "";
    }


    public void characters(char ch[], int start, int length) {
        //System.out.print("Characters:    \"");

        //TODO performance don't change variables when not necessary
        String normalString = new String(ch, start, length);
        Characters += normalString;
        /*
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
                case '\\':
                    System.out.print("\\\\");
                    break;
                case '"':
                    System.out.print("\\\"");
                    break;
                case '\n':
                    System.out.print("\\n");
                    break;
                case '\r':
                    System.out.print("\\r");
                    break;
                case '\t':
                    System.out.print("\\t");
                    break;
                default:
                    System.out.print(ch[i]);
                    break;
            }
        }
        System.out.print("\"\n");
         */
    }

    public void writeCategoriesToCSV(String fileName) {
        try {
            PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8);
            categoryTable.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach((e) -> ps.println(e.getValue() + "," + CSV.escape(e.getKey())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(categoryTable.size());
    }

    public void writeLocationToCSV(String fileName) {
        try {
            PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8);
            locationTable.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach((e) -> {
                        ps.print(e.getValue());
                        for (String s : e.getKey()) {
                            ps.print("," + s);
                        }
                        ps.println();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

/**
 * CSV Hilfsklasse
 */
class CSV {


    /**
     * Escaped den String nach CSV Specification falls nötig.
     *
     * @param value der String der Escaped werden soll
     * @return den leeren String, falls value gleich null war, sonst einen validen String
     */
    public static String escape(String value) {
        // null Fall
        if (value == null)
            return "";
            // enthält Sonderzeichen
        else if (value.contains(",")
                || value.contains("\n")
                || value.contains("\r")
                || value.contains("\""))
            return "\"" + value.replaceAll("\"", "\"\"") + "\"";
            // alles ok
        else
            return value;
    }


}


class FileWriter {
    public static void createFile() {
        //initialize Path object
        Path path = Paths.get("D:file.txt");
        //create file
        try {
            Path createdFilePath = Files.createFile(path);
            System.out.println("Created a file at : " + createdFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}