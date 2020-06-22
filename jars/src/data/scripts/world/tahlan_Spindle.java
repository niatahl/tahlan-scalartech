package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class tahlan_Spindle {

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Spindle");

        system.getLocation().set(22000,-10000);

        system.setBackgroundTextureFilename("graphics/tahlan/backgrounds/tahlan_spindle.jpg");

        PlanetAPI spindle_star = system.initStar("tahlan_spindle",
                "star_yellow",
                400f,
                700f);

        system.setLightColor(new Color(255, 252, 222));

        system.addRingBand(spindle_star, "misc", "rings_dust0", 256f, 2, Color.gray, 400f, 1200f, 200f);

        PlanetAPI spindle_1 = system.addPlanet("tahlan_spindle_p01",
                spindle_star,
                "Weft",
                "lava_minor",
                360f*(float)Math.random(),
                140,
                1600,
                160);

        PlanetConditionGenerator.generateConditionsForPlanet(spindle_1, StarAge.YOUNG);

        PlanetAPI spindle_2 = system.addPlanet("tahlan_spindle_p02",
                spindle_star,
                "Warp",
                "barren",
                360f*(float)Math.random(),
                180,
                2400,
                230);

        PlanetConditionGenerator.generateConditionsForPlanet(spindle_2, StarAge.YOUNG);

        SectorEntityToken stableLoc1 = system.addCustomEntity("tahlan_spindle_stableLoc1", "Stable Location", "stable_location", Factions.NEUTRAL);
        stableLoc1.setCircularOrbit(spindle_star, 360f*(float)Math.random(),3000, 400);

        system.addAsteroidBelt(spindle_star, 1500, 3800, 1500, 200, 600, Terrain.ASTEROID_BELT, "The Shawl");
        system.addRingBand(spindle_star, "misc", "rings_dust0", 256f, 1, Color.gray, 300f, 3200, 220);
        system.addRingBand(spindle_star, "misc", "rings_dust0", 256f, 0, Color.gray, 300f, 3600, 400);
        system.addRingBand(spindle_star, "misc", "rings_asteroids0", 256f, 0, Color.gray, 400, 4000, 260);
        system.addRingBand(spindle_star, "misc", "rings_asteroids0", 256f, 1, Color.gray, 400, 4600, 320);

        PlanetAPI spindle_3 = system.addPlanet("tahlan_spindle_p03",
                spindle_star,
                "Charkha",
                "jungle_charkha",
                360f*(float)Math.random(),
                200,
                5000,
                300);

        spindle_3.setCustomDescriptionId("tahlan_planet_charkha");

        MarketAPI spindle_3_market = addMarketplace("scalartech", spindle_3, null,
                "Charkha",
                7,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_7,
                                Conditions.HABITABLE,
                                Conditions.FARMLAND_ADEQUATE,
                                Conditions.ORGANICS_ABUNDANT,
                                Conditions.ORE_SPARSE,
                                Conditions.INIMICAL_BIOSPHERE,
                                Conditions.REGIONAL_CAPITAL,
                                Conditions.IRRADIATED
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_STORAGE
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.MINING,
                                Industries.ORBITALWORKS,
                                Industries.FARMING,
                                Industries.STARFORTRESS_HIGH,
                                Industries.HEAVYBATTERIES,
                                Industries.HIGHCOMMAND,
                                Industries.WAYSTATION
                        )
                ),
                0.3f,
                true,
                true);


        SectorEntityToken stableLoc2 = system.addCustomEntity("tahlan_spindle_stableLoc2", "Stable Location", "stable_location", Factions.NEUTRAL);
        stableLoc1.setCircularOrbit(spindle_star, 360f*(float)Math.random(),5600, 540);


        PlanetAPI spindle_4 = system.addPlanet("tahlan_spindle_p04",
                spindle_star,
                "Jacquard",
                "gas_giant",
                360f*(float)Math.random(),
                450,
                5600,
                420);

        spindle_4.setCustomDescriptionId("tahlan_planet_jacquard");

        MarketAPI spindle_4_market = addMarketplace("scalartech", spindle_3, null,
                "Charkha",
                4,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.VOLATILES_DIFFUSE,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.HIGH_GRAVITY,
                                Conditions.
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_STORAGE
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.MINING,
                                Industries.LIGHTINDUSTRY,
                                Industries.ORBITALWORKS,
                                Industries.FARMING,
                                Industries.STARFORTRESS_HIGH,
                                Industries.HEAVYBATTERIES,
                                Industries.PATROLHQ,
                                Industries.WAYSTATION
                        )
                ),
                0.3f,
                true,
                true);


        // generates hyperspace destinations for in-system jump points
        system.autogenerateHyperspaceJumpPoints(true, true);

        //Finally cleans up hyperspace
        cleanup(system);
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


    //Shorthand function for adding a market
    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name,
                                           int size, ArrayList<String> marketConditions, ArrayList<String> submarkets, ArrayList<String> industries, float tarrif,
                                           boolean freePort, boolean withJunkAndChatter) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID + "_market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", tarrif);

        //Adds submarkets
        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        //Adds market conditions
        for (String condition : marketConditions) {
            newMarket.addCondition(condition);
        }

        //Add market industries
        for (String industry : industries) {
            newMarket.addIndustry(industry);
        }

        //Sets us to a free port, if we should
        newMarket.setFreePort(freePort);

        //Adds our connected entities, if any
        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, withJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        //Finally, return the newly-generated market
        return newMarket;
    }

    //Shorthand for adding derelicts, thanks Tart
    protected void addDerelict(StarSystemAPI system, SectorEntityToken focus, String variantId,
                               ShipRecoverySpecial.ShipCondition condition, float orbitRadius, boolean recoverable) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
        ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
    }
}
