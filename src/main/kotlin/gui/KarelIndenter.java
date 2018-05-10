package gui;

import freditor.JavaIndenter;

public class KarelIndenter extends JavaIndenter {
    public static final KarelIndenter instance = new KarelIndenter();

    @Override
    public int indentationDelta(int state) {
        switch (state) {
            case Flexer.OPENING_PAREN:
            case Flexer.OPENING_BRACE:
                return +4;

            case Flexer.CLOSING_PAREN:
            case Flexer.CLOSING_BRACE:
                return -4;

            default:
                return 0;
        }
    }
}
