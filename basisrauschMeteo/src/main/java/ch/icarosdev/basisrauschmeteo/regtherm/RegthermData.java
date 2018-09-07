package ch.icarosdev.basisrauschmeteo.regtherm;

import java.util.ArrayList;
import java.util.List;

public class RegthermData {

	public RegthermData(){
		this.regthermPages = new ArrayList<RegthermPageData>();
	}
	
	public List<RegthermPageData> regthermPages;
	
	public RegthermPageData findPageData(String url){
		int hash = url.hashCode();
		for (RegthermPageData pageData : regthermPages) {
			
			if(pageData.pageUrlHash == hash){
				return pageData;
			}
		}
		
		return null;
	}
}
