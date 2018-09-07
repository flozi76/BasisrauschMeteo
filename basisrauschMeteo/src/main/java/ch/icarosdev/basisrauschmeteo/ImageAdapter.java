package ch.icarosdev.basisrauschmeteo;

import java.util.List;

import ch.icarosdev.basisrauschmeteo.regtherm.RegthermRow;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    int height = 0;
    int width = 24;
    List<RegthermRow> regthermRows;
    public static String TAG = "WebViewLoader";
    
    public ImageAdapter(Context c, List<RegthermRow> regthermRows, int height) {
        mContext = c;
        this.regthermRows = regthermRows;
        this.height = height;
    }

    public int getCount() {
        return width;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	TextView textView;
    	textView = new TextView(mContext);
        textView.setLayoutParams(new GridView.LayoutParams(15, 15));
        textView.setTextSize(8);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        
        try{
	    	
        	if(regthermRows != null && regthermRows.size() > position){
        		RegthermRow row = this.regthermRows.get(position);
    	        String regData = row.regthermData[this.height];

		        if (convertView == null) {  // if it's not recycled, initialize some attributes
		            
		            
		            if(regData.equals(".")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_point));
		            	textView.setTextColor(Color.BLACK);
		            } else if(regData.equals("*")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_star));
		            	textView.setTextColor(Color.WHITE);
		            } else if(regData.equals("1")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_1));
		            	textView.setTextColor(Color.BLACK);
		            } else if(regData.equals("2")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_2));
		            	textView.setTextColor(Color.BLACK);
		            } else if(regData.equals("3")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_3));
		            	textView.setTextColor(Color.BLACK);
		            } else if(regData.equals("4")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_4));
		            	textView.setTextColor(Color.RED);
		            } else if(regData.equals("5")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_5));
		            	textView.setTextColor(Color.RED);
		            } else if(regData.equals("6")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_6));
		            	textView.setTextColor(Color.RED);
		            } else if(regData.equals("7")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_7));
		            	textView.setTextColor(Color.RED);
		            } else if(regData.equals("8")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_8));
		            	textView.setTextColor(Color.RED);
		            } else if(regData.equals("9")){
		            	textView.setBackgroundColor(mContext.getResources().getColor(R.color.regtherm_color_9));
		            	textView.setTextColor(Color.RED);
		            }
		            	            
		        } else {
		            textView = (TextView) convertView;
		        }
		
		        //imageView.setImageResource(R.drawable.vpi__tab_selected_holo);
		        textView.setText(regData);
		        return textView;
        	}
     } catch (Exception e) {
		Log.e(TAG, e.toString(), e);
	}
    return textView;
    }
}
