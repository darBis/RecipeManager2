package haveric.recipeManager.flag;

import haveric.recipeManager.RecipeBooks;
import haveric.recipeManager.RecipeManager;
import haveric.recipeManager.Recipes;
import haveric.recipeManager.data.RecipeBook;
import haveric.recipeManager.messages.MessageSender;
import haveric.recipeManager.messages.TestMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class FlagAddToBookTest extends FlagBaseTest {

    @BeforeEach
    public void createBookFiles() {
        File booksDir = new File(workDir.getPath() + "/books/");
        booksDir.mkdirs();

        File originalBook1 = new File(baseResourcesPath + "books/Random Stuff.yml");
        File originalBook2 = new File(baseResourcesPath + "books/Testing Book.yml");
        File book1 = new File(booksDir.getPath() + "/Random Stuff.yml");
        File book2 = new File(booksDir.getPath() + "/Testing Book.yml");
        try {
            java.nio.file.Files.copy(originalBook1.toPath(), book1.toPath(), StandardCopyOption.REPLACE_EXISTING);
            java.nio.file.Files.copy(originalBook2.toPath(), book2.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // TODO: Handle error
        }

        RecipeBooks.getInstance().init(booksDir);

        try (MockedStatic<MessageSender> mockedMessageSender = mockStatic(MessageSender.class)) {
            mockedMessageSender.when(MessageSender::getInstance).thenReturn(TestMessageSender.getInstance());

            RecipeBooks.getInstance().reload(null);
        }
    }

    @Test
    public void onRecipeParse() {
        File file = new File(baseRecipePath + "flagAddToBook/");
        reloadRecipeProcessor(false, file);
        try (MockedStatic<RecipeManager> mockedRecipeManager = mockStatic(RecipeManager.class)) {
            mockedRecipeManager.when(RecipeManager::getSettings).thenReturn(settings);
            mockedRecipeManager.when(RecipeManager::getRecipes).thenReturn(recipes);

            //Map<BaseRecipe, RMCRecipeInfo> queued = RecipeProcessor.getRegistrator().getQueuedRecipes();

            Recipes actualRecipes = RecipeManager.getRecipes();
            assertEquals(5, actualRecipes.getIndex().size());
        }

        Map<String, RecipeBook> books = RecipeBooks.getInstance().getBooks();
        assertEquals(2, books.entrySet().size());

        RecipeBook bookOne = RecipeBooks.getInstance().getBook("random stuff");
        assertEquals("Random Stuff", bookOne.getTitle());
        assertEquals("RecipeManager", bookOne.getAuthor());
        assertEquals("Book description written on the first page", bookOne.getDescription());
        assertEquals(25, bookOne.getRecipesPerVolume());
        assertFalse(bookOne.hasCoverPage());
        assertFalse(bookOne.hasContentsPage());
        assertFalse(bookOne.hasEndPage());
        assertEquals("Custom End", bookOne.getCustomEndPage());
        assertEquals(1, bookOne.getVolumesNum());
        assertEquals(1, bookOne.getVolumeRecipes(0).size());

        RecipeBook bookTwo = RecipeBooks.getInstance().getBook("testing book");
        assertEquals("testing book", bookTwo.getTitle());
        assertEquals("RecipeManager", bookTwo.getAuthor());
        assertEquals("", bookTwo.getDescription());
        assertEquals(50, bookTwo.getRecipesPerVolume());
        assertTrue(bookTwo.hasCoverPage());
        assertTrue(bookTwo.hasContentsPage());
        assertTrue(bookTwo.hasEndPage());
        assertNull(bookTwo.getCustomEndPage());
        assertEquals(1, bookTwo.getVolumesNum());
        assertEquals(3, bookTwo.getVolumeRecipes(0).size());

        // TODO: Add more testing to validate book options
    }
}
