package seedu.quotesify;

import seedu.quotesify.commands.Command;
import seedu.quotesify.lists.ListManager;
import seedu.quotesify.parser.Parser;
import seedu.quotesify.store.Storage;
import seedu.quotesify.ui.TextUi;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Quotesify {
    /**
     * Main entry-point for the java.duke.Duke application.
     */
    private TextUi ui;
    private Parser parser;

    private final Logger logger = Logger.getLogger("QuotesifyLogger");
    private final Storage storage;
    private final String saveFileLocation = "/data/quotesify.json";

    public Quotesify() {
        ui = new TextUi();
        parser = new Parser();

        setupLogger();
        ListManager.initialiseAllLists();
        storage = new Storage(saveFileLocation);
        storage.load();
    }

    public void start() {
        ui.showWelcomeMessage();
        ui.printRandomQuote();
    }

    public void exit() {
        ui.printDividerLine();
        ui.printRandomQuote();
        ui.showGoodbyeMessage();
        ui.printDividerLine();
        System.exit(0);
    }

    public void runLoopUntilExitCommand() {
        boolean isExit = false;
        while (!isExit) {
            String userCommandText = ui.getUserCommand();
            Command command = parser.parseUserCommand(userCommandText);
            ui.printDividerLine();
            if (command == null) {
                ui.printInvalidQuotesifyCommand();
                continue;
            }
            command.execute(ui, storage);
            ui.printDividerLine();
            isExit = command.isExit();
        }
    }

    public void run() {
        start();
        runLoopUntilExitCommand();
        exit();
    }

    public static void main(String[] args) {
        new Quotesify().run();
    }

    private void setupLogger() {
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.INFO);
        try {
            FileHandler fileHandler = new FileHandler("quotesify.log", true);
            // remove this if you want to view logs in XML format
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}