package gui;

public class FlexerGenerator {
    public static void main(String[] args) {
        new freditor.FlexerGenerator(-12, 7).generateTokens(
                "else", "false", "if", "repeat", "true", "void", "while",
                "!", "&&", ";", "||"
        );
    }
}
