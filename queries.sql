#1. 13422
select count(*)
from User;

#2 103
select count(*)
from Item I
         join Location L on I.Location = L.LocationID
where L.Name = 'New York';

select ItemID
from Item
where Location IN (select LocationID L from Location where Name = 'New York');

#3. 8365
-- 8364, 1 item fehlt :(
select count(AuctionID) from
(select count(CategoryID) as categories, AuctionID
from (select AuctionID, CategoryID
      from Auction
               left join Item_Categories IC on Auction.ItemID = IC.ItemID) as AICI
group by AuctionID having categories=4) as cAI;



#4 1046740686


#5 3130
select count(*)
from User
where SellerRating > 1000;

#6 6717

#7 150

-- ------------------
-- test queries

-- 19532
select count(*)
from Item;

-- 90269
select count(*)
from Item_Categories;

select count(*)
from Category;

-- 13129
select count(distinct Seller) as seller_count
from Auction;

-- 7010
select count(distinct Bidder) as bidder_count
from Bid;

-- 1959
select count(*)
from Auction
where Buy_Price <> 0;

-- 13090
select count(DISTINCT i.ItemID)
from Item i
         JOIN Location l ON i.Location = l.LocationID
WHERE l.Latitude <> 0
  AND l.Longitude <> 0;