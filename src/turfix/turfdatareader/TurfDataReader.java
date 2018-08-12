package turfix.turfdatareader;

import java.util.List;

import turfix.turfdatareader.model.IRaceResult;

public interface TurfDataReader {

	List<IRaceResult> readDataFromTurfWebSite();
}
