package org.niatahl.scalartech.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static org.niatahl.scalartech.utils.Scalar_txt.txt;

public class GateScar extends BaseMarketConditionPlugin {

    public static final float ACCESSIBILITY_BONUS = 10f;
    public static final float DEFENSE_BONUS = 50f;

    @Override
    public void apply(String id) {
        super.apply(id);
        market.getAccessibilityMod().modifyFlat(id, -ACCESSIBILITY_BONUS/100f, txt("conGateScar"));
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id,1f+DEFENSE_BONUS/100, txt("conGateScar"));
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        if (market == null) {
            return;
        }

        tooltip.addPara(txt("access"),
                10f, Misc.getHighlightColor(),
                "-"+(int)ACCESSIBILITY_BONUS+"%"
        );
        tooltip.addPara(txt("defense"), 10f, Misc.getHighlightColor(),
                "+"+(int)DEFENSE_BONUS+"%"
                );
    }
}
