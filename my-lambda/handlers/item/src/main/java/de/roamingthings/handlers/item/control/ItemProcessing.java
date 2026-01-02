package de.roamingthings.handlers.item.control;

import de.roamingthings.shared.model.entity.Item;

public interface ItemProcessing {

    System.Logger LOGGER = System.getLogger(ItemProcessing.class.getName());

    static String createItem(Item input) {
        LOGGER.log(System.Logger.Level.INFO, "Creating item: {0}", input);
        return "[%s] You gave me %s with description %s".formatted(input.id(), input.name(), input.description());
    }
}
