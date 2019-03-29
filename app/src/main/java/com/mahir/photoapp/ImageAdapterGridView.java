package com.mahir.photoapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageAdapterGridView extends ArrayAdapter
{
    private Context context;
    private int layoutResourceId;
    private ArrayList<Bitmap> data = new ArrayList();

    public ImageAdapterGridView(Context context, int layoutResourceId, ArrayList<Bitmap> data)
    {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = row.findViewById(R.id.image);
//            holder.image.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) row.getTag();
        }

        holder.image.setImageBitmap(data.get(position));
        return row;
    }

    static class ViewHolder
    {
        ImageView image;
    }
}
