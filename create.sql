CREATE DATABASE IF NOT EXISTS ad;
USE ad;
create table Category
(
    CategoryID int          not null
        primary key,
    Name       varchar(512) null
);

create table Location
(
    LocationID int          not null
        primary key,
    Name       varchar(512) null,
    Latitude   float        null,
    Longitude  float        null,
    Country    varchar(512) null
);

create table Item
(
    ItemID   int not null
        primary key,
    Location int null,
    constraint Item_ibfk_1
        foreign key (Location) references Location (LocationID)
);

create index Location
    on Item (Location);

create table Item_Categories
(
    CategoryID int not null,
    ItemID     int not null,
    primary key (CategoryID, ItemID),
    constraint Item_Categories_ibfk_1
        foreign key (CategoryID) references Category (CategoryID),
    constraint Item_Categories_ibfk_2
        foreign key (ItemID) references Item (ItemID)
);

create index ItemID
    on Item_Categories (ItemID);

create table User
(
    UserID       int          not null
        primary key,
    Name         varchar(512) null,
    Location     int          null,
    SellerRating int          null,
    BidderRating int          null,
    constraint User_UserID_uindex
        unique (UserID),
    constraint User_ibfk_1
        foreign key (Location) references Location (LocationID)
);

create table Auction
(
    AuctionID      int           not null
        primary key,
    Name           varchar(512)  null,
    Currently      decimal(8, 2) null,
    Buy_Price      decimal(8, 2) null,
    First_Bid      decimal(8, 2) null,
    Number_of_Bids int           null,
    Started        timestamp     null,
    Ends           timestamp     null,
    ItemID         int           not null,
    Seller         int           null,
    Description    varchar(4000) null,
    constraint Auction_Item_ItemID_fk
        foreign key (ItemID) references Item (ItemID),
    constraint Auction_Seller_UserID_fk
        foreign key (Seller) references User (UserID)
);

create table Bid
(
    BidID   int                                   not null
        primary key,
    Auction int                                   not null,
    Bidder  int                                   not null,
    Time    timestamp default current_timestamp() not null on update current_timestamp(),
    Amount  decimal(8, 2)                         not null,
    constraint Bid_Bidder_UserID_fk
        foreign key (Bidder) references User (UserID),
    constraint Bid_ibfk_1
        foreign key (Auction) references Auction (AuctionID)
);

create index Auction
    on Bid (Auction);

create index Location
    on User (Location);

