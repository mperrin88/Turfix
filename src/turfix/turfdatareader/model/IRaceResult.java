package turfix.turfdatareader.model;

import java.util.List;

public interface IRaceResult {

	String getRaceName();
	
	int getRaceDistance();
	
	int getHorseCount();
	
	String getRaceType();
	
	String getRacePlace();
	
	List<Integer> getExpectedResult();
}
