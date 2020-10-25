package seedu.quotesify.commands.delete;

import seedu.quotesify.book.Book;
import seedu.quotesify.book.BookList;
import seedu.quotesify.bookmark.Bookmark;
import seedu.quotesify.bookmark.BookmarkList;
import seedu.quotesify.commands.Command;
import seedu.quotesify.lists.ListManager;
import seedu.quotesify.quote.QuoteList;
import seedu.quotesify.rating.Rating;
import seedu.quotesify.rating.RatingList;
import seedu.quotesify.rating.RatingParser;
import seedu.quotesify.store.Storage;
import seedu.quotesify.todo.ToDo;
import seedu.quotesify.todo.ToDoList;
import seedu.quotesify.ui.TextUi;

public class DeleteCommand extends Command {
    public String type;
    public String information;
    private String arguments;

    public DeleteCommand(String arguments) {
        this.arguments = arguments;
        String[] details = arguments.split(" ", 2);

        // if user did not provide arguments, let details[1] be empty string
        if (details.length == 1) {
            details = new String[]{details[0], ""};
        }
        type = details[0];
        information = details[1];
    }

    @Override
    public void execute(TextUi ui, Storage storage) {
        switch (type) {
        case TAG_CATEGORY:
            new DeleteCategoryCommand(arguments).execute(ui, storage);
            break;
        case TAG_BOOK:
            new DeleteBookCommand(arguments).execute(ui, storage);
            break;
        case TAG_RATING:
            RatingList ratings = (RatingList) ListManager.getList(ListManager.RATING_LIST);
            deleteRating(ratings, ui);
            break;
        case TAG_TODO:
            new DeleteToDoCommand(arguments).execute(ui, storage);
            break;
        case TAG_BOOKMARK:
            new DeleteBookmarkCommand(arguments).execute(ui, storage);
            break;
        case TAG_QUOTE:
            new DeleteQuoteCommand(arguments).execute(ui, storage);
            break;
        case TAG_QUOTE_REFLECTION:
            QuoteList quoteList = (QuoteList) ListManager.getList(ListManager.QUOTE_LIST);
            deleteQuoteReflection(quoteList, ui, information);
            break;
        default:
            ui.printListOfDeleteCommands();
            break;
        }
        storage.save();
    }

    private void deleteQuoteReflection(QuoteList quoteList, TextUi ui, String information) {
        try {
            int quoteNumber = Integer.parseInt(information.trim()) - 1;
            quoteList.deleteReflection(quoteNumber);
            ui.printDeleteQuoteReflection(quoteList.getQuote(quoteNumber).getQuote());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println(ERROR_INVALID_QUOTE_NUM);
        }
    }

    private void deleteRating(RatingList ratings, TextUi ui) {
        if (information.isEmpty()) {
            System.out.println(ERROR_RATING_MISSING_INPUTS);
            return;
        }

        String[] titleAndAuthor;
        String title;
        String author;
        try {
            titleAndAuthor = information.split(Command.FLAG_AUTHOR, 2);
            title = titleAndAuthor[0].trim();
            author = titleAndAuthor[1].trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(RatingParser.ERROR_INVALID_FORMAT_RATING);
            return;
        }

        Rating ratingToBeDeleted = null;
        for (Rating rating : ratings.getList()) {
            if (rating.getTitleOfRatedBook().equals(title)
                    && rating.getAuthorOfRatedBook().equals(author)) {
                ratingToBeDeleted = rating;
                break;
            }
        }

        if (ratingToBeDeleted == null) {
            System.out.println(ERROR_RATING_NOT_FOUND);
            return;
        }
        ratingToBeDeleted.getRatedBook().setRating(0);
        ratings.delete(ratings.getList().indexOf(ratingToBeDeleted));
        ui.printDeleteRating(title, author);
    }

