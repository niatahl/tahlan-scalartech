package org.niatahl.scalartech;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import com.fs.starfarer.api.util.Misc;
import org.niatahl.scalartech.ai.EMPtorpedoAI;
import org.niatahl.scalartech.world.ScalarRelationPlugin;
import org.niatahl.scalartech.world.Spindle;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class ScalarModPlugin extends BaseModPlugin {
    static private boolean graphicsLibAvailable = false;
    static public boolean isGraphicsLibAvailable () {
        return graphicsLibAvailable;
    }
    //All hullmods related to shields, saved in a convenient list
    public static List<String> SHIELD_HULLMODS = new ArrayList<String>();

    public static final String EMPMISSILE_ID = "tahlan_tear_msl";

    public static Logger log = Global.getLogger(ScalarModPlugin.class);

    @Override
    public void onApplicationLoad() {
        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!hasLazyLib) {
            throw new RuntimeException("ScalarTech Solutions requires LazyLib by LazyWizard"  + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444");
        }
        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        if (!hasMagicLib) {
            throw new RuntimeException("ScalarTech Solutions requires MagicLib!"  + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718");
        }
        if (Global.getSettings().getModManager().isModEnabled("@_ss_rebal_@"))
            throw new RuntimeException("ScalarTech Solutions is incompatible with Starsector Rebal. It breaks everything");

        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            graphicsLibAvailable = true;
            ShaderLib.init();
            LightData.readLightDataCSV("data/lights/tahlan_scalar_lights.csv");
            TextureData.readTextureDataCSV("data/lights/tahlan_scalar_texture.csv");
        } else {
            graphicsLibAvailable = false;
        }

        //Adds shield hullmods
        for (HullModSpecAPI hullModSpecAPI : Global.getSettings().getAllHullModSpecs()) {
            if (hullModSpecAPI.hasTag("shields") && !SHIELD_HULLMODS.contains(hullModSpecAPI.getId())) {
                SHIELD_HULLMODS.add(hullModSpecAPI.getId());
            } else if (hullModSpecAPI.getId().contains("swp_shieldbypass") && !SHIELD_HULLMODS.contains(hullModSpecAPI.getId())) {
                SHIELD_HULLMODS.add("swp_shieldbypass"); //Dirty fix for Shield Bypass, since that one is actually not tagged as a Shield mod, apparently
            }
        }
    }


    //New game stuff
    @Override
    public void onNewGame() {
        SectorAPI sector = Global.getSector();

        //If we have Nexerelin and random worlds enabled, don't spawn our manual systems
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getManager().isCorvusMode()){
            new Spindle().generate(sector);
        }

        ScalarRelationPlugin.initFactionRelationships(sector);

        //Adding ScalarTech to bounty system
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("scalartech");

    }

    @Override
    public void onNewGameAfterProcGen() {
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        MarketAPI market = Global.getSector().getEconomy().getMarket("tahlan_spindle_charkha_market");
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        if (market != null) {
            log.info("Adding admin");
            PersonAPI admin = Global.getFactory().createPerson();
            admin.setId("scalar_Yurika");
            admin.setFaction("scalartech");
            admin.setGender(FullName.Gender.FEMALE);
            admin.setPostId(Ranks.POST_FACTION_LEADER);
            admin.setRankId(Ranks.FACTION_LEADER);
            admin.getName().setFirst("Yurika");
            admin.getName().setLast("Kusanagi");
            admin.setImportance(PersonImportance.VERY_HIGH);
            admin.setPersonality(Personalities.CAUTIOUS);
            admin.setVoice(Voices.BUSINESS);
            admin.setPortraitSprite("graphics/tahlan/portraits/yurika.png");

            admin.getMemoryWithoutUpdate().set("$nex_preferredAdmin", true);
            admin.getMemoryWithoutUpdate().set("$nex_preferredAdmin_factionId", "scalartech");
            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
            admin.getStats().setLevel(1);

            ip.addPerson(admin);
            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin, 0);
            market.addPerson(admin);

            PersonAPI silvys = Global.getFactory().createPerson();
            silvys.setId("scalar_Silvys");
            silvys.setFaction("scalartech");
            silvys.setGender(FullName.Gender.FEMALE);
            silvys.setPostId("scalar_headResearch");
            silvys.setRankId("scalar_headResearch");
            silvys.getName().setFirst("Silvys");
            silvys.getName().setLast("Renham");
            silvys.setPortraitSprite("graphics/tahlan/portraits/silvys.png");
            silvys.setPersonality(Personalities.STEADY);
            silvys.setVoice(Voices.SCIENTIST);
            silvys.setImportance(PersonImportance.VERY_HIGH);
            silvys.addTag(Tags.CONTACT_SCIENCE);
            silvys.addTag(Tags.CONTACT_MILITARY);


            silvys.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
            silvys.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
            silvys.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            silvys.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
            silvys.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
            silvys.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
            silvys.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
            silvys.getStats().setLevel(7);

            ip.addPerson(silvys);
            market.getCommDirectory().addPerson(silvys,1);
            market.addPerson(silvys);

            // Move Spindle where it's supposed to be
            market.getStarSystem().getLocation().set(22000,-14000);
            cleanup(market.getStarSystem());
        }

    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case EMPMISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new EMPtorpedoAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (!SharedData.getData().getPersonBountyEventData().isParticipating("scalartech")) {
            SharedData.getData().getPersonBountyEventData().addParticipatingFaction("scalartech");
        }
    }

    //Shorthand function for cleaning up hyperspace
    private void cleanup(StarSystemAPI system){
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0f, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0f, 360f, 0.25f);
    }
}