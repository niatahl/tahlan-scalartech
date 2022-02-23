package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
//import exerelin.utilities.StringHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.input.Keyboard;


public class tahlan_SilvysTurnInScript extends PaginatedOptions {

    public static final String POINTS_KEY = "$nex_BPSwapPoints";
    public static final String STOCK_ARRAY_KEY = "$nex_BPSwapStock";
    public static final String ALREADY_SOLD_KEY = "$nex_BPSwapAlreadySold";
    public static final float PRICE_POINT_MULT = 0.0001f;
    public static final String PERSISTENT_RANDOM_KEY = "nex_blueprintSwapRandom";

    public static final String DIALOG_OPTION_PREFIX = "nex_blueprintSwap_pick_";

    protected static PurchaseInfo toPurchase = null;

    // Things that count as blueprints for trade-in
    public static final Set<String> ALLOWED_IDS = new HashSet<>(Arrays.asList(Items.SHIP_BP, Items.WEAPON_BP, Items.FIGHTER_BP, "industry_bp",
            "tiandong_retrofit_bp", "tiandong_retrofit_fighter_bp", "roider_retrofit_bp"));

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected MarketAPI market;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected CargoAPI playerCargo;
    protected PersonAPI person;
    protected FactionAPI faction;
    protected float points;
    protected List<String> disabledOpts = new ArrayList<>();

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
        setupVars(dialog, memoryMap);

        switch (arg) {
            case "check":
                return checkBP();
            case "sell":
                selectBPs();
                break;
        }

