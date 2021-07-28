package com.openrec.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openrec.R;
import com.openrec.models.VoiceFile;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static com.openrec.utility.Utility.InputFullDateFormat;
import static com.openrec.utility.Utility.OutputDateFormat12;
import static com.openrec.utility.Utility.OutputDateFormat24;

public class FilesItemAdapter extends RecyclerView.Adapter<FilesItemAdapter.ListViewHolder>
{
    private final List<VoiceFile> aListViewItems;
    private boolean is24Hrs;
    private OnItemClickListener itemClickListener;
    private OnItemLongCLickListener itemLongCLickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongCLickListener {
        void onItemLongCLick (int position);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        itemClickListener=clickListener;
    }

    public void setOnItemLongClickListener(OnItemLongCLickListener longClickListener) {
        itemLongCLickListener=longClickListener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView listViewTitle, dateTime;

        public ListViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener, OnItemLongCLickListener onItemLongCLickListener) {
            super(itemView);
            listViewTitle =itemView.findViewById(R.id.files_title);
            dateTime = itemView.findViewById(R.id.files_date_time);

            // HANDLE CLICK EVENTS OF ITEMS
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position=getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });

            // HANDLE LONG CLICK EVENTS OF ITEMS
            itemView.setOnLongClickListener(v -> {
                if (onItemLongCLickListener != null) {
                    int position=getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemLongCLickListener.onItemLongCLick(position);
                    }
                }
                return true;
            });
        }
    }

    public FilesItemAdapter(List<VoiceFile> listViewItems)
    {
        aListViewItems =listViewItems;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public FilesItemAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_files, parent, false);
        is24Hrs= DateFormat.is24HourFormat(view.getContext()); // CHECK IF 24 HRS OR 12 HRS FORMAT
        return new FilesItemAdapter.ListViewHolder(view, itemClickListener, itemLongCLickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilesItemAdapter.ListViewHolder holder, int position) {
        final VoiceFile voiceFile = aListViewItems.get(position);

        // SET TITLE WITHOUT EXTENSION
        holder.listViewTitle.setText(voiceFile.getTitle().substring(0, voiceFile.getTitle().length() - 4));

        // SHOW DATE AND TIME OF SAVING RECORD
        try {
            final Date date =InputFullDateFormat().parse(voiceFile.getLastUpdated());
            assert date != null;

            if (is24Hrs) {
                holder.dateTime.setText(OutputDateFormat24().format(date));
            }
            else{
                holder.dateTime.setText(OutputDateFormat12().format(date));
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return aListViewItems.size();
    }

    @Override
    public  int getItemViewType(int position){
        return aListViewItems.get(position).getId();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}