package pl.zayer.shoppinglist.activities;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import pl.zayer.shoppinglist.R;
import pl.zayer.shoppinglist.pojos.ShoppingList;

/**
 * RecyclerView for MainActivityFragment fragment.
 */
public class MainActivityFragmentListAdapter extends RecyclerView.Adapter<MainActivityFragmentListAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<ShoppingList> mShoppingLists;
    private OnShoppingListClickedListener mListener;

    public MainActivityFragmentListAdapter(Context context, OnShoppingListClickedListener listener) {
        mContext = context;
        mListener = listener;
        mShoppingLists = new ArrayList<>();
    }

    public void setShoppingLists(ArrayList<ShoppingList> shoppingLists) {
        mShoppingLists = shoppingLists;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_main_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ShoppingList item = mShoppingLists.get(position);

        holder.mainCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onShoppingListClicked(item, v);
            }
        });
        if (item.getTitle().equals("")) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                holder.titleTV.setTextColor(mContext.getResources().getColor(R.color.secondaryText));
            } else {
                holder.titleTV.setTextColor(mContext.getColor(R.color.secondaryText));
            }
            holder.titleTV.setText(R.string.fragment_main_list_item_no_title);
        } else {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                holder.titleTV.setTextColor(mContext.getResources().getColor(R.color.primaryText));
            } else {
                holder.titleTV.setTextColor(mContext.getColor(R.color.primaryText));
            }
            holder.titleTV.setText(item.getTitle());
        }

        if (item.getUncheckedItemsAsString(", ").equals("")) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                holder.contentTV.setTextColor(mContext.getResources().getColor(R.color.divider));
            } else {
                holder.contentTV.setTextColor(mContext.getColor(R.color.divider));
            }
            holder.contentTV.setText(R.string.fragment_main_list_item_empty_list);
        } else {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                holder.contentTV.setTextColor(mContext.getResources().getColor(R.color.secondaryText));
            } else {
                holder.contentTV.setTextColor(mContext.getColor(R.color.secondaryText));
            }
            holder.contentTV.setText(item.getUncheckedItemsAsString(", "));
        }
    }

    @Override
    public int getItemCount() {
        return mShoppingLists.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected CardView mainCV;
        protected TextView titleTV;
        protected TextView contentTV;


        public ViewHolder(View itemView) {
            super(itemView);
            this.mainCV = (CardView) itemView.findViewById(R.id.main_cv);
            this.titleTV = (TextView) itemView.findViewById(R.id.title_tv);
            this.contentTV = (TextView) itemView.findViewById(R.id.content_tv);
        }
    }

    public interface OnShoppingListClickedListener {
        void onShoppingListClicked(ShoppingList shoppingList, View view);
    }

}
