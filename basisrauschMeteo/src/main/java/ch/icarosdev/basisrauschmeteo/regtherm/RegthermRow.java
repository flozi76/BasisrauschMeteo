package ch.icarosdev.basisrauschmeteo.regtherm;

import android.util.Log;

public class RegthermRow
{
    public static String TAG = "Basisrausch: Reghterm";

	public RegthermRow()
	{
		this.regthermData = new String[24];
	}
	
	public void parseRegthermData(String substring) {
		for (int i = 0; i < 21; i++)
		{
            try{
			    regthermData[i] = substring.substring(i, i+1);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Error parsing regthermstring:" + substring +"\n"+  e.toString(), e);
            }

		}
	}

	public String time;
	public String temperatur;
	public String temperaturTaupunkt;
	public String[] regthermData;
	public String midThermik;
}
