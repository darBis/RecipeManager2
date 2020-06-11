package haveric.recipeManager.flag.flags.any;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.common.util.RMCUtil;
import org.bukkit.block.Block;

public class FlagTemperature extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.TEMPERATURE;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} [operator]<number> | [fail message]", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Checks if the crafter or furnace has at least 'min' temperature unless [operator] is set.",
            "Using this flag more than once will overwrite the previous one.",
            "",
            "The '[operator]' argument can be nothing at all or you can use >= (which is the same as nothing, to check for greater than or equal), <= (less than or equal), > (greater), or < (less than).",
            "The '<number>' argument must be the temperature you want to check against. Uses doubles, meaning 1 and 1.0 are valid numbers.",
            "The '[operator]<number>' combination can be used multiple times when separated by a comma. In that case, all checks must be successful",
            "",
            "Optionally you can overwrite the fail message or you can use 'false' to hide it.",
            "In the message the following variables can be used:",
            "  {temperature} = temperature conditions",
            "  {actual}      = the actual temperature player or furnace is at", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} < 0 // Must be in an icy biome",
            "{flag} 1.2 // Must be in a hot biome, such as a desert or savanna",
            "{flag} >= 1.2 // Equivalent to the above example",
            "{flag} >= .15, <= .95 // Must be within a temperature where it can rain", };
    }


    private Double lteTemp;
    private Double gteTemp;
    private Double ltTemp;
    private Double gtTemp;
    private String failMessage;

    public FlagTemperature() { }

    public FlagTemperature(FlagTemperature flag) {
        lteTemp = flag.lteTemp;
        gteTemp = flag.gteTemp;
        ltTemp = flag.ltTemp;
        gtTemp = flag.gtTemp;
        failMessage = flag.failMessage;
    }

    @Override
    public FlagTemperature clone() {
        return new FlagTemperature((FlagTemperature) super.clone());
    }

    public Double getLTETemp() {
        return lteTemp;
    }

    public void setLTETemp(Double newLTETemp) {
        lteTemp = newLTETemp;
    }

    public Double getGTETemp() {
        return gteTemp;
    }

    public void setGTETemp(Double newGTETemp) {
        gteTemp = newGTETemp;
    }

    public Double getLTTemp() {
        return ltTemp;
    }

    public void setLTTemp(Double newLTTemp) {
        ltTemp = newLTTemp;
    }

    public Double getGTTemp() {
        return gtTemp;
    }

    public void setGTTemp(Double newGTTemp) {
        gtTemp = newGTTemp;
    }

    public String getTemperatureString() {
        String tempString = "";

        if (ltTemp != null) {
            tempString += "< " + ltTemp;
        } else if (lteTemp != null) {
            tempString += "<= " + lteTemp;
        }

        if (!tempString.isEmpty() && (gtTemp != null || gteTemp != null)) {
            tempString += " and ";
        }

        if (gtTemp != null) {
            tempString += "> " + gtTemp;
        } else if (gteTemp != null) {
            tempString += ">= " + gteTemp;
        }

        return tempString;
    }

    public boolean checkTemperature(double temp) {
        boolean check = true;

        if (gteTemp != null) {
            check = temp >= gteTemp;
        }

        if (check && lteTemp != null) {
            check = temp <= lteTemp;
        }

        if (check && gtTemp != null) {
            check = temp > gtTemp;
        }

        if (check && ltTemp != null) {
            check = temp < ltTemp;
        }

        return check;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String newFailMessage) {
        failMessage = newFailMessage;
    }

    @Override
    public boolean onParse(String value, String fileName, int lineNum, int restrictedBit) {
        super.onParse(value, fileName, lineNum, restrictedBit);
        String[] split = value.split("\\|");

        if (split.length > 1) {
            failMessage = RMCUtil.trimExactQuotes(split[1]);
        }

        split = split[0].split(",", 2);

        for (String temp : split) {
            value = temp.trim();

            if (value.startsWith("<=")) {
                value = value.substring(2).trim();

                try {
                    lteTemp = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    ErrorReporter.getInstance().error("The " + getFlagType() + " flag has invalid <= temperature number: " + value);
                }
            } else if (value.startsWith(">=")) {
                value = value.substring(2).trim();

                try {
                    gteTemp = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    ErrorReporter.getInstance().error("The " + getFlagType() + " flag has invalid >= temperature number: " + value);
                }
            } else if (value.charAt(0) == '<') {
                value = value.substring(1).trim();

                try {
                    ltTemp = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    ErrorReporter.getInstance().error("The " + getFlagType() + " flag has invalid < temperature number: " + value);
                }
            } else if (value.charAt(0) == '>') {
                value = value.substring(1).trim();

                try {
                    gtTemp = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    ErrorReporter.getInstance().error("The " + getFlagType() + " flag has invalid > temperature number: " + value);
                }
            } else {
                // Default to >= when no operator set
                try {
                    gteTemp = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    ErrorReporter.getInstance().error("The " + getFlagType() + " flag has invalid  temperature number: " + value);
                }
            }
        }

        return true;
    }

    @Override
    public void onCheck(Args a) {
        if (!a.hasLocation()) {
            a.addReason("flag.temperature", failMessage, "{temperature}", getTemperatureString(), "{actual}", "Unknown Temperature");
        } else {
            Block block = a.location().getBlock();
            double biomeTemperature = block.getTemperature();
            int y = block.getY();
            int seaLevel = a.location().getWorld().getSeaLevel();

            if (y > seaLevel) {
                biomeTemperature -= ((y - seaLevel) * 0.00166667);
            }

            if (!checkTemperature(biomeTemperature)) {
                a.addReason("flag.temperature", failMessage, "{temperature}", getTemperatureString(), "{actual}", biomeTemperature);
            }
        }
    }

    @Override
    public int hashCode() {
        String toHash = "" + super.hashCode();

        toHash += "lteTemp: " + lteTemp.toString();
        toHash += "gteTemp: " + gteTemp.toString();
        toHash += "ltTemp: " + ltTemp.toString();
        toHash += "gtTemp: " + gtTemp.toString();
        toHash += "failMessage: " + failMessage;

        return toHash.hashCode();
    }
}
