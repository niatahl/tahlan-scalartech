package org.niatahl.scalartech.world;

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
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Spindle {

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Spindle");
        system.addTag(Tags.THEME_CORE_POPULATED);

        // Tart-style fake start location for procgen
        system.getLocation().set(5000,-5000);

        system.setBackgroundTextureFilename("graphics/tahlan/backgrounds/tahlan_spindle.jpg");

        PlanetAPI spindle_star = system.initStar("tahlan_spindle",
                "star_yellow",
                400f,
                700f);

        system.setLightColor(new Color(255, 252, 222));

        system.addRingBand(spindle_star, "misc", "rings_dust0", 256f, 2, Color.gray, 400f, 1600f, 200f);
        system.addRingBand(spindle_star, "misc", "rings_dust0", 256f, 3, Color.gray, 400f, 1800f, 200f);

        PlanetAPI spindle_1 = system.addPlanet("tahlan_spindle_p01",
                spindle_star,
                "Weft",
                "lava_minor",
                360f*(float)Math.random(),
                140,
                2600,
                160);

        PlanetConditionGenerator.generateConditionsForPlanet(spindle_1, StarAge.YOUNG);

        PlanetAPI spindle_2 = system.addPlanet("tahlan_spindle_p02",
                spindle_star,
                "Warp",
                "barren",
                360f*(float)Math.random(),
                180,
                4200,
                230);

        PlanetConditionGenerator.generateConditionsForPlanet(spindle_2, StarAge.YOUNG);

        SectorEntityToken stableLoc1 = system.addCustomEntity("tahlan_spindle_stableLoc1", "Stable Location", "stable_location", Factions.NEUTRAL);
        stableLoc1.setCircularOrbit(spindle_star, 360f*(float)Math.random(),3400, 400);

        system.addAsteroidBelt(spindle_star, 1500, 5500, 1500, 200, 600, Terrain.ASTEROID_BELT, "The Shawl");
        system.addRingBand(spindle_star, "misc", "rings_dust0", 256f, 1, Color.gray, 600f, 5200, 220);
        system.addRingBand(spindle_star, "misc", "rings_dust0", 256f, 0, Color.gray, 600f, 5800, 400);
        system.addRingBand(spindle_star, "misc", "rings_asteroids0", 256f, 0, Color.gray, 600f, 5400, 260);
        system.addRingBand(spindle_star, "misc", "rings_asteroids0", 256f, 1, Color.gray, 600, 5600, 320);

        float charkhaAngle = 360f*(float)Math.random();

        PlanetAPI spindle_3 = system.addPlanet("tahlan_spindle_charkha",
                spindle_star,
                "Charkha",
                "jungle_charkha",
                charkhaAngle,
                190,
                8500,
                300);

        spindle_3.setCustomDescriptionId("tahlan_planet_charkha");
        spindle_3.setInteractionImage("illustrations","tahlan_charkha_illus");

        JumpPointAPI jumpPointCharkha = Global.getFactory().createJumpPoint("tahlan_spindle_charkha_jump", "Spinner's Road");
        jumpPointCharkha.setCircularOrbit(spindle_star, charkhaAngle+30, 8000, 300);
        jumpPointCharkha.setRelatedPlanet(spindle_3);
        system.addEntity(jumpPointCharkha);

        MarketAPI spindle_3_market = addMarketplace("scalartech", spindle_3, null,
                "Charkha",
                7,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_7,
                                Conditions.HABITABLE,
                                Conditions.FARMLAND_ADEQUATE,
                                Conditions.ORGANICS_COMMON,
                                Conditions.ORE_SPARSE,
                                Conditions.INIMICAL_BIOSPHERE,
                                Conditions.REGIONAL_CAPITAL,
                                "tahlan_gatescar"
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.GENERIC_MILITARY,
                                "tahlan_stdfmarket",
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_STORAGE
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.MINING,
                                Industries.FARMING,
                                Industries.STARFORTRESS_HIGH,
                                Industries.HEAVYBATTERIES,
                                Industries.HIGHCOMMAND,
                                "tahlan_scalartechhq"
                        )
                ),
                0.3f,
                false,
                true);

        spindle_3_market.addIndustry(Industries.ORBITALWORKS,new ArrayList<String>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));
        spindle_3_market.getIndustry(Industries.HIGHCOMMAND).setAICoreId(Commodities.ALPHA_CORE);
        spindle_3_market.getIndustry(Industries.STARFORTRESS_HIGH).setAICoreId(Commodities.ALPHA_CORE);

        SectorEntityToken spindle_3_field = system.addTerrain(Terrain.MAGNETIC_FIELD,new MagneticFieldTerrainPlugin.MagneticFieldParams(200f, // terrain effect band width
                300f, // terrain effect middle radius
                spindle_3, // entity that it's around
                190f, // visual band start
                600f, // visual band end
                new Color(22, 95, 100, 50), // base color
                0.5f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                new Color(73, 180, 168),
                new Color(105, 190, 177),
                new Color(123, 210, 225),
                new Color(132, 198, 240),
                new Color(25, 93, 250),
                new Color(0, 19, 240),
                new Color(0, 3, 150)));

        spindle_3_field.setCircularOrbit(spindle_3,0f,0f,120);

        PlanetAPI spindle_3_moon = system.addPlanet("tahlan_spindle_p01",
                spindle_3,
                "Mika",
                "barren_mika",
                360f*(float)Math.random(),
                60,
                600,
                60);

        addConditionMarket(spindle_3_moon,new ArrayList<>(
                        Arrays.asList(Conditions.NO_ATMOSPHERE, Conditions.LOW_GRAVITY, Conditions.ORE_SPARSE))
                );

        spindle_3_moon.setCustomDescriptionId("tahlan_planet_mika");

        SectorEntityToken stableLoc2 = system.addCustomEntity("tahlan_spindle_stableLoc2", "Comm Relay", "comm_relay", Factions.NEUTRAL);
        stableLoc2.setCircularOrbit(spindle_star, 360f*(float)Math.random(),7200, 540);


        PlanetAPI spindle_4 = system.addPlanet("tahlan_spindle_jacquard",
                spindle_star,
                "Jacquard",
                "gas_giant",
                360f*(float)Math.random(),
                400,
                14000,
                420);

        spindle_4.setCustomDescriptionId("tahlan_planet_jacquard");
        spindle_4.setInteractionImage("illustrations","tahlan_jacquard_illus");

        MarketAPI spindle_4_market = addMarketplace("scalartech", spindle_4, null,
                "Jacquard",
                5,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.VOLATILES_DIFFUSE,
                                Conditions.ORE_SPARSE,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.HIGH_GRAVITY
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.GENERIC_MILITARY,
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
                                Industries.REFINING,
                                Industries.BATTLESTATION_HIGH,
                                Industries.GROUNDDEFENSES
                        )
                ),
                0.3f,
                true,
                true);

        system.addRingBand(spindle_4, "misc", "rings_asteroids0", 256f, 0, Color.gray, 300, 500, 200);

        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, spindle_star, StarAge.YOUNG,
                2, 3, // min/max entities to add
                15000, // radius to start adding at
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

        PlanetAPI spindle_5 = system.addPlanet("tahlan_spindle_ardor",
                spindle_star,
                "Ardor",
                "barren",
                360f*(float)Math.random(),
                220,
                radiusAfter+1500,
                510);

        spindle_5.setCustomDescriptionId("tahlan_planet_ardor");
        spindle_5.setInteractionImage("illustrations","tahlan_ardor_illus");

        MarketAPI spindle_5_market = addMarketplace(Factions.PIRATES, spindle_5, null,
                "Ardor",
                4,
                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.ORE_SPARSE,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.LOW_GRAVITY,
                                Conditions.DARK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_STORAGE
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.MINING,
                                Industries.ORBITALSTATION,
                                Industries.GROUNDDEFENSES,
                                Industries.PATROLHQ
                        )
                ),
                0.3f,
                true,
                true);

        float radiusAfter2 = StarSystemGenerator.addOrbitingEntities(system, spindle_star, StarAge.YOUNG,
                1, 2, // min/max entities to add
                radiusAfter+3000, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

        generateNebula(system, 14000);

        // generates hyperspace destinations for in-system jump points
        system.autogenerateHyperspaceJumpPoints(true, true);

        //Finally cleans up hyperspace - Now done after moving the system
        //cleanup(system);
    }

    protected void generateNebula(StarSystemAPI system, float holeRadius)
    {
        Random random = new Random(getStartingSeed());
        float w = 50000;
        float h = 50000;

        // First make a solid map-spanning nebula
        SectorEntityToken nebulaTiles = Misc.addNebulaFromPNG("data/campaign/terrain/tahlan_nebula_spindle.png",
                0, 0, // Center of nebula
                system, // Location to add to
                "terrain", "nebula", // Texture to use, uses xxx_map for map
                4, 4, Terrain.NEBULA, StarAge.YOUNG);

        nebulaTiles.getLocation().set(0, 0);

        BaseTiledTerrain nebula = getNebula(system);
        nebula.setTerrainName("Spinner's Thread");
        NebulaEditor editor = new NebulaEditor(nebula);

        // Donut hole
        editor.clearArc(0, 0, 0, holeRadius, 0, 360);

        // Do some random arcs
        // Taken from vanilla's SectorProcGen.java
        int numArcs = 0;

        for (int i = 0; i < numArcs; i++)
        {
            float dist = w / 2f + w / 2f * random.nextFloat();
            float angle = random.nextFloat() * 360f;

            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
            dir.scale(dist - (w / 12f + w / 3f * random.nextFloat()));

            float width = 800f * (1f + 2f * random.nextFloat());

            float clearThreshold = 0f + 0.5f * random.nextFloat();

            editor.clearArc(dir.x, dir.y, dist - width / 2f, dist + width / 2f, 0, 360f, clearThreshold);
        }

        // Clear planet orbit paths
        SectorEntityToken center = system.getCenter();
        for (PlanetAPI planet : system.getPlanets())
        {
            if (planet == center)
            {
                continue;
            }
            if (MathUtils.isWithinRange(center, planet, holeRadius - 3000))
            {
                continue;
            }
            float dist = MathUtils.getDistance(center, planet);
            float width = 2000 + planet.getRadius() * 4;
            float clearThreshold = 0f + 0.5f * random.nextFloat();
            editor.clearArc(0, 0, dist - width / 2f, dist + width / 2f, 0, 360f, clearThreshold);
        }

        // Noise
        editor.regenNoise();
        editor.noisePrune(0.6f);
        editor.regenNoise();
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

    //Shorthand for Conditions-only markets
    public static MarketAPI addConditionMarket(SectorEntityToken entity, ArrayList<String> marketConditions)
    {
        String planetID = entity.getId();
        String marketID = planetID + "_market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, entity.getName(), 0);
        newMarket.setPrimaryEntity(entity);
        newMarket.setPlanetConditionMarketOnly(true);
        entity.setMarket(newMarket);

        for (String condition : marketConditions)
        {
            newMarket.addCondition(condition);
        }
        return newMarket;
    }

    long getStartingSeed()
    {
        String seedStr = Global.getSector().getSeedString().replaceAll("[^0-9]", "");
        return Long.parseLong(seedStr);
    }


    float getRandomFloat(Random random, float min, float max)
    {
        return min + (max - min) * random.nextFloat();
    }

    BaseTiledTerrain getNebula(StarSystemAPI system)
    {
        for (CampaignTerrainAPI curr : system.getTerrainCopy())
        {
            if (curr.getPlugin().getTerrainId().equals(Terrain.NEBULA))
            {
                return (BaseTiledTerrain) (curr.getPlugin());
            }
        }
        return null;
    }
}
