#!/bin/bash

# Run the drop.sql batch file to drop existing tables.
# Inside the drop.sql, you should check whether the table exists. Drop them ONLY if they exist.
mysql -u stmw < drop.sql

# Run the create.sql batch file to create the database (if it does not exist) and the tables.
mysql -u stmw ad < create.sql

# Compile and run the convertor
javac MySAX.java
java MySAX ebay-data/items-*.xml

# Run the load.sql batch file to load the data
mysql -u stmw ad < load.sql

# Remove all temporary files
rm *.class
rm *.csv


