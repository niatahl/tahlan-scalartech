package org.niatahl.scalartech.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;


public class ScalarRelationPlugin implements SectorGeneratorPlugin {

    //Just call initFactionRelationships: this is only intended as a means to set faction relations at start
    @Override
    public void generate(SectorAPI sector) {
        initFactionRelationships(sector);
    }

    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI scalartech = sector.getFaction("scalartech");

        //but not pirates and dabble
        scalartech.setRelationship("pirates",-0.6f);
        scalartech.setRelationship("diableavionics", 0.4f);
        scalartech.setRelationship("hegemony", -0.3f);
        scalartech.setRelationship("sylphon", 0.1f);
        scalartech.setRelationship("luddic_path", -0.6f);
        scalartech.setRelationship("luddic_church", -0.2f);
        scalartech.setRelationship("fpe", -0.6f);
        scalartech.setRelationship("blackrock_driveyards", -0.5f);
        scalartech.setRelationship("dassault_mikoyan", -0.6f);
        scalartech.setRelationship("aic", 0.4f);
        scalartech.setRelationship("brighton", 0.5f);
        scalartech.setRelationship("hmi", -0.3f);
        scalartech.setRelationship("interstellarimperium", 0.1f);
        scalartech.setRelationship("kadur_remnant", 0.1f);
        scalartech.setRelationship("al_ars", -0.1f);
        scalartech.setRelationship("ora", 0.4f);
        scalartech.setRelationship("shadow_industry", 0.4f);
        scalartech.setRelationship("tiandong", 0.3f);
        scalartech.setRelationship("remnant", -0.6f);

    }
}
