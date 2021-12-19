#1. 13422 done
select count(*)
from User;

#2 103 query correct, but missing locations
select COUNT(ItemID)
from Item
where Location IN (select LocationID L from Location where Name = 'New York');

#3. 8365 done
-- 8364, 1 item fehlt :(
select count(ItemID)
from (select * from Item_Categories group by ItemID having count(CategoryID) = 4) as `IC*`;


#4 1046740686 done
select ItemID, Currently
from Auction
where Currently =
      (select max(Currently)
       from (select Currently
             from Auction
             where Number_of_Bids > 0
               and Ends > '2001-12-20 00:00:01'
             group by Currently
            ) as CII)
  and Ends > '2001-12-20 00:00:01'
  and Number_of_Bids > 0;


#5 3130 done
select count(*)
from User
where SellerRating > 1000;

#6 6717 done
select count(distinct Bidder) from Bid where Bidder in
(select Seller from Auction);

#7 150 done
select count(distinct CategoryID) from Item_Categories where ItemID in
(select ItemID from Auction where auctionID in
(select Auction from Bid where Amount > 100));

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