package com.cx.sumicashersystem;

import java.util.List;



import com.cx.sumicashersystem.object.Vouches;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class VouchesAdapter extends ArrayAdapter<Vouches> {
	private Context mContext;
	private int mResource;
	private List<Vouches> mVouchesList;

	public VouchesAdapter(Context context, int resource, List<Vouches> objects) {
		super(context, resource, objects);
		mContext=context;
		mResource=resource;
		mVouchesList=objects;
	}

	@Override
	public int getCount() {
		// TODO �Զ����ɵķ������
		return super.getCount();
	}

	@Override
	public Vouches getItem(int position) {
		// TODO �Զ����ɵķ������
		return super.getItem(position);
	}

	@Override
	public int getPosition(Vouches item) {
		// TODO �Զ����ɵķ������
		return super.getPosition(item);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO �Զ����ɵķ������
		return super.getView(position, convertView, parent);
	}

}
