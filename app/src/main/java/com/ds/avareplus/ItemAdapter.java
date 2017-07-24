package com.ds.avareplus;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.avareplus.place.Destination;
import com.ds.avareplus.userDefinedWaypoints.Waypoint;

import junit.framework.Test;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.List;

/**
 * Created by clay d
 */
public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private List<Destination> mDestinationList;
    OnItemClickListener mItemClickListener;
    private static final int TYPE_ITEM = 0;
    private final LayoutInflater mInflater;
    private final OnStartDragListener mDragStartListener;
    private Context mContext;
    private int selected;

    public ItemAdapter(Context context, List<Destination> list, OnStartDragListener dragListner) {
        this.mDestinationList = list;
        this.mInflater = LayoutInflater.from(context);
        mDragStartListener = dragListner;
        mContext = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            View v = mInflater.inflate(R.layout.person_item, viewGroup, false);
            return new VHItem(v);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

    }

    @Override
    public int getItemViewType(int position) {
            return TYPE_ITEM;
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {

        if (viewHolder instanceof VHItem) {

            final VHItem holder= (VHItem)viewHolder;
            holder.container.setBackgroundColor(Color.LTGRAY);
            if (i == selected) {
                holder.container.setBackgroundColor(Color.argb(255,40,40,40));
            }


            ((VHItem) viewHolder).setWaypoint(mDestinationList.get(i));


            ((VHItem) viewHolder).waypoint.setText(mDestinationList.get(i).getID());
            ((VHItem) viewHolder).type.setText(mDestinationList.get(i).getType());
            ((VHItem) viewHolder).distance.setText(String.valueOf(mDestinationList.get(i).getDistance()));

            ((VHItem) viewHolder).time.setText(mDestinationList.get(i).getEte());
            ((VHItem) viewHolder).course.setText(mDestinationList.get(i).getCourse());
            ((VHItem) viewHolder).heading.setText("");
            ((VHItem) viewHolder).wind.setText(mDestinationList.get(i).getWinds());
            ((VHItem) viewHolder).fuel.setText(mDestinationList.get(i).getFuel());






            ((VHItem) viewHolder).image_menu.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });

            ((VHItem) viewHolder).image_select.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {

                        //enter code here to select as next destination
                        ((PlanActivity) mContext).newDestination(mDestinationList.get(i));
                        selected = holder.getAdapterPosition();
                        updateList(mDestinationList);



                    }
                    return false;
                }
            });

            ((VHItem) viewHolder).holo.setOnTouchListener(new OnSwipeTouchListener(mContext) {

                //add functionality to select
                /*
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
                */


                //add functionality to delete
                public void onSwipeLeft() {
                     mDestinationList.remove(i);
                    updateList(mDestinationList);
                    Toast.makeText(mContext, "Waypoint Deleted", Toast.LENGTH_SHORT).show();

                    ((PlanActivity) mContext).delete();
                }


            });

        }
    }


    @Override
    public int getItemCount() {
        if (mDestinationList == null) return 0;
        return mDestinationList.size();
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener ,ItemTouchHelperViewHolder{

        private ImageView image_menu; //set this to whatever u want to use to drag
        private ImageView image_select;
        public TextView waypoint;
        public TextView type;
        public TextView distance;
        public TextView time;
        public TextView course;
        public TextView heading;
        public TextView wind;
        public TextView fuel;
        public View container;
        private ImageView holo;
        private Destination data;


        public VHItem(View itemView) {
            super(itemView);

            data = null;
            container = (View) itemView.findViewById(R.id.container);
            image_menu = (ImageView) itemView.findViewById(R.id.image_menu);
            image_select = (ImageView) itemView.findViewById(R.id.image_Select);
            holo = (ImageView) itemView.findViewById(R.id.holo);

            waypoint = (TextView) itemView.findViewById(R.id.waypoint);
            type = (TextView) itemView.findViewById(R.id.type);
            distance = (TextView) itemView.findViewById(R.id.distance);
            time = (TextView) itemView.findViewById(R.id.time);
            course = (TextView) itemView.findViewById(R.id.course);
            heading = (TextView) itemView.findViewById(R.id.heading);
            wind = (TextView) itemView.findViewById(R.id.wind);
            fuel = (TextView) itemView.findViewById(R.id.fuel);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        public Destination getWaypoint() {
            return data;
        }

        public void setWaypoint(Destination wayPo) {
            data = wayPo;
        }

    }

    @Override
    public void onItemDismiss(int position) {
        mDestinationList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        //Log.v("", "Log position" + fromPosition + " " + toPosition);
        if (fromPosition < mDestinationList.size() && toPosition < mDestinationList.size()) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mDestinationList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mDestinationList, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }
        return true;
    }

    public void updateList(List<Destination> list) {
        mDestinationList = list;
        notifyDataSetChanged();
    }

    public List<Destination> getDestinationList() {
        return mDestinationList;
    }

}