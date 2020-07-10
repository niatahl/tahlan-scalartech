package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;


public class tahlan_ScalarRelationPlugin implements SectorGeneratorPlugin {

    //Just call initFactionRelationships: this is only intended as a means to set faction relations at start
    @Override
    public void generate(SectorAPI sector) {
        initFactionRelationships(sector);
    }

    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI scalartech = sector.getFaction("scalartech");

        //but not pirates and dabble
        scalartech.setRelationship("pirates",-0.6f);
        scalartech.setRelationship("diableavionics", 0.5f);
        scalartech.setRelationship("hegemony", -0.3f);
        scalartech.setRelationship("sylphon", 0.1f);
        scalartech.setRelationship("luddic_path", -0.6f);
        scalartech.setRelationship("luddic_church", -0.2f);
    }
}
