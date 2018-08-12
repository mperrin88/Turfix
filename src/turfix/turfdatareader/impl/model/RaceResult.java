package turfix.turfdatareader.impl.model;

import java.util.List;

import turfix.turfdatareader.model.IRaceResult;

public class RaceResult implements IRaceResult{
	private final String raceName;
	
	private final int raceDistance;
	
	private final int horseCount;
	
	private final String raceType;
	
	private final String racePlace;
	
	private final List<Integer> expectedResult;		
	
	public RaceResult(String raceName, int raceDistance, int horseCount, String raceType, String racePlace,
			List<Integer> expectedResult) {
		this.raceName = raceName;
		this.raceDistance = raceDistance;
		this.horseCount = horseCount;
		this.raceType = raceType;
		this.racePlace = racePlace;
		this.expectedResult = expectedResult;
	}

	@Override
	public String getRaceName() {
		return raceName;
	}

	@Override
	public int getRaceDistance() {
		return raceDistance;
	}

	@Override
	public int getHorseCount() {
		return horseCount;
	}

	@Override
	public String getRaceType() {
		return raceType;
	}

	@Override
	public String getRacePlace() {
		return racePlace;
	}

	@Override
	public List<Integer> getExpectedResult() {
		return expectedResult;
	}
}
