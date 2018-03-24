package pl.coreorb.shoppinglist.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import pl.coreorb.shoppinglist.R;
import pl.coreorb.shoppinglist.pojos.ShoppingList;

/**
 * RecyclerView for MainActivityFragment fragment.
 */
public class MainActivityFragmentListAdapter extends RecyclerView.Adapter<MainActivityFragmentListAdapter.ViewHolder> {

    private final Context mContext;
    private ArrayList<ShoppingList> mShoppingLists;
    private final OnShoppingListClickedListener mListener;

    MainActivityFragmentListAdapter(Context context, OnShoppingListClickedListener listener) {
        mContext = context;
        mListener = listener;
        mShoppingLists = new ArrayList<>();
    }

    void setShoppingLists(ArrayList<ShoppingList> shoppingLists) {
        mShoppingLists = shoppingLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_main_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ShoppingList item = mShoppingLists.get(position);

        holder.mainCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onShoppingListClicked(item, v);
            }
        });
        if (item.getTitle().equals("")) {
            holder.titleTV.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryText));
            holder.titleTV.setText(R.string.fragment_main_list_item_no_title);
        } else {
            holder.titleTV.setTextColor(ContextCompat.getColor(mContext, R.color.primaryText));
            holder.titleTV.setText(item.getTitle());
        }

        if (item.getUncheckedItemsAsString(", ").equals("")) {
            holder.contentTV.setTextColor(ContextCompat.getColor(mContext, R.color.divider));
            holder.contentTV.setText(R.string.fragment_main_list_item_empty_list);
        } else {
            holder.contentTV.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryText));
            holder.contentTV.setText(item.getUncheckedItemsAsString(", "));
        }
    }

    @Override
    public int getItemCount() {
        return mShoppingLists.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        final CardView mainCV;
        final TextView titleTV;
        final TextView contentTV;


        ViewHolder(View itemView) {
            super(itemView);
            this.mainCV = itemView.findViewById(R.id.main_cv);
            this.titleTV = itemView.findViewById(R.id.title_tv);
            this.contentTV = itemView.findViewById(R.id.content_tv);
        }
    }

    public interface OnShoppingListClickedListener {
        void onShoppingListClicked(ShoppingList shoppingList, View view);
    }

}
