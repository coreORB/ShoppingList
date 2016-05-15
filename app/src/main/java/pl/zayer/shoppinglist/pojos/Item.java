package pl.zayer.shoppinglist.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * POJO class for storing shopping lists items, implements Parcelable.
 * Created by ZaYeR on 2016-05-12.
 */
public class Item implements Parcelable {

    private long id;
    private String content;
    private boolean checked;

    public Item() {
        id = -1;
        content = "";
        checked = false;
    }

    public Item(Parcel in) {
        id = in.readLong();
        content = in.readString();
        checked = in.readByte() != 0;
    }

    public Item(long id, long shoppingListId, String content, boolean checked) {
        this.id = id;
        this.content = content;
        this.checked = checked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(content);
        dest.writeByte((byte) (checked ? 1 : 0));
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {

        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    /**
     *
     * @param o object to compare to
     * @return true if object is instance of Item and ids are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Item) {
            if (((Item) o).getId() == this.id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", checked=" + checked +
                '}';
    }
}