        return true;
    }

    /**
     * To be called only when paginated dialog options are required.
     * Otherwise we get nested dialogs that take multiple clicks of the exit option to actually exit.
     *
     * @param dialog
     */
    protected void setupDelegateDialog(InteractionDialogAPI dialog) {
        originalPlugin = dialog.getPlugin();

        dialog.setPlugin(this);
        init(dialog);
    }

    protected void setupVars(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        entity = dialog.getInteractionTarget();
        market = entity.getMarket();
        text = dialog.getTextPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        person = dialog.getInteractionTarget().getActivePerson();
        faction = person.getFaction();
    }

    @Override
    public void showOptions() {
        super.showOptions();
        for (String optId : disabledOpts) {
            dialog.getOptionPanel().setEnabled(optId, false);
        }
        dialog.getOptionPanel().setShortcut("nex_blueprintSwapMenuReturn", Keyboard.KEY_ESCAPE, false, false, false, false);
    }

    protected void selectBPs() {
        final CargoAPI copy = Global.getFactory().createCargo(false);

        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            if (isBlueprints(stack)) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        // prevents an IllegalAccessError
        final InteractionDialogAPI dialog = this.dialog;
        final Map<String, MemoryAPI> memoryMap = this.memoryMap;

        dialog.showCargoPickerDialog("Select blueprints to trade in", //StringHelper.getString("exerelin_misc", "blueprintSwapSelect"),
                Misc.ucFirst("confirm"),//Misc.ucFirst(StringHelper.getString("confirm")),
                Misc.ucFirst("cancel"),//Misc.ucFirst(StringHelper.getString("cancel")),
                true, width, copy, new CargoPickerListener() {
                    public void pickedCargo(CargoAPI cargo) {
                        cargo.sort();
                        for (CargoStackAPI stack : cargo.getStacksCopy()) {
                            playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
                            if (stack.isCommodityStack()) { // should be always, but just in case
                                AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
                            }
                        }

                        int points = (int) getPointValue(cargo);



                        if (points >= 1f) {
                            CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
                            impact.delta = points * 0.01f;
                            if (impact.delta >= 0.01f) {
                                Global.getSector().adjustPlayerReputation(
                                        new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, impact,
                                                null, text, true),
                                        person);
                            }
                            impact = new CoreReputationPlugin.CustomRepImpact();
                            impact.delta = points * 0.005f;
                            if (impact.delta >= 0.01f) {
                                Global.getSector().adjustPlayerReputation(
                                        new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, impact,
                                                null, text, true),
                                        faction.getId());
                            }
                        }

                        FireBest.fire(null, dialog, memoryMap, "tahlan_BPTurnedInSilvys");
                    }

                    @Override
                    public void cancelledCargoSelection() {
                    }

                    @Override
                    public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

                        int points = (int) getPointValue(cargo);

                        float pad = 3f;
                        float opad = 10f;

                        panel.setParaOrbitronLarge();
                        panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), opad);
                        panel.setParaFontDefault();
                        panel.addImage(faction.getLogo(), width, pad);
                        panel.addPara("If you turn in the selected blueprints, your standing with " + person.getName().getFullName() + " will improve by %s points.",
                                opad, Misc.getHighlightColor(),
                                "" + points);
                    }
                });
    }

    public static boolean isBlueprints(CargoStackAPI stack) {
        SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
        if (spec == null) return false;
        String id = spec.getId();
        return spec.hasTag("package_bp") || ALLOWED_IDS.contains(id);
    }

    public static float getPointValue(CargoAPI cargo) {
        float totalPoints = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (!isBlueprints(stack)) continue;

            float points = getPointValue(stack);

            totalPoints += points;
        }
        return totalPoints;
    }

    public static float getPointValue(CargoStackAPI stack) {
        SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
        SpecialItemData data = stack.getSpecialDataIfSpecial();
        float points, base = 0;

        switch (spec.getId()) {
            case Items.SHIP_BP:
            case "tiandong_retrofit_bp":
            case "roider_retrofit_bp":
                base = Global.getSettings().getHullSpec(data.getData()).getBaseValue();
                break;
            case Items.FIGHTER_BP:
            case "tiandong_retrofit_fighter_bp":
                base = Global.getSettings().getFighterWingSpec(data.getData()).getBaseValue();
                break;
            case Items.WEAPON_BP:
                base = Global.getSettings().getWeaponSpec(data.getData()).getBaseValue();
                break;
        }
        points = getBlueprintPointValue(spec.getId(), data.getData(), base);

        points *= stack.getSize();

        return points;
    }

    /**
     * Gets the point value of a blueprint based on its sale price.
     *
     * @param itemId   e.g. "fighter_bp", Items.SHIP_BP
     * @param dataId   e.g. "onslaught_XIV"
     * @param baseCost Base cost of the hull, fighter wing or weapon
     * @return
     */
    public static float getBlueprintPointValue(String itemId, String dataId, float baseCost) {

        SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(itemId);

        float cost = spec.getBasePrice() + baseCost * Global.getSettings().getFloat("blueprintPriceOriginalItemMult") * 0.25f;
        if (spec.hasTag("tiandong_retrofit_bp") || itemId.equals("roider_retrofit_bp")) {
            cost *= 0.5f;
        }
        cost *= PRICE_POINT_MULT;
        if (spec.hasTag("package_bp"))
            cost *= 5;

        // rounding
        cost = 5 * Math.round(cost / 5f);

        return cost;
    }

    public static class PurchaseInfo implements Comparable<PurchaseInfo> {
        public String id;
        public String itemId;
        public PurchaseType type;
        public String name;
        public float cost;

        public PurchaseInfo(String id, PurchaseType type, String name, float cost) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.cost = cost;
        }

        public String getItemId() {
            if (itemId != null) return itemId;
            switch (type) {
                case SHIP:
                    return Items.SHIP_BP;
                case FIGHTER:
                    return Items.FIGHTER_BP;
                case WEAPON:
                    return Items.WEAPON_BP;
            }
            return null;
        }

        public SpecialItemData getItemData() {
            return new SpecialItemData(getItemId(), id);
        }

        @Override
        public int compareTo(PurchaseInfo other) {
            // ships first, then fighters, then weapons
            if (type != other.type)
                return type.compareTo(other.type);

            // descending cost order
            if (cost != other.cost) return Float.compare(other.cost, cost);

            return name.compareTo(other.name);
        }
    }

    public enum PurchaseType {
        SHIP, FIGHTER, WEAPON
    }

    protected boolean checkBP() {
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            if (isBlueprints(stack)) {
                return true;
            }
        }
        return false;
    }
}
