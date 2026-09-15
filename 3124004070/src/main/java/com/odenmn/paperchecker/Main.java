package com.odenmn.paperchecker;

/**
 * Command-line entry point for the paper checker.
 */
public final class Main {

    private Main() {
        // Utility class.
    }

    /**
     * Runs the application and reports failures through a non-zero exit code.
     *
     * @param args original file, suspicious file and answer file
     */
    public static void main(String[] args) {
        int exitCode = new PaperCheckerApplication().run(args, System.err);
        if (exitCode != PaperCheckerApplication.SUCCESS) {
            System.exit(exitCode);
        }
    }
}
