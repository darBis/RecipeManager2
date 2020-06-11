package haveric.recipeManager.flag.flags.recipe;

import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.common.util.RMCUtil;

public class FlagRestrict extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.RESTRICT;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} [fail message]", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Restricts the recipe for everybody.",
            "This is the player-friendly version of @remove because crafter gets a message when trying to craft the recipe.",
            "",
            "Optionally you can overwrite the default restrict message.", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag}",
            "{flag} <red>Access denied!", };
    }


    private String message;

    public FlagRestrict() {
    }

    public FlagRestrict(FlagRestrict flag) {
        message = flag.message;
    }

    @Override
    public FlagRestrict clone() {
        return new FlagRestrict((FlagRestrict) super.clone());
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }

    @Override
    public boolean onParse(String value, String fileName, int lineNum, int restrictedBit) {
        super.onParse(value, fileName, lineNum, restrictedBit);
        message = RMCUtil.trimExactQuotes(value);
        return true;
    }

    @Override
    public void onCheck(Args a) {
        a.addReason("flag.restrict", message);
    }

    @Override
    public int hashCode() {
        String toHash = "" + super.hashCode();

        toHash += "message: " + message;

        return toHash.hashCode();
    }
}
