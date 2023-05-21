package org.niatahl.scalartech.campaign.nexerelin;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import exerelin.ExerelinConstants;
import exerelin.campaign.intel.specialforces.namer.SpecialForcesNamer;
import exerelin.utilities.StringHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static exerelin.utilities.NexUtils.JSONArrayToArrayList;
import static exerelin.utilities.NexUtils.getRandomListElement;

public class ScalartechNamer implements SpecialForcesNamer {
	
	public static final String FILE_PATH = "data/config/exerelin/specialForcesNames.json";
	
	public static final List<String> NAMES_FIRST = new ArrayList<>();
	public static final List<String> NAMES_SECOND = new ArrayList<>();
	public static final List<String> PREPOSITIONS = new ArrayList<>();
	public static final String FORMAT;
	
	static {
		try {
			JSONObject json = Global.getSettings().getMergedJSONForMod(FILE_PATH, ExerelinConstants.MOD_ID);
			JSONArray names1 = json.getJSONArray("scalartech_names1");
			JSONArray names2 = json.getJSONArray("scalartech_names2");
			JSONArray prepos = json.getJSONArray("scalartech_prepositions");
			NAMES_FIRST.addAll(JSONArrayToArrayList(names1));
			NAMES_SECOND.addAll(JSONArrayToArrayList(names2));
			PREPOSITIONS.addAll(JSONArrayToArrayList(prepos));
			FORMAT = json.getString("scalartech_nameFormat");
		}
		catch (IOException | JSONException ex) {
			throw new RuntimeException("Failed to load ScalarTech special forces namer", ex);
		}
	}

	@Override
	public String getFleetName(CampaignFleetAPI fleet, MarketAPI origin, PersonAPI commander) {
		String one = getRandomListElement(NAMES_FIRST);
		String two = getRandomListElement(NAMES_SECOND);
		String prepos = getRandomListElement(PREPOSITIONS);
		
		String name = StringHelper.substituteToken(FORMAT, "$one", one);
		name = StringHelper.substituteToken(name, "$preposition", prepos);
		name = StringHelper.substituteToken(name, "$two", two);
		
		return name;
	}
}
