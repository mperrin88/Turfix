package turfix.turfdatareader.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import turfix.turfdatareader.TurfDataReader;
import turfix.turfdatareader.impl.model.RaceResult;
import turfix.turfdatareader.model.IRaceResult;

public class TurfDataReaderImpl implements TurfDataReader{
	
	private final static String TurfDataWebAdress = "http://www.turf-fr.com/cgi-bin/presse_archives.pl?date=";
	
	private final static int NumberOfDays = 2500;
	
	private final static String CookieIdentifier = "CookieNom";
	
	private final static String LoginIdentifier = "PERRIN";
	
	private final static int ConnectionTimeOut = 20000;
	
	private final static Logger LOGGER = Logger.getLogger(TurfDataReaderImpl.class.getName());

	@Override
	public List<IRaceResult> readDataFromTurfWebSite() {
		List<String> datesAsStrings = buildDatesAsStrings();
		List<IRaceResult> raceResults = new ArrayList<>();
		
		for(String dateAsString : datesAsStrings){
			String dataWebAdress = buildAdressFromDate(dateAsString);
			
			raceResults.add(readDataFromDay(dataWebAdress));
		}
		
		return raceResults;
	}
	
	private List<String> buildDatesAsStrings(){
		List<String> dateList = new ArrayList<String>();
		
		Calendar dataCalendar = Calendar.getInstance();
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		
		for (int i=0; i<NumberOfDays ;i++){
			dataCalendar.add(Calendar.DAY_OF_YEAR, -1);
			
			String dateAsString = dateFormatter.format(dataCalendar.getTime());
			
			dateList.add(dateAsString);		
		}
		
		return dateList;
	}
	
	private String buildAdressFromDate(String dateAsString){
		return TurfDataWebAdress + dateAsString;
	}
	
	private RaceResult readDataFromDay(String dataWebAdress){
		Document document = null;
		try {
			document = Jsoup.connect(dataWebAdress).cookie(CookieIdentifier, LoginIdentifier).timeout(ConnectionTimeOut).get();
			
			String raceName = getRaceName(document);
			
			if(raceName.length() > 2){
				return buildRaceResult(raceName, document);
			}
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}
		
		return null;
	}
	
	private String getRaceName(Document document){
		Element elementsCourse = document.getElementsByTag("title").get(0);
		String infosCourse = String.valueOf(elementsCourse).replaceAll("<[^>]*>", "");
		List<String> infos= Arrays.asList(infosCourse.split("(Quinté"));

		return infos.get(0).trim();
	}
	
	
	private RaceResult buildRaceResult(String raceName, Document document){
		
		if(document.getElementsByTag("table").size()>20){
			List<String> raceInfos = new ArrayList<>();
			
			for (Element divElement: document.getElementsByTag("div")){
				
				String divElementAsString = divElement.toString();
				
				if (divElementAsString.contains("<div class=\"GraTxt\">") && divElementAsString.length()<200){
					String divContentAsString = divElementAsString.replaceAll("<[^>]*>", "");;
					raceInfos.addAll(Arrays.asList(divContentAsString.split(" ")));
					break;
				}
			}

			int raceDistance = 0;
			String racePlace = null;
			int horseCount = 0;
			String raceType = null;
			for(int i=0; i<raceInfos.size(); i++){
				String raceInfo = raceInfos.get(i);
				
				if(raceInfo.equals("mètres")){
					raceDistance=  Integer.parseInt(raceInfos.get(i-1));
					racePlace = raceInfos.get(i-3);
				}
				else if(raceInfo.equals("partants")){
					horseCount = Integer.parseInt(raceInfos.get(i-1));
					raceType =raceInfos.get(i+2);
				}
			}
			
			List<Integer> expectedResult = getExpectedRaceResult(document, horseCount);

			return new RaceResult(raceName, raceDistance, horseCount, raceType, racePlace, expectedResult);
		}
		return null;
	}
	
	private List<Integer> getExpectedRaceResult(Document doc, int horseCount){
		// Parcours le code source pour trouver le tableau contenant le classement de la presse
		int i;
		boolean trouve=false;
		
		Element elementTable = null;
		Elements elementsTR = null;
		for (i=0;i<doc.getElementsByTag("table").size();i++){
			elementTable = doc.getElementsByTag("table").get(i);
			elementsTR = elementTable.getElementsByTag("tr");
			for (Element tr : elementsTR) {
				Elements tds = tr.select("td");
                // Parcours chaque ligne du tableau
                for (Element td : tds) {
                	String tableContent = td.select("td").toString();
					if(tableContent.contains("Classement de la Presse")){
						trouve=true;
					}
                }
                if (trouve)
                	break;
			}
			if (trouve)
            	break;
			
		}
		
		//Classement prÃ©sent le tableau d'aprÃ¨s
		elementTable = doc.getElementsByTag("table").get(i+2);
		elementsTR = elementTable.getElementsByTag("tr");
		Elements tds = elementsTR.select("td");
		String contenu_Ligne = tds.select("td").toString();
    	contenu_Ligne=contenu_Ligne.replaceAll("<[^>]*>", "");
    	contenu_Ligne=contenu_Ligne.replaceAll("[\r\n]+", "");
    	List<String> lineInfos = Arrays.asList(contenu_Ligne.split(" "));
		//System.out.println(contenu_Ligne);
    	
    	int indice=0;
    	ArrayList<Integer> classement=new ArrayList<>();
    	for (String lineInfo : lineInfos){
    		if(!lineInfo.equals("")){
    			classement.add(Integer.parseInt(lineInfo));
    			indice++;
    			
    			//DÃ©passe le nb de partant on arrete la boucle
    			if (indice==horseCount){
    				break;
    			}
    		}
    		
    	}
    	
    	return classement;
	}
	
}
