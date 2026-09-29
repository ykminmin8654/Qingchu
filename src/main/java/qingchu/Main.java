package qingchu;

import qingchu.error.QingchuError;
import qingchu.lexer.Lexer;
import qingchu.lexer.Token;
import qingchu.parse.Node;
import qingchu.parse.Parser;
import qingchu.runtime.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * The entry point for Qingchu.
 *
 * Two modes:
 *   java -cp out qingchu.Main run file.qc    — run a file
 *   java -cp out qingchu.Main                — start the REPL
 */
public class Main {

    public static void main(String[] args) {
        if (args.length >= 2 && args[0].equals("run")) {
            runFile(args[1]);
        } else {
            repl();
        }
    }

    // === File mode ===

    private static void runFile(String filename) {
        String source;
        try {
            source = Files.readString(Path.of(filename));
            // Strip a UTF-8 BOM if present.
            if (!source.isEmpty() && source.charAt(0) == '\uFEFF') {
                source = source.substring(1);
            }
        } catch (IOException e) {
            System.err.println("error could not read file " + filename);
            return;
        }

        try {
            run(source);
        } catch (QingchuError e) {
            System.err.println(e.fullMessage());
        }
    }

    // === REPL mode ===

    private static void repl() {
        System.out.println("Qingchu v0.1.0");
        System.out.println("Type your program. Press Ctrl+D (or Ctrl+Z on Windows) to exit.");
        System.out.println();

        Scanner in = new Scanner(System.in);
        StringBuilder buffer = new StringBuilder();

        while (true) {
            System.out.print("qc> ");
            if (!in.hasNextLine()) break;
            String line = in.nextLine();
            buffer.append(line).append("\n");

            // A line ending with " ." completes the program.
            if (line.trim().endsWith(".")) {
                try {
                    run(buffer.toString());
                } catch (QingchuError e) {
                    System.err.println(e.fullMessage());
                }
                buffer.setLength(0);
            }
        }

        System.out.println();
        System.out.println("Goodbye.");
    }

    // === The pipeline ===

    private static void run(String source) {
        // 1. Lex: source text → tokens
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.lex();

        // 2. Parse: tokens → AST
        Parser parser = new Parser(tokens);
        List<Node> program = parser.parse();

        // 3. Run: AST → execution
        Interpreter interpreter = new Interpreter();
        interpreter.run(program);
    }
}