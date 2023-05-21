package org.niatahl.scalartech.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

//Nabbed this from Alfonzo, madman extraordinaire
//Original code by DR, mad god of the modiverse
//Further modified by Nia, also a bit mad
public class GownModule extends BaseHullMod {


    private static void advanceChild(ShipAPI child, ShipAPI parent) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            if (parent.isAlive()) {
                if (ec.isAccelerating()) {
                    child.giveCommand(ShipCommand.ACCELERATE, null, 0);
                }
                if (ec.isAcceleratingBackwards()) {
                    child.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
                }
                if (ec.isDecelerating()) {
                    child.giveCommand(ShipCommand.DECELERATE, null, 0);
                }
                if (ec.isStrafingLeft()) {
                    child.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
                }
                if (ec.isStrafingRight()) {
                    child.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
                }
                if (ec.isTurningLeft()) {
                    child.giveCommand(ShipCommand.TURN_LEFT, null, 0);
                }
                if (ec.isTurningRight()) {
                    child.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
                }
            }

            if (parent.getSystem() != null) {
                ShipSystemAPI system = parent.getSystem();
                if (system.getId().contains("supercruise") && system.isActive()) {
                    child.getEngineController().extendFlame("tahlan_supercruise_module", 1.2f, 1f, 1f);
                    child.getEngineController().fadeToOtherColor("tahlan_supercruise_module", new Color(255, 0, 100), null, system.getEffectLevel(), 0.7f);
                }
            }

