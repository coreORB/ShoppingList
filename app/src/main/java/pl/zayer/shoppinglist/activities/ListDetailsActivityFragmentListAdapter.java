package pl.zayer.shoppinglist.activities;

import android.content.Context;
import android.graphics.Paint;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pl.zayer.shoppinglist.R;
import pl.zayer.shoppinglist.pojos.Item;

/**
 * RecyclerView for ListDetailsActivityFragment fragment.
 */
public class ListDetailsActivityFragmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = ListDetailsActivityFragmentListAdapter.class.getSimpleName();

    //constant for types of rows
    private static final int ROW_TYPE_HEADER = 1;
    private static final int ROW_TYPE_REGULAR = 2;

    private Context mContext;
    private ArrayList<Item> mItems;
    private OnItemAddedListener mAddedListener;
    private OnItemEditedListener mEditedListener;
    private OnItemRemoveClickedListener mRemoveClickedListener;
    private OnShoppingListTitleEditedListener mTitleEditedListener;
    private boolean mShoppingListArchived;
    private String mShoppingListTitle;

    public ListDetailsActivityFragmentListAdapter(Context context, boolean shoppingListArchived,
                                                  String shoppingListTitle,
                                                  ArrayList<Item> items,
                                                  OnItemRemoveClickedListener listener,
                                                  OnItemEditedListener editedListener,
                                                  OnItemAddedListener addedListener,
                                                  OnShoppingListTitleEditedListener titleEditedListener) {
        mContext = context;
        mShoppingListArchived = shoppingListArchived;
        mShoppingListTitle = shoppingListTitle;
        mItems = items;
        mAddedListener = addedListener;
        mEditedListener = editedListener;
        mRemoveClickedListener = listener;
        mTitleEditedListener = titleEditedListener;
    }

    /**
     * Sets title of shopping list in adapter.
     * @param title title of shopping list
     */
    public void setShoppingListTitle(String title) {
        mShoppingListTitle = title;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case ROW_TYPE_HEADER:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_details_list_header, parent, false);
                viewHolder = new HeaderViewHolder(view);
                break;
            case ROW_TYPE_REGULAR:
                View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_details_list_item, parent, false);
                viewHolder = new RegularViewHolder(view2);
                break;
        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ROW_TYPE_HEADER;
        } else {
            return ROW_TYPE_REGULAR;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ROW_TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            bindHeaderViewHolder(headerHolder);
        } else if (getItemViewType(position) == ROW_TYPE_REGULAR) {
            final Item item = mItems.get(position-1);
            RegularViewHolder regularHolder = (RegularViewHolder) holder;
            bindRegularViewHolder(regularHolder, item);
        }
    }

    /**
     * Binder method for row of header type.
     * Check HeaderViewHolder for more info.
     * @param holder holder
     */
    private void bindHeaderViewHolder(final HeaderViewHolder holder) {
        holder.titleET.setText(mShoppingListTitle);
        if (mShoppingListArchived) {
            holder.archivedMessageTV.setVisibility(View.VISIBLE);
            holder.addItemLL.setVisibility(View.GONE);
            holder.titleET.setFocusable(false);
        } else {
            holder.addItemLL.setVisibility(View.VISIBLE);
            holder.archivedMessageTV.setVisibility(View.GONE);
            holder.titleET.setFocusable(true);
            holder.titleET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus && !((EditText) v).getText().toString().equals(mShoppingListTitle)) {
                        mTitleEditedListener.onShoppingListTitleEdited(((EditText) v).getText().toString());
                    }
                }
            });
            holder.addItemIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = holder.addItemET.getText().toString();
                    if (content.equals("")) {
                        Toast.makeText(mContext,
                                R.string.fragment_list_details_list_header_item_no_content_message,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Item item = new Item();
                        item.setContent(content);
                        holder.addItemET.setText("");
                        mAddedListener.onItemAdded(item);
                    }
                }
            });
        }
    }

    /**
     * Binder method for row of regular type.
     * Check RegularViewHolder for more info.
     * @param holder holder
     */
    private void bindRegularViewHolder(final RegularViewHolder holder, final Item item) {
        holder.checkedCB.setChecked(item.isChecked());
        holder.contentET.setText(item.getContent());
        if (item.isChecked()) {
            holder.contentET.setPaintFlags(holder.contentET.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.contentET.setPaintFlags(holder.contentET.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }

        if (mShoppingListArchived) {
            holder.checkedCB.setClickable(false);
            holder.contentET.setFocusable(false);
            holder.removeIV.setVisibility(View.GONE);
        } else {
            holder.checkedCB.setClickable(true);
            holder.contentET.setFocusable(true);
            holder.removeIV.setVisibility(View.VISIBLE);
            holder.checkedCB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.setChecked(((CheckBox) v).isChecked());
                    mEditedListener.onItemEdited(item);
                    if (item.isChecked()) {
                        holder.contentET.setPaintFlags(holder.contentET.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        holder.contentET.setPaintFlags(holder.contentET.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                }
            });
            holder.contentET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus && !((EditText) v).getText().toString().equals(item.getContent())) {
                        item.setContent(((EditText) v).getText().toString());
                        mEditedListener.onItemEdited(item);
                    }
                }
            });
            holder.removeIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRemoveClickedListener.onItemRemoveClicked(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size()+1;
    }

    /**
     * Class of header type holder. Displays EditText for shopping list title.
     * Also displays mini-form for adding new item if shopping list is NOT archived.
     * Otherwise, information that shopping list is archived will be shown.
     */
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        protected TextInputEditText titleET;
        protected LinearLayout addItemLL;
        protected EditText addItemET;
        protected ImageView addItemIV;
        protected TextView archivedMessageTV;


        public HeaderViewHolder(View itemView) {
            super(itemView);
            this.titleET = (TextInputEditText) itemView.findViewById(R.id.title_et);
            this.addItemLL = (LinearLayout) itemView.findViewById(R.id.add_item_ll);
            this.addItemET = (EditText) itemView.findViewById(R.id.add_item_et);
            this.addItemIV = (ImageView) itemView.findViewById(R.id.add_item_iv);
            this.archivedMessageTV = (TextView) itemView.findViewById(R.id.archived_message_tv);
        }
    }

    /**
     * Class of regular type holder. Displays item content, it's check state if shopping list is
     * archived. Otherwise updating and removing of item will be enabled.
     */
    public static class RegularViewHolder extends RecyclerView.ViewHolder {
        protected CheckBox checkedCB;
        protected EditText contentET;
        protected ImageView removeIV;


        public RegularViewHolder(View itemView) {
            super(itemView);
            this.checkedCB = (CheckBox) itemView.findViewById(R.id.checked_cb);
            this.contentET = (EditText) itemView.findViewById(R.id.content_tv);
            this.removeIV = (ImageView) itemView.findViewById(R.id.remove_iv);
        }
    }

    public interface OnItemAddedListener {
        void onItemAdded(Item item);
    }

    public interface OnItemEditedListener {
        void onItemEdited(Item item);
    }

    public interface OnItemRemoveClickedListener {
        void onItemRemoveClicked(Item item);
    }

    public interface OnShoppingListTitleEditedListener {
        void onShoppingListTitleEdited(String title);
    }
}
