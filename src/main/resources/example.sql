SELECT 
    *
FROM
    inventory.return_order
WHERE
    order_no IN (1950001812 , 1950001814, 1950001821);
SELECT 
    *
FROM
    inventory.return_order_item
WHERE
    return_id IN (1816004 , 1816000, 1815998, 1816010);
SELECT 
    *
FROM
    inventory.return_history
ORDER BY created_at DESC
LIMIT 0 , 100;
SELECT 
    *
FROM
    inventory.sync_complete_shipment_details_unicom
ORDER BY created_at DESC
LIMIT 0 , 100;
