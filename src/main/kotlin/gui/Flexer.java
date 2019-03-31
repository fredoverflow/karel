package gui;

import freditor.FlexerState;
import freditor.FlexerStateBuilder;
import freditor.persistent.ChampMap;

import static freditor.FlexerState.EMPTY;
import static freditor.FlexerState.THIS;

public class Flexer extends freditor.Flexer {
    public static final Flexer instance = new Flexer();

    private static final FlexerState SLASH_SLASH = new FlexerState('\n', null).setDefault(THIS);
    private static final FlexerState SLASH_ASTERISK___ASTERISK_SLASH = EMPTY.tail();
    private static final FlexerState SLASH_ASTERISK___ASTERISK = new FlexerState('*', THIS, '/', SLASH_ASTERISK___ASTERISK_SLASH);
    private static final FlexerState SLASH_ASTERISK = new FlexerState('*', SLASH_ASTERISK___ASTERISK).setDefault(THIS);

    static {
        SLASH_ASTERISK___ASTERISK.setDefault(SLASH_ASTERISK);
    }

    private static final FlexerState NUMBER_TAIL = new FlexerState("09", THIS);
    private static final FlexerState NUMBER_HEAD = NUMBER_TAIL.head();

    private static final FlexerState IDENTIFIER_TAIL = new FlexerState("09AZ__az", THIS);
    private static final FlexerState IDENTIFIER_HEAD = IDENTIFIER_TAIL.head();

    private static final FlexerState START = new FlexerStateBuilder()
            .set('(', OPENING_PAREN)
            .set(')', CLOSING_PAREN)
            .set('{', OPENING_BRACE)
            .set('}', CLOSING_BRACE)
            .set('\n', NEWLINE)
            .set(' ', SPACE_HEAD)
            .set('/', new FlexerState('*', SLASH_ASTERISK, '/', SLASH_SLASH).head())
            .set("09", NUMBER_HEAD)
            .set("AZ__az", IDENTIFIER_HEAD)
            .build()
            .verbatim(IDENTIFIER_TAIL, "else", "false", "if", "repeat", "true", "void", "while")
            .verbatim(EMPTY, "!", "&&", ";", "||")
            .setDefault(ERROR);

    @Override
    protected FlexerState start() {
        return START;
    }

    @Override
    public int pickColorForLexeme(FlexerState previousState, FlexerState endState) {
        Integer color = lexemeColors.get(endState);
        return color != null ? color : 0x000000;
    }

    private static final ChampMap<FlexerState, Integer> lexemeColors = ChampMap.of(ERROR, 0x808080)
            .put(START.read("/", "&", "|"), 0x808080)
            .put(SLASH_SLASH, SLASH_ASTERISK, SLASH_ASTERISK___ASTERISK, SLASH_ASTERISK___ASTERISK_SLASH, 0x008000)
            .put(NUMBER_HEAD, NUMBER_TAIL, 0x6400c8)
            .put(START.read("else", "false", "if", "repeat", "true", "while"), 0x0000ff)
            .put(START.read("void"), 0x008080)
            .put(START.read("(", ")", "{", "}"), 0xff0000)
            .put(START.read("!", "&&", "||"), 0x804040);
}
