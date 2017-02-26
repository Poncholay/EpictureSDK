package com.poncholay.EpictureSdkDemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class ResponseAdapter extends ArrayAdapter<String> {
	public ResponseAdapter(Context context, ArrayList<String> responses) {
		super(context, 0, responses);
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		String response = getItem(position);

		if (convertView == null) {
			if (getItemViewType(position) == 1) {
				response = response.replaceFirst("Url : ", "").replaceFirst("Thumbnail : ", "");
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.image, null);
				ImageLoader imageLoader = ImageLoader.getInstance();
				imageLoader.displayImage(response, (ImageView) convertView);
			} else {
				convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
				((TextView) convertView).setText(response);
			}
		}

		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		String response = getItem(position);

		if (response.startsWith("Url : http") || response.startsWith("Thumbnail : http")) {
			return 1;
		}
		return 0;
	}
}