            ShipEngineControllerAPI cec = child.getEngineController();
            if (cec != null) {
                if ((ec.isFlamingOut() || ec.isFlamedOut()) && !cec.isFlamingOut() && !cec.isFlamedOut()) {
                    child.getEngineController().forceFlameout(true);
                }
            }


        }


        if (parent.getVariant().hasHullMod("unstableinjector")) {
            child.getMutableStats().getBallisticWeaponRangeBonus().modifyMult("tahlan_module_ui", 0.85f);
            child.getMutableStats().getEnergyWeaponRangeBonus().modifyMult("tahlan_module_ui", 0.85f);
            child.getMutableStats().getFighterRefitTimeMult().modifyPercent("tahlan_module_ui", 25f);
        } else {
            child.getMutableStats().getBallisticWeaponRangeBonus().unmodify("tahlan_module_ui");
            child.getMutableStats().getEnergyWeaponRangeBonus().unmodify("tahlan_module_ui");
            child.getMutableStats().getFighterRefitTimeMult().unmodify("tahlan_module_ui");
        }

        if (parent.getShipTarget() != null) {
            child.setShipTarget(parent.getShipTarget());

        }

        //Vent mirroring. This code is so fucky but hey, you do what works

        if (parent.getFluxTracker().isVenting()) {
            child.getMutableStats().getVentRateMult().unmodify("tahlan_supercruise_module");
            child.giveCommand(ShipCommand.VENT_FLUX, null, 0);
        } else if (!child.getFluxTracker().isVenting()) {
            child.getMutableStats().getVentRateMult().modifyMult("tahlan_supercruise_module", 0f);
        }


        if (parent.getSystem() != null) {
            ShipSystemAPI system = parent.getSystem();
            if (system.getId().contains("supercruise") && system.isActive()) {
                child.getMutableStats().getBallisticWeaponRangeBonus().modifyMult("tahlan_supercruise_module", 1f - 0.33f * system.getEffectLevel());
                child.getMutableStats().getEnergyWeaponRangeBonus().modifyMult("tahlan_supercruise_module", 1f - 0.33f * system.getEffectLevel());
                child.getMutableStats().getBallisticRoFMult().modifyMult("tahlan_supercruise_module", 1f - 0.33f * system.getEffectLevel());
                child.getMutableStats().getEnergyWeaponDamageMult().modifyMult("tahlan_supercruise_module", 1f - 0.33f * system.getEffectLevel());
                child.getMutableStats().getEnergyWeaponFluxCostMod().modifyMult("tahlan_supercruise_module", 1f - 0.33f * system.getEffectLevel());
                child.getMutableStats().getFluxDissipation().modifyPercent("tahlan_supercruise_module", -33f * system.getEffectLevel());
            } else {
                child.getMutableStats().getEnergyWeaponRangeBonus().unmodify("tahlan_supercruise_module");
                child.getMutableStats().getBallisticWeaponRangeBonus().unmodify("tahlan_supercruise_module");
                child.getMutableStats().getBallisticRoFMult().unmodify("tahlan_supercruise_module");
                child.getMutableStats().getEnergyWeaponDamageMult().unmodify("tahlan_supercruise_module");
                child.getMutableStats().getEnergyWeaponFluxCostMod().unmodify("tahlan_supercruise_module");
                child.getMutableStats().getFluxDissipation().unmodify("tahlan_supercruise_module");
            }
        }

        /* Mirror parent's fighter commands */
        if (child.hasLaunchBays()) {
            if (child.isPullBackFighters() ^ parent.isPullBackFighters()) {
                child.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
            }
            if (child.getAIFlags() != null) {
                if (((Global.getCombatEngine().getPlayerShip() == parent) || (parent.getAIFlags() == null))
                        && (parent.getShipTarget() != null)) {
                    child.getAIFlags().setFlag(AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getShipTarget());
                } else if ((parent.getAIFlags() != null)
                        && parent.getAIFlags().hasFlag(AIFlags.CARRIER_FIGHTER_TARGET)
                        && (parent.getAIFlags().getCustom(AIFlags.CARRIER_FIGHTER_TARGET) != null)) {
                    child.getAIFlags().setFlag(AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getAIFlags().getCustom(AIFlags.CARRIER_FIGHTER_TARGET));
                }
            }
        }

        //Fucky 0-flux boost mirroring that mostly works
        if (parent.getFluxLevel() > parent.getMutableStats().getZeroFluxMinimumFluxLevel().getModifiedValue()) {
            child.getMutableStats().getZeroFluxMinimumFluxLevel().modifyFlat("zerofluxmirror", -2f);
        } else {
            child.getMutableStats().getZeroFluxMinimumFluxLevel().modifyFlat("zerofluxmirror", 2f);
        }

        //Overload module with parent
        if (parent.getFluxTracker().isOverloaded()) {
            if (!child.getFluxTracker().isOverloaded()) {
                child.getFluxTracker().forceOverload(parent.getFluxTracker().getOverloadTimeRemaining());
            } else {
                if (child.getFluxTracker().getOverloadTimeRemaining() < parent.getFluxTracker().getOverloadTimeRemaining()) {
                    child.getFluxTracker().forceOverload(parent.getFluxTracker().getOverloadTimeRemaining()-child.getFluxTracker().getOverloadTimeRemaining());
                }
            }
        }

    }

    private static void advanceParent(ShipAPI parent, List<ShipAPI> children) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            float originalMass;
            int originalEngines;
            switch (parent.getHullSpec().getBaseHullId()) {
                default:
                case "tahlan_gown":
                    originalMass = 4500f;
                    originalEngines = 22;
                    break;
            }
            float thrustPerEngine = originalMass / originalEngines;

            /* Don't count parent's engines for this stuff - game already affects stats */
            float workingEngines = ec.getShipEngines().size();
            for (ShipAPI child : children) {
                if ((child.getParentStation() == parent) && (child.getStationSlot() != null) && child.isAlive()) {
                    ShipEngineControllerAPI cec = child.getEngineController();
                    if (cec != null) {
                        float contribution = 0f;
                        for (ShipEngineAPI ce : cec.getShipEngines()) {
                            if (ce.isActive() && !ce.isDisabled() && !ce.isPermanentlyDisabled() && !ce.isSystemActivated()) {
                                contribution += ce.getContribution();
                            }
                        }
                        workingEngines += cec.getShipEngines().size() * contribution;
                    }
                }
            }

            float thrust = workingEngines * thrustPerEngine;
            float enginePerformance = thrust / Math.max(1f, parent.getMassWithModules());
            parent.getMutableStats().getAcceleration().modifyMult("tahlan_gownmodule", enginePerformance);
            parent.getMutableStats().getDeceleration().modifyMult("tahlan_gownmodule", enginePerformance);
            parent.getMutableStats().getTurnAcceleration().modifyMult("tahlan_gownmodule", enginePerformance);
            parent.getMutableStats().getMaxTurnRate().modifyMult("tahlan_gownmodule", enginePerformance);
            parent.getMutableStats().getMaxSpeed().modifyMult("tahlan_gownmodule", enginePerformance);
            parent.getMutableStats().getZeroFluxSpeedBoost().modifyMult("tahlan_gownmodule", enginePerformance);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ShipAPI parent = ship.getParentStation();
        if (parent != null) {
            advanceChild(ship, parent);
        }

        List<ShipAPI> children = ship.getChildModulesCopy();
        if (children != null && !children.isEmpty()) {
            advanceParent(ship, children);
        }
    }

    private static final Set<String> BLOCKED_FRONT = new HashSet<>();
    private static final Set<String> BLOCKED_OTHER = new HashSet<>();
    private static final Set<String> BLOCKED_OTHER_PLAYER_ONLY = new HashSet<>();

    static {
        /* No shields on my modules */
        BLOCKED_FRONT.add("frontemitter");
        BLOCKED_FRONT.add("frontshield");
        BLOCKED_FRONT.add("adaptiveshields");

        /* Modules don't move on their own */
        BLOCKED_OTHER.add("auxiliarythrusters");
        BLOCKED_OTHER.add("unstable_injector");

        /* Module's can't provide ECM/Nav */
        BLOCKED_OTHER.add("ecm");
        BLOCKED_OTHER.add("nav_relay");

        /* Logistics mods partially or completely don't apply on modules */
        BLOCKED_OTHER.add("operations_center");
        BLOCKED_OTHER.add("recovery_shuttles");
        BLOCKED_OTHER.add("additional_berthing");
        BLOCKED_OTHER.add("augmentedengines");
        BLOCKED_OTHER.add("auxiliary_fuel_tanks");
        BLOCKED_OTHER.add("efficiency_overhaul");
        BLOCKED_OTHER.add("expanded_cargo_holds");
        BLOCKED_OTHER.add("hiressensors");
        //BLOCKED_OTHER.add("insulatedengine"); // Niche use
        BLOCKED_OTHER.add("militarized_subsystems");
        //BLOCKED_OTHER.add("solar_shielding"); // Niche use
        BLOCKED_OTHER.add("surveying_equipment");

        /* Crew penalty doesn't reflect in campaign */
        BLOCKED_OTHER_PLAYER_ONLY.add("converted_hangar");
        //BLOCKED_OTHER_PLAYER_ONLY.add("expanded_deck_crew");
        BLOCKED_OTHER_PLAYER_ONLY.add("TSC_converted_hangar");
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        switch (ship.getHullSpec().getBaseHullId()) {
            case "tahlan_gown_ringleft":
            case "tahlan_gown_ringright":
                for (String tmp : BLOCKED_FRONT) {
                    if (ship.getVariant().getHullMods().contains(tmp)) {
                        MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "tahlan_gownmodule");
                    }
                }
                break;
            default:
                break;
        }
        switch (ship.getHullSpec().getBaseHullId()) {
            case "tahlan_gown_ringleft":
            case "tahlan_gown_ringright":
                for (String tmp : BLOCKED_OTHER) {
                    if (ship.getVariant().getHullMods().contains(tmp)) {
                        MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "tahlan_gownmodule");
                    }
                }
                break;
            default:
                break;
        }
        switch (ship.getHullSpec().getBaseHullId()) {
            case "tahlan_gown_ringleft":
            case "tahlan_gown_ringright":
                for (String tmp : BLOCKED_OTHER_PLAYER_ONLY) {
                    if (ship.getVariant().getHullMods().contains(tmp)) {
                        MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "tahlan_gownmodule");
                    }
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addPara("%s " + txt("gownModule_01"),5f, Misc.getHighlightColor(),">");
        tooltip.addPara("%s " + txt("gownModule_02"),5f, Misc.getHighlightColor(),">");
        tooltip.addPara("%s " + txt("gownModule_03"),5f, Misc.getHighlightColor(),">");
        tooltip.addPara("%s " + txt("gownModule_04"),5f, Misc.getHighlightColor(),">");
    }
}
