package ch.icarosdev.basisrauschmeteo.regtherm;

import java.util.ArrayList;
import java.util.List;

public class RegthermPageData {
	
	public RegthermPageData(){
		this.regthermRows = new ArrayList<RegthermRow>();
	}
	
	public int pageUrlHash;
	public String headerRow;
	public List<RegthermRow> regthermRows;
	public String basis;
	public String top;
	public String maxtherm;
	public double maxwindkmh;
	
	public void resetRows() {
		this.regthermRows = new ArrayList<RegthermRow>();
	}
	
	public void setPageUrl(String url)
	{
		this.pageUrlHash = url.hashCode();
	}
}
