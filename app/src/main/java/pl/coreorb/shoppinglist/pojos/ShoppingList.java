package pl.coreorb.shoppinglist.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * POJO class for storing shopping list, implements Parcelable.
 * Created by ZaYeR on 2016-05-12.
 */
public class ShoppingList implements Parcelable {

    private long id;
    private String title;
    private boolean archived;
    private Calendar createdAt;
    private ArrayList<Item> items;

    /**
     * Additional flag telling if ShoppingList and its Items have been fully loaded from database.
     */
    private boolean fullyLoaded;

    public ShoppingList() {
        id = -1;
        title = "";
        archived = false;
        createdAt = new GregorianCalendar();
        items = new ArrayList<>();
        fullyLoaded = false;
    }

    public ShoppingList(long id, String title, boolean archived, Calendar createdAt, ArrayList<Item> items, boolean fullyLoaded) {
        this.id = id;
        this.title = title;
        this.archived = archived;
        this.createdAt = createdAt;
        this.items = items;
        this.fullyLoaded = fullyLoaded;
    }

    public ShoppingList(Parcel in) {
        id = in.readLong();
        title = in.readString();
        archived = in.readByte() != 0;
        createdAt = new GregorianCalendar();
        createdAt.setTimeInMillis(in.readLong());
        items = new ArrayList<>();
        in.readTypedList(items, Item.CREATOR);
        fullyLoaded = in.readByte() != 0;
    }

    /**
     * Reads all unchecked items in shopping list and joins them in one String object
     * separated by separator.
     * @param separator separator String
     * @return all unchecked items as single String
     */
    public String getUncheckedItemsAsString(String separator) {
        StringBuilder result = new StringBuilder();
        for (int i=0; i<items.size(); i++) {
            if (!items.get(i).isChecked()) {
                result.append(items.get(i).getContent() + separator);
            }
        }

        //if result contains text remove last separator
        if (result.length() > 0) {
            return result.substring(0, result.length() - separator.length());
        } else {
            return "";
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    /**
     * Gets additional flag telling if ShoppingList and its Items have been fully loaded from database.
     * @return true if object is fully loaded, false otherwise
     */
    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    /**
     * Sets additional flag telling if ShoppingList and its Items have been fully loaded from database.
     * @param fullyLoaded flag
     */
    public void setFullyLoaded(boolean fullyLoaded) {
        this.fullyLoaded = fullyLoaded;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void addItemAtBeginning(Item item) {
        items.add(0, item);
    }

    public void updateItem(Item item) {
        items.set(items.indexOf(item), item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeByte((byte) (archived ? 1 : 0));
        dest.writeLong(createdAt.getTimeInMillis());
        dest.writeTypedList(items);
        dest.writeByte((byte) (fullyLoaded ? 1 : 0));
    }

    /**
     *
     * @param o object to compare to
     * @return true if object is instance of ShoppingList and ids are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ShoppingList) {
            if (((ShoppingList) o).getId() == this.id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return "ShoppingList{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", archived=" + archived +
                ", createdAt=" + format.format(createdAt.getTime()) +
                ", items=" + items +
                '}';
    }

    public static final Creator<ShoppingList> CREATOR = new Creator<ShoppingList>() {

        @Override
        public ShoppingList createFromParcel(Parcel source) {
            return new ShoppingList(source);
        }

        @Override
        public ShoppingList[] newArray(int size) {
            return new ShoppingList[size];
        }
    };
}
