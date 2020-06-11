package haveric.recipeManager.flag.flags.any;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.Files;
import haveric.recipeManager.common.util.RMCUtil;
import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.RMParticle;
import haveric.recipeManager.flag.args.Args;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

public class FlagSpawnParticle extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.SPAWN_PARTICLE;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <particle> | [arguments]", };
    }

    @Override
    protected String[] getDescription() {
    return new String[] {
            "Spawn a particle at crafting location",
            "This flag can be used more than once to spawn more particles.",
            "",
            "The &lt;particle&gt; argument must be a particle name, see " + Files.getNameIndexHashLink("particle"),
            "",
            "Optionally you can specify some arguments separated by | character:",
            "  offset <x> <y> <z>          = (default: 0.5 1.0 0.5) Offset positioning of the particle relative to the block/player crafting. Allows doubles (0.0)",
            "  randomoffset <x> <y> <z>    = (default: .25 .25 .25) Random offset of the particle relative to the block/player crafting. Allows doubles (0.0)",
            "  count <amount>              = How many particles are spawned",
            "  extra <value>               = Used to set extra data for certain particles. For example, speed. Allows doubles (0.0)",
            "You can specify these arguments in any order and they're completely optional.", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag}",
            "{flag} heart | count 3",
            "{flag} smoke_normal | count 5",
            "{flag} lava | count 20 | randomoffset 0.5 1 .5 | offset 0 2", };
    }

    private List<RMParticle> particles = new ArrayList<>();

    public FlagSpawnParticle() {}

    public FlagSpawnParticle(FlagSpawnParticle flag) {
        particles.addAll(flag.particles);
    }

    public List<RMParticle> getParticles() {
        return particles;
    }

    @Override
    public FlagSpawnParticle clone() {
        return new FlagSpawnParticle((FlagSpawnParticle) super.clone());
    }

    @Override
    public boolean onParse(String value, String fileName, int lineNum, int restrictedBit) {
        super.onParse(value, fileName, lineNum, restrictedBit);
        String[] split = value.toLowerCase().split("\\|");

        value = split[0].trim();

        Particle particle = RMCUtil.parseEnum(value, Particle.values());
        if (particle == null) {
            ErrorReporter.getInstance().error("The " + getFlagType() + " flag has invalid particle: " + value, "Look in '" + Files.FILE_INFO_NAMES + "' at 'PARTICLE LIST' section for particles.");
            return false;
        }

        RMParticle rmParticle = new RMParticle(particle);

        if (split.length > 1) {
            for (int n = 1; n < split.length; n++) {
                String original = split[n].trim();
                value = original.toLowerCase();

                if (value.startsWith("offset")) {
                    value = value.substring("offset".length()).trim();
                    String[] offsets = value.split(" ", 3);

                    if (offsets.length < 1) {
                        ErrorReporter.getInstance().error("Flag " + getFlagType() + " has 'offset' argument with no values!", "Add values separated by a space (ex: 1.0 2.2 1.2)");
                        return false;
                    }

                    try {
                        rmParticle.setOffsetX(Double.parseDouble(offsets[0]));
                    } catch (NumberFormatException e) {
                        ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid offset x value: " + offsets[0]);
                    }

                    if (offsets.length >= 2) {
                        try {
                            rmParticle.setOffsetY(Double.parseDouble(offsets[1]));
                        } catch (NumberFormatException e) {
                            ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid offset y value: " + offsets[1]);
                        }
                    }

                    if (offsets.length >= 3) {
                        try {
                            rmParticle.setOffsetZ(Double.parseDouble(offsets[2]));
                        } catch (NumberFormatException e) {
                            ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid offset z value: " + offsets[2]);
                        }
                    }
                } else if (value.startsWith("randomoffset") || value.startsWith("offsetrandom")) {
                    value = value.substring("randomoffset".length()).trim();
                    String[] offsets = value.split(" ", 3);

                    if (offsets.length < 1) {
                        ErrorReporter.getInstance().error("Flag " + getFlagType() + " has 'randomoffset' argument with no values!", "Add values separated by a space (ex: 1.0 2.2 1.2)");
                        return false;
                    }

                    try {
                        rmParticle.setRandomOffsetX(Double.parseDouble(offsets[0]));
                    } catch (NumberFormatException e) {
                        ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid randomoffset x value: " + offsets[0]);
                    }

                    if (offsets.length >= 2) {
                        try {
                            rmParticle.setRandomOffsetY(Double.parseDouble(offsets[1]));
                        } catch (NumberFormatException e) {
                            ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid randomoffset y value: " + offsets[1]);
                        }
                    }

                    if (offsets.length >= 3) {
                        try {
                            rmParticle.setRandomOffsetZ(Double.parseDouble(offsets[2]));
                        } catch (NumberFormatException e) {
                            ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid randomoffset z value: " + offsets[2]);
                        }
                    }
                } else if (value.startsWith("count")) {
                    value = value.substring("count".length()).trim();

                    try {
                        rmParticle.setCount(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid count value: " + value);
                    }
                } else if (value.startsWith("extra")) {
                    value = value.substring("extra".length()).trim();

                    try {
                        rmParticle.setExtra(Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid extra value: " + value);
                    }
                }
            }
        }

        particles.add(rmParticle);

        return true;
    }

    @Override
    public void onCrafted(Args a) {
        if (!a.hasLocation()) {
            a.addCustomReason("Needs location!");
            return;
        }

        for (RMParticle particle : particles) {
            spawnParticle(a, particle);
        }
    }

    private void spawnParticle(final Args a, final RMParticle rmParticle) {
        Location location = a.location();

        final double x = location.getX() + rmParticle.getOffsetX();
        final double y = location.getY() + rmParticle.getOffsetY();
        final double z = location.getZ() + rmParticle.getOffsetZ();

        final double randomOffsetX = rmParticle.getRandomOffsetX();
        final double randomOffsetY = rmParticle.getRandomOffsetY();
        final double randomOffsetZ = rmParticle.getRandomOffsetZ();

        final int count = rmParticle.getCount();

        final Double extra = rmParticle.getExtra();

        final Particle particle = rmParticle.getParticle();

        if (extra.isNaN()) {
            a.location().getWorld().spawnParticle(particle, x, y, z, count, randomOffsetX, randomOffsetY, randomOffsetZ);
        } else {
            a.location().getWorld().spawnParticle(particle, x, y, z, count, randomOffsetX, randomOffsetY, randomOffsetZ, extra);
        }
    }

    @Override
    public int hashCode() {
        String toHash = "" + super.hashCode();

        for (RMParticle particle : particles) {
            toHash += particle.hashCode();
        }

        return toHash.hashCode();
    }
}
