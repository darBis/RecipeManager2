package haveric.recipeManager.flag.flags.result;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.RecipeBooks;
import haveric.recipeManager.data.RecipeBook;
import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.recipes.ItemResult;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import java.util.List;

public class FlagGetRecipeBook extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.GET_RECIPE_BOOK;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <book id> [volume <num>]", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Overwrites result with the specified recipe book.",
            "",
            "For the '<book id>' argument you need to specify the book ID/filename, case insensitive.",
            "",
            "Optionally you can set which volume to give, will give first by default, using a bigger number than the number of volumes will pick the last volume.", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} recipe stuff // matches a 'Recipe Stuff.yml' book for example.",
            "{flag} vanilla_recipes volume 2 // matches a 'vanilla_recipes.yml' with volume 2 for example.", };
    }


    private String bookID;
    private int volume = 1;

    public FlagGetRecipeBook() {
    }

    public FlagGetRecipeBook(FlagGetRecipeBook flag) {
        bookID = flag.bookID;
        volume = flag.volume;
    }

    @Override
    public FlagGetRecipeBook clone() {
        return new FlagGetRecipeBook((FlagGetRecipeBook) super.clone());
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String id) {
        Validate.notNull(id, "The 'id' argument must not be null!");
        bookID = id;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int newVolume) {
        volume = Math.max(newVolume, 1);
    }

    @Override
    public boolean onValidate() {
        ItemResult result = getResult();

        if (result == null || result.getType() != Material.WRITTEN_BOOK) {
            return ErrorReporter.getInstance().error("Flag " + getFlagType() + " needs a WRITTEN_BOOK to work!");
        }

        return true;
    }

    @Override
    public boolean onParse(String value, String fileName, int lineNum, int restrictedBit) {
        super.onParse(value, fileName, lineNum, restrictedBit);
        value = value.toLowerCase();
        String id = value;
        int index = value.lastIndexOf("volume");

        if (index > 0) {
            value = value.substring(index + "volume".length()).trim();

            try {
                setVolume(Integer.parseInt(value));
                id = id.substring(0, index).trim();
            } catch (NumberFormatException e) {
                // TODO: Handle exception
            }
        }

        setBookID(id);

        return true;
    }

    @Override
    public void onRegistered() {
        List<RecipeBook> books = RecipeBooks.getInstance().getBooksPartialMatch(bookID);

        if (books.isEmpty()) {
            ErrorReporter.getInstance().flagWarning(this, "Flag " + getFlagType() + " could not find book ID containing '" + bookID + "', flag ignored.");
            remove();
            return;
        }

        RecipeBook book = books.get(0);

        if (books.size() > 1) {
            ErrorReporter.getInstance().flagWarning(this, "Flag " + getFlagType() + " found " + books.size() + " books matching ID '" + bookID + "', using first: " + book.getTitle());
        }
    }

    @Override
    public void onPrepare(Args a) {
        onCrafted(a);
    }

    @Override
    public void onCrafted(Args a) {
        if (bookID == null) {
            a.addCustomReason("Book ID not set!");
            return;
        }

        if (!a.hasResult()) {
            a.addCustomReason("Need result!");
            return;
        }

        List<RecipeBook> books = RecipeBooks.getInstance().getBooksPartialMatch(bookID);

        if (books.isEmpty()) {
            return;
        }

        RecipeBook book = books.get(0);

        a.result().setItemMeta(book.getBookItem(volume).getItemMeta());
    }

    @Override
    public int hashCode() {
        String toHash = "" + super.hashCode();

        toHash += "bookID: " + bookID;
        toHash += "volume: " + volume;

        return toHash.hashCode();
    }
}
