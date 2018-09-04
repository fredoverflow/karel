package gui;

public class BytecodeFlexerGenerator {
    public static void main(String[] args) {
        new freditor.FlexerGenerator(-10, 2)
                .withIdentifierCall("END")
                .generateTokens(
                        "@", "CODE", "MNEMONIC",
                        "RET",
                        "MOVE", "TRNL", "TRNA", "TRNR", "PICK", "DROP",
                        "BEEP", "HEAD", "LCLR", "FCLR", "RCLR",
                        "NOT", "AND", "OR", "XOR",
                        "PUSH", "LOOP", "CALL", "JUMP", "J0MP", "J1MP"
                );
    }
}
