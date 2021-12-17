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

        handler.writeItemCategoriesToCSV("itemCategories.csv");
        handler.writeItemToCSV("item.csv");
        handler.writeCategoriesToCSV("categories.csv");
        handler.writeLocationToCSV("location.csv");
        handler.writeMergedUsersToCSV("user.csv");
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

    private String characters = "";
    private HashMap<String, Integer> categoryTable = new HashMap<>();
    private HashMap<ArrayList<String>, Integer> locationTable = new HashMap<>();
    private ArrayList<String> currentLocation = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> itemTable = new HashMap<>();
    private int currentItemID;
    private int currentLocationID;

    private HashMap<Integer, Integer> itemCategories = new HashMap<>();

    private HashMap<String, List<String>> allSellers = new HashMap<>();
    private HashMap<String, List<String>> allBidders = new HashMap<>();

    private List<String> currentBidder = new ArrayList<>();

    private Deque<Touple> stack = new LinkedList<>();

    private ArrayList<String> currentAtts = new ArrayList<>();

    public void startDocument() {
        System.out.println("Start document");
    }


    public void endDocument() {

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
        currentAtts.clear();
        for (int i = 0; i < atts.getLength(); i++) {
            currentAtts.add(atts.getValue(i));
        }

        Touple touple = new Touple(qName, currentAtts, "");
        stack.push(touple);

        if (qName.equals("Item")) {
            currentItemID = Integer.parseInt(currentAtts.get(0));
        }

        if (qName.equals("Bidder")) {
            List<String> tempBidderList = new ArrayList<>();
            //added an empty slot to fill it later with the possible seller rating
            tempBidderList.addAll(Arrays.asList("" + currentLocationID, "", currentAtts.get(0)));
            allBidders.put(currentAtts.get(1), tempBidderList);
        }

        characters = "";
    }

    /*
    hashmap von allen sellern mit user id (name) und sellerRating
    hashmap von allen bidder mit user id (name) und bidderRating
    bei end document auf keys verschmelzen und userIDs generieren #big brain


     */

    public void endElement(String uri, String name, String qName) {
        /*
        if ("".equals(uri))
            System.out.println("End element: " + qName);
        else
            System.out.println("End element:   {" + uri + "}" + name);
         */

        /*
        if (qName.equals("Item")){
            for (element:currentItemXML) {

            }
        }
        */
        Touple currElement = stack.pop();

        if (stack.isEmpty())
            return;

        Touple parentElement = stack.peek();
        String parentqName = parentElement.qNameElement;
        List<String> parentAtts = parentElement.attributeList;
        String parentCharacters = parentElement.elementContent;


        switch (qName) {
            case "Category":
                if (!categoryTable.containsKey(characters)) {
                    categoryTable.put(characters, categoryTable.size() + 1);
                    itemCategories.put(categoryTable.size() + 1, currentItemID);
                }
                break;
            case "Location":
                currentLocation.add(characters);
                if (!currElement.attributeList.isEmpty()) {
                    currentLocation.addAll(currElement.attributeList);
                } else {
                    currentLocation.addAll(Arrays.asList("", ""));
                }
                break;
            case "Country":
                currentLocation.add(characters);
                if (!locationTable.containsKey(currentLocation)) {
                    ArrayList<String> tempArrayList = new ArrayList<>(currentLocation);
                    currentLocationID = locationTable.size() + 1;
                    locationTable.put(tempArrayList, currentLocationID);
                }
                currentLocation.clear();
                break;
            case "Item":

                break;
            case "Name":
                if (!itemTable.containsKey(currentItemID)) {
                    ArrayList<String> tempArrayList = new ArrayList<>();
                    tempArrayList.add(characters);
                    tempArrayList.add("" + currentLocationID);
                    itemTable.put(currentItemID, tempArrayList);
                }
                break;
            case "Seller":
                List<String> tempSellerList = new ArrayList<>();
                tempSellerList.addAll(Arrays.asList("" + currentLocationID, currentAtts.get(0), ""));
                allSellers.put(currentAtts.get(1), tempSellerList);
                break;
            case "Bidder":

                break;
        }
        currentAtts.clear();
        characters = "";
    }


    public void characters(char ch[], int start, int length) {
        //System.out.print("Characters:    \"");

        String normalString = new String(ch, start, length);
        characters += normalString;
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


    public HashMap<Integer, List<String>> mergeAllUsers() {
        HashMap<Integer, List<String>> mergedUsers = new HashMap<>();
        int userID = 0;

        for (Map.Entry<String, List<String>> sellerEntry : allSellers.entrySet()) {
            if (allBidders.containsKey(sellerEntry.getKey())) {
                List<String> tempList = new ArrayList<>();
                tempList.add(sellerEntry.getKey());
                tempList.addAll(sellerEntry.getValue());
                if (tempList.size()==4)
                tempList.set(3, allBidders.get(sellerEntry.getKey()).get(2));
                else tempList.add(allBidders.get(sellerEntry.getKey()).get(2));
                mergedUsers.put(userID, tempList);
                userID++;
                allBidders.remove(sellerEntry.getKey());
            } else {
                List<String> tempList = new ArrayList<>();
                tempList.add(sellerEntry.getKey());
                tempList.addAll(sellerEntry.getValue());
                mergedUsers.put(userID, tempList);
                userID++;
            }
        }

        if (!allBidders.isEmpty()) {
            for (Map.Entry<String, List<String>> bidderEntry : allBidders.entrySet()) {
                List<String> tempList = new ArrayList<>();
                tempList.add(bidderEntry.getKey());
                tempList.addAll(bidderEntry.getValue());
                mergedUsers.put(userID, tempList);
                userID++;
            }
        }
        return mergedUsers;
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
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(x -> x.get(0))))
                    .forEach((e) -> {
                        ps.print(e.getValue());
                        for (String s : e.getKey()) {
                            ps.print("," + CSV.escape(s));
                        }
                        ps.println();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeItemToCSV(String fileName) {
        try {
            PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8);
            itemTable.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .distinct()
                    .forEach((e) -> {
                        ps.print(e.getKey());
                        for (String s : e.getValue()) {
                            ps.print("," + CSV.escape(s));
                        }
                        ps.println();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeItemCategoriesToCSV(String fileName) {
        try {
            PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8);
            itemCategories.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach((e) -> ps.println(e.getKey() + "," + e.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeMergedUsersToCSV(String fileName) {
        try {
            PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8);
            mergeAllUsers().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .distinct()
                    .forEach((e) -> {
                        ps.print(e.getKey());
                        for (String s : e.getValue()) {
                            ps.print("," + CSV.escape(s));
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


class Touple {
    String qNameElement;
    List<String> attributeList;
    String elementContent;

    Touple(String qName, List<String> attributes, String characters) {
        qNameElement = qName;
        attributeList = new ArrayList<>(attributes);
        elementContent = characters;
    }

}
