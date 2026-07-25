package com.dhj.ingameime.mixins.vanilla;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dhj.ingameime.mixins.vanilla.MixinGuiTextField.GraphemeClusters;
import org.junit.jupiter.api.Test;

class GraphemeClustersTest {
    private static final String GRINNING = "\uD83D\uDE00"; // U+1F600, one surrogate pair
    private static final String FLAG = "\uD83C\uDDE8\uD83C\uDDF3"; // U+1F1E8 U+1F1F3, two regional indicators
    private static final String FAMILY =
            "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67"; // man ZWJ woman ZWJ girl
    private static final String THUMBS_UP_TONE = "\uD83D\uDC4D\uD83C\uDFFD"; // U+1F44D + skin tone U+1F3FD
    private static final String PLANE_VS16 = "\u2708\uFE0F"; // U+2708 + variation selector 16

    @Test
    void nextClusterEndAdvancesOneCodePointForPlainText() {
        assertEquals(1, GraphemeClusters.nextClusterEnd("abc", 0));
        assertEquals(2, GraphemeClusters.nextClusterEnd("abc", 1));
        assertEquals(2, GraphemeClusters.nextClusterEnd(GRINNING + "x", 0));
        assertEquals(3, GraphemeClusters.nextClusterEnd(GRINNING + "x", 2));
    }

    @Test
    void nextClusterEndGroupsEmojiSequencesIntoOneCluster() {
        assertEquals(4, GraphemeClusters.nextClusterEnd(FLAG + "x", 0));
        assertEquals(8, GraphemeClusters.nextClusterEnd(FAMILY, 0));
        assertEquals(2, GraphemeClusters.nextClusterEnd("e\u0301x", 0));
        assertEquals(4, GraphemeClusters.nextClusterEnd(THUMBS_UP_TONE + "x", 0));
        assertEquals(2, GraphemeClusters.nextClusterEnd(PLANE_VS16, 0));
    }

    @Test
    void floorClusterBoundaryRollsBackWhenLimitCutsACluster() {
        // Truncation point lands in the middle of a surrogate pair: roll back to before the pair.
        String text = "a" + GRINNING + "b"; // UTF-16 length 4, pair spans [1, 3)
        assertEquals(1, GraphemeClusters.floorClusterBoundary(text, 2));
        // Same for multi-codepoint clusters: any interior limit falls back to the cluster start.
        assertEquals(0, GraphemeClusters.floorClusterBoundary(FLAG, 2));
        assertEquals(0, GraphemeClusters.floorClusterBoundary(FLAG, 3));
        String prefixed = "a" + FAMILY + "b"; // family cluster spans [1, 9)
        assertEquals(1, GraphemeClusters.floorClusterBoundary(prefixed, 5));
        assertEquals(0, GraphemeClusters.floorClusterBoundary("e\u0301", 1));
        // Vanilla writeText scenario: room for 3 chars but the second emoji needs 2.
        assertEquals(2, GraphemeClusters.floorClusterBoundary(GRINNING + GRINNING, 3));
    }

    @Test
    void floorClusterBoundaryKeepsExactBoundariesAndExtremes() {
        String text = "a" + GRINNING + "b";
        assertEquals(0, GraphemeClusters.floorClusterBoundary(text, 0));
        assertEquals(1, GraphemeClusters.floorClusterBoundary(text, 1));
        assertEquals(3, GraphemeClusters.floorClusterBoundary(text, 3));
        assertEquals(4, GraphemeClusters.floorClusterBoundary(text, 4));
        assertEquals(4, GraphemeClusters.floorClusterBoundary(FLAG, 4));
        assertEquals(2, GraphemeClusters.floorClusterBoundary("abc", 2));
        // A limit at or beyond the text length resolves to the text length.
        assertEquals(2, GraphemeClusters.floorClusterBoundary("ab", 10));
        assertEquals(0, GraphemeClusters.floorClusterBoundary("", 0));
    }
}
