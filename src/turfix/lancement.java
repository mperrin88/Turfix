package turfix;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.FileOutputStream;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class lancement {

	public static String adress_fichier="/Users/maximeP/Desktop/Turfix_V2/Data_base.xlsx";
	public static ArrayList<String> l_nomCourse=new ArrayList<String>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//Liste de date
		ArrayList<String> l_date=Liste_date();
System.out.println("PRONO");
		//lecture classement des chevaux par jour
		Contenu_HTMl_prono(l_date);
System.out.println("RESULTAT");	
		//On inscrit les résultats sur la feuille résultats
    	//Contenu_HTMl_res(l_date, l_nomCourse);
    	
System.out.println("TERMINER");
	}

	private static ArrayList<String> Liste_date(){
	
		ArrayList<String> liste_date=new ArrayList<String>();
		int mois,jour;
		String Smois,Sjour;
		
		for (int i=0;i<2500;i++){
			Calendar cal = GregorianCalendar.getInstance(Locale.FRANCE);
	        cal.add(Calendar.DAY_OF_YEAR, -i);
	        mois=cal.get(Calendar.MONTH)+1;
	        if (mois<10){
	        	Smois="0"+mois;
	        }
	        else{
	        	Smois=String.valueOf(mois);
	        }
	        
	        jour=cal.get(Calendar.DAY_OF_MONTH);
	        if (jour<10){
	        	Sjour="0"+jour;
	        }
	        else{
	        	Sjour=String.valueOf(jour);
	        }
	        
	        //System.out.println(cal.get(Calendar.YEAR)+ "_" +Smois+ "_" +Sjour+ " " +i);
	        liste_date.add(cal.get(Calendar.YEAR)+ "-" +Smois+ "-" +Sjour);
		}
		
		return liste_date;
		
	}

	public static void Contenu_HTMl_prono (ArrayList<String> lDate) {
    
	String dateJ,adresse;
	String contenu_Tableau,contenu_Ligne;
	String infosCourse,nom_course,distance = null,nb_partants = null,lieux = null,style_course = null;
	
	String [] infos = null,L_contenu_Ligne = null;
	
	Element elementsTable = null,elementsCourse;
	Elements elementsTR;
	
	try {
			// R�cup�re contenu de la page web mis en param�tre
			for (int d=0; d<lDate.size();d++){
				dateJ=lDate.get(d);
	System.out.println(dateJ+ " " + d);
				//dateJ="2018-08-03";
				adresse="http://www.turf-fr.com/cgi-bin/presse_archives.pl?date="+dateJ;
			
				Document doc = Jsoup.connect(adresse).cookie("CookieNom", "PERRIN").timeout(20000).get();
				
				//Récupère le nom de la course
				elementsCourse =doc.getElementsByTag("title").get(0);
				infosCourse=String.valueOf(elementsCourse).replaceAll("<[^>]*>", "");
				infos=infosCourse.split("(Quinté)");

				nom_course=infos[0];
				if(nom_course.length()>2){
					
					nom_course=nom_course.substring(0,nom_course.length()-2);
					l_nomCourse.add(nom_course);
					//Récupère les infos de la course
					if(doc.getElementsByTag("table").size()>20){
						
						for (int i=0;i<doc.getElementsByTag("div").size();i++){
							elementsCourse =doc.getElementsByTag("div").get(i);
							String t=elementsCourse.toString();
							if (t.contains("<div class=\"GraTxt\">") && t.length()<200){
								infosCourse=t.replaceAll("<[^>]*>", "");
								infos=infosCourse.split(" ");
								break;
							}
						}
						
						for (int i=0;i<infos.length;i++){
							//System.out.println(infos[i]+ " " +i);
							if(infos[i].equals("mètres")){
								distance=infos[i-1];	//Distance
								lieux=infos[i-3];//nom de la course
							}
							else if(infos[i].equals("partants")){
								nb_partants=infos[i-1]; //Nombre de partants 
								style_course=infos[i+2]; //Style de course
							}
						}
								
						// Parcours le code source pour trouver le tableau contenant le classement de la presse
						int i;
						boolean trouve=false;
						for (i=0;i<doc.getElementsByTag("table").size();i++){
							elementsTable = doc.getElementsByTag("table").get(i);
							elementsTR=elementsTable.getElementsByTag("tr");
							for (Element tr : elementsTR) {
								Elements tds = tr.select("td");
				                // Parcours chaque ligne du tableau
				                for (Element td : tds) {
				                	contenu_Tableau = td.select("td").toString();
									if(contenu_Tableau.contains("Classement de la Presse")){
										trouve=true;
									}
				                }
				                if (trouve)
				                	break;
							}
							if (trouve)
			                	break;
							
						}
			
						//Classement présent le tableau d'après
						elementsTable = doc.getElementsByTag("table").get(i+2);
						elementsTR=elementsTable.getElementsByTag("tr");
						Elements tds = elementsTR.select("td");
						contenu_Ligne = tds.select("td").toString();
			        	contenu_Ligne=contenu_Ligne.replaceAll("<[^>]*>", "");
			        	contenu_Ligne=contenu_Ligne.replaceAll("[\r\n]+", "");
			        	L_contenu_Ligne=contenu_Ligne.split(" ");
						//System.out.println(contenu_Ligne);
			        	
			        	int nb=Integer.parseInt(nb_partants);
			        	int indice=0;
			        	ArrayList<String> classement=new ArrayList<>();
			        	for (int c=0;c<L_contenu_Ligne.length;c++){
			        		if(!L_contenu_Ligne[c].equals("")){
			        			classement.add(L_contenu_Ligne[c].toString());
			        			indice++;
			        			
			        			//Dépasse le nb de partant on arrete la boucle
			        			if (indice==nb){
			        				break;
			        			}
			        		}
			        		
			        	}
			        	inscrit_infosProno(dateJ,nom_course,distance,nb_partants,lieux,style_course,classement);
					
					}
				}
			}
				
		} catch (IOException ex) {
			Logger.getLogger("test").log(Level.SEVERE, null, ex);
		}

	}

	private static void inscrit_infosProno(String DateCourse,String nomCourse,String distanceCourse,String nbPartants,
										String lieuxCourse,String styleCourse,ArrayList<String> Lclassement){
		//Ouvre le fichier Excel pour enregistrer les donnéees
		
    	XSSFSheet feuil ;
        XSSFRow row ;
        XSSFCell cell;
        FileInputStream file;
        
		try {
			file = new FileInputStream(adress_fichier);
		 
	        XSSFWorkbook wb = new XSSFWorkbook(file);
	        feuil=wb.getSheetAt(0);
	        int DernLigne =feuil.getPhysicalNumberOfRows();
	        row = feuil.createRow(DernLigne);
	        
	        //Inscrit les infos et le classement des chevaux
	        cell = row.createCell(0);
	        cell.setCellValue(DateCourse);
	        
	        cell = row.createCell(1);
	        cell.setCellValue(nomCourse);
	        
	        cell = row.createCell(2);
	        cell.setCellValue(lieuxCourse);
	        
	        cell = row.createCell(3);
	        cell.setCellValue(distanceCourse);
	        
	        cell = row.createCell(4);
	        cell.setCellValue(styleCourse);
	        
	        cell = row.createCell(5);
	        cell.setCellValue(nbPartants);
	        
	        for (int i=0;i<Lclassement.size();i++){
	        	cell = row.createCell(6+i);
		        cell.setCellValue(Lclassement.get(i));
	        }
        
	        //Ecriture dans le fichier
            FileOutputStream fileOut= new FileOutputStream(adress_fichier);
            wb.write(fileOut);
            wb.close();
            
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static void Contenu_HTMl_res (ArrayList<String> lDateJ,ArrayList<String> lnomCourse) {
        
        String NomCourseRES = null,res=null;
        boolean Trouve=false;
        String nomCourse;
        
        try {
        	
        	for (int i=0;i<lDateJ.size();i++){
        		Trouve=false;
        		nomCourse="";
System.out.println("Resultat "+lDateJ.get(i));
	        	// R�cup�re contenu de la page web mis en param�tre
	        	String adresse_res= "http://www.turf-fr.com/calendrier-courses/"+lDateJ.get(i)+".html";
	        	Document doc = Jsoup.connect(adresse_res)
	                    .cookie("CookieNom", "PERRIN")
	                    .timeout(20000)
	                    .get();		
	        	if(doc.getElementsByTag("table").size()>3){
	        		
		            // Cible le tableau contenant le tableau chronologique des arriv�es
		            Element elementsByTag = doc.getElementsByTag("table").get(4);
		            Elements trs = elementsByTag.getElementsByTag("tr");
		            for (Element tr : trs) {  
		                Elements tds = tr.select("td");
		                // Parcours chaque ligne du tableau
		                Trouve=false;
		                for (Element td : tds) {
		                    //i correspond � une colonne du tableau. Chaque colonne est rang� dans une liste sp�cifique
		                    //Récupère le tire de la course
		
		                    if(Trouve){
		                        res = td.select("td").toString();
		                        res=res.replaceAll("<[^>]*>", "");       
		                        break;
		                    }
		
		                    NomCourseRES = td.select("td").toString();
		                    NomCourseRES=NomCourseRES.replaceAll("<[^>]*>", ""); 
		                    
		                    //System.out.println("1 "+NomCourseRES+  " "+NCourse);
		                    //Si la ligne contient le nom de la course du quinté      
		                    for(int j=0;j<lnomCourse.size();j++){
		                    	if (NomCourseRES.contains(lnomCourse.get(j))){
		                    		nomCourse=lnomCourse.get(j);
			                        Trouve=true;
			                    }
		                    }
		                    
		                }
		            }
		            
		            if(Trouve){
		            	System.out.println(lDateJ.get(i)+ " " + i);
		            
		                //Ouvre le fichier Excel pour enregistrer les donnéees
			            FileInputStream file = new FileInputStream(adress_fichier); 
			            XSSFWorkbook wb = new XSSFWorkbook(file);
			            
			            //System.out.println(NbFeuil)    ;  
			            XSSFSheet feuil ;
			            XSSFRow row ;
			            XSSFCell cell;
			             
			         // System.out.println("RES2 "+res);   
			            res=res.replace("&nbsp;","");
			            
			            res=res.replace(" ","");
			            //Découpe la valeur récupérée
			            String values[]  = res.split("-");
			            
			            //Enregistre les données ds le fichier Excel sources
			            feuil=wb.getSheetAt(1);       
			            
			            //Recherche la dernière ligne
			            int DernLigne =feuil.getPhysicalNumberOfRows();
			            row = feuil.createRow(DernLigne);
			            
			            //Date du prono et nom de la course    
			            cell = row.createCell(0);
			            cell.setCellValue(lDateJ.get(i));
			            cell = row.createCell(1);
			            cell.setCellValue(nomCourse);     
			            
			            cell = row.createCell(2);
			            cell.setCellValue(Integer.parseInt(values[0]));
			            cell = row.createCell(3);
			            cell.setCellValue(Integer.parseInt(values[1]));
			            cell = row.createCell(4);
			            cell.setCellValue(Integer.parseInt(values[2]));
			            cell = row.createCell(5);
			            cell.setCellValue(Integer.parseInt(values[3]));
			            cell = row.createCell(6);
			            cell.setCellValue(Integer.parseInt(values[4]));
			                
			
			            //Ecriture dans le fichier
			            FileOutputStream fileOut;
			            fileOut = new FileOutputStream(adress_fichier);
			            wb.write(fileOut);
			            wb.close(); 
	
		            }
	        	}
        	
        	}
        } catch (IOException ex) {
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }

    }


}
