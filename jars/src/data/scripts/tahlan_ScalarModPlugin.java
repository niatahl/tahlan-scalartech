package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import data.scripts.ai.tahlan_emptorpedo_ai;
import data.scripts.world.tahlan_ScalarRelationPlugin;
import data.scripts.world.tahlan_Spindle;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class tahlan_ScalarModPlugin extends BaseModPlugin {
    static private boolean graphicsLibAvailable = false;
    static public boolean isGraphicsLibAvailable () {
        return graphicsLibAvailable;
    }
    //All hullmods related to shields, saved in a convenient list
    public static List<String> SHIELD_HULLMODS = new ArrayList<String>();

    public static final String EMPMISSILE_ID = "tahlan_tear_msl";

    public static Logger log = Global.getLogger(tahlan_ScalarModPlugin.class);

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
            new tahlan_Spindle().generate(sector);
        }
        if (!haveNexerelin) {
            tahlan_ScalarRelationPlugin.initFactionRelationships(sector);
        }

        //Adding ScalarTech to bounty system
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("scalartech");

    }

    @Override
    public void onNewGameAfterProcGen() {
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        MarketAPI market = Global.getSector().getEconomy().getMarket("tahlan_spindle_charkha_market");
        if (market != null) {
            log.info("Adding admin");
            PersonAPI admin = Global.getFactory().createPerson();
            admin.setFaction("scalartech");
            admin.setGender(FullName.Gender.FEMALE);
            admin.setPostId(Ranks.POST_FACTION_LEADER);
            admin.setRankId(Ranks.FACTION_LEADER);
            admin.getName().setFirst("Yurika");
            admin.getName().setLast("Kusanagi");
            admin.setPortraitSprite("graphics/tahlan/portraits/yurika.png");

            //admin.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
            //admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
            //admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);

            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin, 0);
            market.addPerson(admin);
        }
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case EMPMISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new tahlan_emptorpedo_ai(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
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

    private static void loadTahlanSettings() throws IOException, JSONException {
    }
}