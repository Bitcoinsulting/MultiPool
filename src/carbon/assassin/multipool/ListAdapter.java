package carbon.assassin.multipool;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {
	LinkedHashMap<String, String> data;
	Context context;
	public ListAdapter(Context context, LinkedHashMap<String, String> data)
	{
		super();
		this.data = data;
		this.context = context;
	}

	  public int getCount() {
		  if(data.size() == 0)
		  {
			  return 1;
		  }
	        return data.keySet().size();
	    }
	 
	    public Object getItem(int position) {
	        return position;
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View newView = convertView;
		if(data.size() == 0)
		{
			if(convertView == null)
			{
				 newView = inflater.inflate(R.layout.list_item_def, null);
				 TextView def = (TextView) newView.findViewById(R.id.defText);
				 def.setTextColor(ColorStateList.valueOf(Color.GRAY));
				 
			}
			return newView;
		}
		if(convertView == null)
		{
        newView = inflater.inflate(R.layout.list_item, null);
		}
		TextView title = (TextView) newView.findViewById(R.id.listTitle);
		TextView desc = (TextView) newView.findViewById(R.id.listValue);
		title.setText((new ArrayList<String>(data.keySet())).get(position));
		desc.setText((new ArrayList<String>(data.values())).get(position));
		return newView;
		
	}

}
