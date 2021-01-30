package com.evermc.evershop.api;

public enum ShopType {
    // type(id, location_count, item_set_count)
    BUY(1, 1, 1),
    SELL(2, 1, 1),
    TRADE(3, 2, 2),
    IBUY(4, 0, 1),
    ISELL(5, 0, 1),
    ITRADE(6, 0, 2),

    TOGGLE(8, 1, 0),
    DEVICE(9, 1, 0),
    DEVICEON(10, 1, 0),
    DEVICEOFF(11, 1, 0),

    SLOT(12, 1, 1),
    ISLOT(13, 0, 1),
    ITEMISLOT(14, 0, 2),

    DONATEHAND(15, 1, 0),
    DISPOSE(16, 1, 0)
    ;

    private int index;
    private int location_count;
    private int item_set_count;

    ShopType(int index, int location_count, int item_set_count){
        this.index = index;
        this.location_count = location_count;
        this.item_set_count = item_set_count;
    }

    public int id() {
        return this.index;
    }

    public int location_count() {
        return this.location_count;
    }

    public int item_set_count() {
        return this.item_set_count;
    }
}