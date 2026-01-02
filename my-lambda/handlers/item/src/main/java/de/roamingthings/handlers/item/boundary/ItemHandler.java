package de.roamingthings.handlers.item.boundary;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import de.roamingthings.handlers.item.control.ItemProcessing;
import de.roamingthings.shared.model.entity.Item;

public class ItemHandler implements RequestHandler<Item, ItemResult> {

    @Override
    public ItemResult handleRequest(Item input, Context context) {
        return new ItemResult(ItemProcessing.createItem(input));
    }
}
