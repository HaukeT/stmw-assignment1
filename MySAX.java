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
import java.text.SimpleDateFormat;
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

        handler.writeItemCategoriesToCSV("Item_Categories.csv");
        handler.writeItemToCSV("item.csv");
        handler.writeCategoriesToCSV("category.csv");
        handler.writeLocationToCSV("location.csv");
        handler.mergedUsers = handler.mergeAllUsers();
        handler.writeToCSV("user.csv", handler.mergedUsers);
        handler.insertIntoAuctionTable();
        handler.writeToCSV("auction.csv", handler.finishedAuctionTable);
        handler.insertIntoBidTable();
        handler.writeToCSV("bid.csv", handler.finishedBidTable);
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
    private HashMap<Integer, Integer> itemTable = new HashMap<>();
    private int currentItemID;
    private int currentLocationID;
    private String currentBidderName;

    private String currentSellerName;

    private List<Map.Entry<Integer, Integer>> itemCategories = new ArrayList<>();

    private HashMap<String, List<String>> allSellers = new HashMap<>();
    private HashMap<String, List<String>> allBidders = new HashMap<>();
    private HashMap<Integer, List<String>> mergedUsers = new HashMap<>();

    private List<List<String>> auctionTable = new ArrayList<>();
    private HashMap<Integer, List<String>> finishedAuctionTable = new HashMap<>();
    private List<String> currentAuction = new ArrayList<>();

    private List<List<String>> bidTable = new ArrayList<>();
    private List<String> currentBid = new ArrayList<>();
    private Map<Integer, List<String>> finishedBidTable = new HashMap<>();

    private Deque<Touple> stack = new LinkedList<>();

    private ArrayList<String> currentAtts = new ArrayList<>();


    public void startElement(String uri, String name,
                             String qName, Attributes atts) {
        currentAtts.clear();
        for (int i = 0; i < atts.getLength(); i++) {
            currentAtts.add(atts.getValue(i));
        }

        Touple touple = new Touple(qName, currentAtts, "");
        stack.push(touple);

        if (qName.equals("Item")) {
            currentItemID = Integer.parseInt(currentAtts.get(0));
            itemTable.put(currentItemID, currentLocationID);
        }

        if (qName.equals("Bidder")) {
            List<String> tempBidderList = new ArrayList<>();
            //added an empty slot to fill it later with the possible seller rating
            tempBidderList.addAll(Arrays.asList("" + currentLocationID, "", currentAtts.get(0)));
            allBidders.put(currentAtts.get(1), tempBidderList);
            currentBidderName = currentAtts.get(1);
        }

        characters = "";
    }

    public void endElement(String uri, String name, String qName) {
        Touple currElement = stack.pop();

        switch (qName) {
            case "Category":
                if (!categoryTable.containsKey(characters)) {
                    categoryTable.put(characters, categoryTable.size() + 1);
                }
                itemCategories.add(Map.entry(categoryTable.get(characters), currentItemID));
                break;
            case "Location":
                currentLocation.clear();
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
            case "Bidder":
                if (!currentLocation.isEmpty()) {
                    if (!locationTable.containsKey(currentLocation)) {
                        currentLocation.add("");
                        ArrayList<String> tempArrayList = new ArrayList<>(currentLocation);
                        currentLocationID = locationTable.size() + 1;
                        locationTable.put(tempArrayList, currentLocationID);
                    }
                }
                break;

            case "Seller":
                List<String> tempSellerList = new ArrayList<>();
                tempSellerList.addAll(Arrays.asList("" + currentLocationID, currentAtts.get(0), ""));
                allSellers.put(currentAtts.get(1), tempSellerList);
                currentSellerName = currentAtts.get(1);
                break;
            case "Name":
                currentAuction.add(characters);
                break;
            case "Currently":
                currentAuction.add(strip(characters));
                break;
            case "Buy_Price":
                currentAuction.add(strip(characters));
                break;
            case "First_Bid":
                //if there is no Buy_Price add empty field
                if (currentAuction.size() < 3)
                    currentAuction.add("");
                currentAuction.add(strip(characters));
                break;
            case "Number_of_Bids":
                currentAuction.add(characters);
                break;
            case "Started":
                //Date is "MMM-dd-yy HH:mm:ss"
                //TIMESTAMP is "yyyy-MM-dd'T'HH:mm:ss"
                try {
                    currentAuction.add("" + reformatDate(characters));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case "Ends":
                try {
                    currentAuction.add("" + reformatDate(characters));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                currentAuction.add("" + currentItemID);
                break;
            case "Description":
                currentAuction.addAll(Arrays.asList(currentSellerName, limitStringLength(characters)));
                auctionTable.add(new ArrayList<>(currentAuction));
                currentAuction.clear();
                break;
            case "Time":
                currentBid.add("" + currentItemID);
                currentBid.add(currentBidderName);
                try {
                    currentBid.add(reformatDate(characters));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case "Amount":
                currentBid.add(strip(characters));
                bidTable.add(new ArrayList<>(currentBid));
                currentBid.clear();
                break;
        }
        currentAtts.clear();
        characters = "";
    }

    private String limitStringLength(String str) {
        if (str.length() >= 4000)
            return str.substring(0, 4000);
        return str;
    }


    private String reformatDate(final String str) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM-dd-yy HH:mm:ss", Locale.US);
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return iso.format(formatter.parse(str));
    }

    public void characters(char ch[], int start, int length) {
        String normalString = new String(ch, start, length);
        characters += normalString.replaceAll("^([\n\r\\s]+)", "");
    }


    public HashMap<Integer, List<String>> mergeAllUsers() {
        HashMap<Integer, List<String>> mergedUsers = new HashMap<>();
        int userID = 0;

        for (Map.Entry<String, List<String>> sellerEntry : allSellers.entrySet()) {
            if (allBidders.containsKey(sellerEntry.getKey())) {
                List<String> tempList = new ArrayList<>();
                tempList.add(sellerEntry.getKey());
                tempList.addAll(sellerEntry.getValue());
                if (tempList.size() == 4)
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


    public void insertIntoAuctionTable() {
        int currentAuctionID = 0;
        Map<String, Integer> userMapID = new HashMap<>();

        for (Map.Entry<Integer, List<String>> user : mergedUsers.entrySet()) {
            // map user name to userID
            userMapID.put(user.getValue().get(0), user.getKey());
        }

        for (List<String> entry : auctionTable) {
            List<String> tempList = new ArrayList<>(entry);
            //at slot 8 replace Seller name with user id, by asking userMapID for the userID with the Seller name
            tempList.set(8, "" + userMapID.get(entry.get(8)));
            finishedAuctionTable.put(currentAuctionID, tempList);
            currentAuctionID++;
        }
    }

    public void insertIntoBidTable() {
        int currentBidID = 0;
        Map<String, Integer> bidderMapID = new HashMap<>();
        Map<String, Integer> auctionMapItemID = new HashMap<>();

        for (Map.Entry<Integer, List<String>> user : mergedUsers.entrySet()) {
            // map user name to userID
            bidderMapID.put(user.getValue().get(0), user.getKey());
        }

        for (Map.Entry<Integer, List<String>> auction : finishedAuctionTable.entrySet()) {
            //map auctionID to its ItemID
            auctionMapItemID.put(auction.getValue().get(7), auction.getKey());
        }

        for (List<String> entry : bidTable) {
            List<String> tempList = new ArrayList<>(entry);
            //at slot 1 replace BidderName with userID, by asking bidderMapID for the userID with the Bidder name
            tempList.set(1, "" + bidderMapID.get(entry.get(1)));
            //at slot 0 replace AuctionName with auctionID, by asking auctionMapItemID for the
            tempList.set(0, "" + auctionMapItemID.get(entry.get(0)));
            finishedBidTable.put(currentBidID, tempList);
            currentBidID++;
        }
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
                    .forEach((e) -> ps.println(e.getKey() + "," + e.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeItemCategoriesToCSV(String fileName) {
        try {
            PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8);
            itemCategories.stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach((e) -> ps.println(e.getKey() + "," + e.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToCSV(String fileName, Map<Integer, List<String>> map) {
        try {
            PrintStream ps = new PrintStream(fileName, StandardCharsets.UTF_8);
            map.entrySet().stream()
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