    private void deleteCategoryFromBookOrQuote(CategoryList categories, TextUi ui) {
        String[] tokens = information.split(" ");
        String[] parameters = CategoryParser.getRequiredParameters(tokens);
        int result = CategoryParser.validateParametersResult(parameters);
        if (result == 1) {
            executeParameters(categories, parameters, ui);
        } else if (result == 0) {
            deleteCategory(categories, parameters[0], ui);
        } else {
            ui.printErrorMessage(ERROR_MISSING_CATEGORY);
        }
    }

    private void executeParameters(CategoryList categoryList, String[] parameters, TextUi ui) {
        try {
            String categoryNames = parameters[0];
            assert !categoryNames.isEmpty() : "category name should not be empty";

            List<String> categories = CategoryParser.parseCategoriesToList(categoryNames);
            for (String categoryName : categories) {
                Category category = categoryList.getCategoryByName(categoryName);

                String bookTitle = parameters[1];
                String quoteNum = parameters[2];

                deleteCategoryFromBook(category, bookTitle, ui);
                deleteCategoryFromQuote(category, quoteNum, ui);
                categoryList.updateListInCategory(category);

                if (category.getSize() == 0) {
                    categoryList.remove(category);
                }
            }
        } catch (QuotesifyException e) {
            ui.printErrorMessage(e.getMessage());
        }
    }

    private void deleteCategoryFromBook(Category category, String bookTitle, TextUi ui) {
        // ignore this action if user did not provide book title
        if (bookTitle.isEmpty()) {
            return;
        }

        BookList bookList = category.getBookList();
        try {
            int bookNum = Integer.parseInt(bookTitle) - 1;
            Book book = bookList.getBook(bookNum);
            ArrayList<String> categories = book.getCategories();
            categories.remove(category.getCategoryName());
            ui.printRemoveCategoryFromBook(book.getTitle(), category.getCategoryName());
        } catch (IndexOutOfBoundsException e) {
            ui.printErrorMessage(ERROR_NO_BOOK_FOUND + "\b tagged as [" + category.getCategoryName() + "]!");
        } catch (NumberFormatException e) {
            ui.printErrorMessage(ERROR_INVALID_BOOK_NUM);
        }
    }

    private void deleteCategoryFromQuote(Category category, String index, TextUi ui) {
        // ignore this action if user did not provide quote number
        if (index.isEmpty()) {
            return;
        }

        QuoteList quoteList = category.getQuoteList();
        ArrayList<Quote> quotes = quoteList.getList();
        try {
            int quoteNum = Integer.parseInt(index) - 1;
            Quote quote = quotes.get(quoteNum);
            ArrayList<String> categories = quote.getCategories();
            categories.remove(category.getCategoryName());
            ui.printRemoveCategoryFromQuote(quote.getQuote(), category.getCategoryName());
        } catch (IndexOutOfBoundsException e) {
            ui.printErrorMessage(ERROR_NO_QUOTE_FOUND + "\b tagged as [" + category.getCategoryName() + "]!");
        } catch (NumberFormatException e) {
            ui.printErrorMessage(ERROR_INVALID_QUOTE_NUM);
        }
    }

    private void deleteCategory(CategoryList categoryList, String categories, TextUi ui) {
        for (String name : categories.split(" ")) {
            try {
                Category category = categoryList.getCategoryByName(name);
                deleteCategoryInBooksAndQuotes(name);
                categoryList.remove(category);
                ui.printRemoveCategory(name);
            } catch (QuotesifyException e) {
                ui.printErrorMessage(e.getMessage());
            }
        }
    }

    public void deleteCategoryInBooksAndQuotes(String oldCategory) {
        BookList bookList = (BookList) ListManager.getList(ListManager.BOOK_LIST);
        QuoteList quoteList = (QuoteList) ListManager.getList(ListManager.QUOTE_LIST);
        bookList.filterByCategory(oldCategory).getList().forEach(book -> {
            book.getCategories().remove(oldCategory);
        });

        quoteList.filterByCategory(oldCategory).getList().forEach(quote -> {
            quote.getCategories().remove(oldCategory);
        });

    }

    @Override
    public boolean isExit() {
        return false;
    }
}