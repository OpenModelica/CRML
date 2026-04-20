package crml.model;

import org.example.library.Author;
import org.example.library.Book;
import org.example.library.BookCategory;
import org.example.library.Library;
import org.example.library.LibraryFactory;

/**
 * Demonstrates using the EMF model classes generated from library.xcore.
 *
 * Run with:  ./gradlew run
 */
public class Main {

    public static void main(String[] args) {

        LibraryFactory factory = LibraryFactory.eINSTANCE;

        // ── Create the library ────────────────────────────────────────────
        Library library = factory.createLibrary();
        library.setName("Linköping City Library");

        // ── Create authors ────────────────────────────────────────────────
        Author austen = factory.createAuthor();
        austen.setFirstName("Jane");
        austen.setLastName("Austen");
        library.getAuthors().add(austen);

        Author sagan = factory.createAuthor();
        sagan.setFirstName("Carl");
        sagan.setLastName("Sagan");
        library.getAuthors().add(sagan);

        // ── Create books ──────────────────────────────────────────────────
        addBook(factory, library, "Pride and Prejudice", 432, BookCategory.FICTION, austen);
        addBook(factory, library, "Sense and Sensibility", 352, BookCategory.FICTION, austen);
        addBook(factory, library, "Cosmos", 365, BookCategory.SCIENCE, sagan);
        addBook(factory, library, "Pale Blue Dot", 384, BookCategory.SCIENCE, sagan);

        // ── Print the catalogue ───────────────────────────────────────────
        System.out.println("=== " + library.getName() + " ===");
        System.out.println();
        for (Book book : library.getBooks()) {
            System.out.printf("  %-30s  %-15s  %4d pages  [%s]%n",
                    book.getTitle(),
                    book.getAuthor().fullName(),
                    book.getPages(),
                    book.getCategory());
        }

        // ── Demonstrate the generated findBookByTitle() operation ─────────
        System.out.println();
        String searchTitle = "Cosmos";
        Book found = library.findBookByTitle(searchTitle);
        if (found != null) {
            System.out.println("Found \"" + searchTitle + "\" → " + found.getPages() + " pages");
        } else {
            System.out.println("\"" + searchTitle + "\" not found.");
        }
    }

    private static void addBook(LibraryFactory factory, Library library,
                                 String title, int pages,
                                 BookCategory category, Author author) {
        Book book = factory.createBook();
        book.setTitle(title);
        book.setPages(pages);
        book.setCategory(category);
        book.setAuthor(author);
        library.getBooks().add(book);
    }
}
