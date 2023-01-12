package jadelab2;

import java.util.UUID;

public class Book {
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String ID;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getPrice() {
        return Price;
    }

    public void setPrice(int price) {
        Price = price;
    }

    public  int getShippingPrice() {
        return ShippingPrice;
    }

    public void setShippingPrice(int shippingPrice) {
        ShippingPrice = shippingPrice;
    }

    public String Title;
    public int Price;
    public int ShippingPrice;

    public Book(String id, String title, int price, int shippingPrice){
        ID = id;
        Title = title;
        Price = price;
        ShippingPrice = shippingPrice;
    }

    public int getFullPrice(){
        return getPrice() + getShippingPrice();
    }
}
