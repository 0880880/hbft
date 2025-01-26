package com.github.zeroeighteightzero.newtext;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Disposable;

import java.nio.charset.StandardCharsets;

public class Harfbuzz {

    // @off
	/*JNI
	    #include <hb.h>
	    #include <hb-ft.h>
	    #include <ft2build.h>
	    #include FT_FREETYPE_H
	 */

    public static class Buffer implements Disposable {
        private final long address;

        private Buffer(long address) {
            this.address = address;
        }

        public void addText(String text) {
            bufferAddUTF8Text(address, text.getBytes(StandardCharsets.UTF_8));
        }

        public void setDirection(int direction) {
            bufferSetDirection(address, direction);
        }

        public void setScript(int script) {
            bufferSetScript(address, script);
        }

        public void setLanguage(long language) {
            bufferSetLanguage(address, language);
        }

        public void setLanguage(String language) {
            bufferSetLanguage(address, languageFromString(language));
        }

        public void guessSegmentProperties() {
            bufferGuessSegmentProperties(address);
        }

        public GlyphInfo[] getGlyphInfos() {
            long[] glyphInfosAddr = bufferGetGlyphInfos(address);
            GlyphInfo[] glyphInfos = new GlyphInfo[glyphInfosAddr.length];
            for (int i = 0; i < glyphInfos.length; i++) {
                glyphInfos[i] = new GlyphInfo(glyphInfosAddr[i]);
            }
            return glyphInfos;
        }

        public GlyphPosition[] getGlyphPositions() {
            long[] glyphPositionsAddr = bufferGetGlyphPositions(address);
            GlyphPosition[] glyphPositions = new GlyphPosition[glyphPositionsAddr.length];
            for (int i = 0; i < glyphPositions.length; i++) {
                glyphPositions[i] = new GlyphPosition(glyphPositionsAddr[i]);
            }
            return glyphPositions;
        }

        @Override
        public void dispose() {
            bufferDestroy(address);
        }
    }

    public static class GlyphInfo {
        private final long address;

        public final int codepoint;

        public GlyphInfo(long address) {
            this.address = address;
            this.codepoint = Harfbuzz.glyphInfoCodepoint(address);
        }
    }

    public static class GlyphPosition {
        private final long address;

        public final GridPoint2 offset;
        public final GridPoint2 advance;

        public GlyphPosition(long address) {
            this.address = address;
            this.offset = new GridPoint2(Harfbuzz.glyphPositionXOffset(address), Harfbuzz.glyphPositionYOffset(address));
            this.advance = new GridPoint2(Harfbuzz.glyphPositionXAdvance(address), Harfbuzz.glyphPositionYAdvance(address));
        }
    }

    public static class Blob implements Disposable {
        private final long address;

        private Blob(long address) {
            this.address = address;
        }

        public Face createFace(int index) {
            return new Face(Harfbuzz.createFace(address, index));
        }

        public Face createFace() {
            return new Face(Harfbuzz.createFace(address, 0));
        }

        @Override
        public void dispose() {
            blobDestroy(address);
        }
    }

    public static class Face implements Disposable {
        private final long address;

        private Face(long address) {
            this.address = address;
        }

        public Font createFont() {
            return new Font(Harfbuzz.createFont(address));
        }

        public int getUpem() {
            return faceGetUpem(address);
        }

        @Override
        public void dispose() {
            faceDestroy(address);
        }
    }

    public static class Font implements Disposable {
        private final long address;

        private Font(long address) {
            this.address = address;
        }

        public void setScale(int xScale, int yScale) {
            fontSetScale(address, xScale, yScale);
        }

        public void setScalePixels(int fontSize, int upem) {
            fontSetScale(address, fontSize * upem, fontSize * upem);
        }

        public void shape(Buffer buffer) {
            Harfbuzz.shape(address, buffer.address);
        }

        @Override
        public void dispose() {
            fontDestroy(address);
        }
    }

    private static native void bufferAddUTF8Text(long address, byte[] text); /*
        hb_buffer_add_utf8((hb_buffer_t*) address, text, -1, 0, -1);
    */

    private static native void bufferDestroy(long address); /*
        hb_buffer_destroy((hb_buffer_t*) address);
    */

    private static native void bufferSetDirection(long address, int direction); /*
        hb_buffer_set_direction((hb_buffer_t*) address, (hb_direction_t) direction);
    */

    private static native void bufferSetScript(long address, int script); /*
        hb_buffer_set_script((hb_buffer_t*) address, (hb_script_t) script);
    */

    private static native void bufferSetLanguage(long address, long language); /*
        hb_buffer_set_language((hb_buffer_t*) address, (hb_language_t) language);
    */

    private static native void bufferGuessSegmentProperties(long address); /*
        hb_buffer_guess_segment_properties((hb_buffer_t*) address);
    */

    private static native long[] bufferGetGlyphInfos(long address); /*
        hb_buffer_t* v = ((hb_buffer_t *) address);
        unsigned int glyph_count;
        hb_glyph_info_t* glyph_infos = hb_buffer_get_glyph_infos(v, &glyph_count);
        jlongArray arr = env->NewLongArray(glyph_count);
        jlong* ptr = env->GetLongArrayElements(arr, NULL);
        for (int i = 0; i < glyph_count; i++) {
            ptr[i] = (jlong) &glyph_infos[i];
        }
        env->ReleaseLongArrayElements(arr, ptr, 0);
        return arr;
    */

    private static native int glyphInfoCodepoint(long address); /*
        return ((hb_glyph_info_t*) address)->codepoint;
    */

    private static native int glyphPositionXOffset(long address); /*
        return ((hb_glyph_position_t*) address)->x_offset;
    */

    private static native int glyphPositionYOffset(long address); /*
        return ((hb_glyph_position_t*) address)->y_offset;
    */

    private static native int glyphPositionXAdvance(long address); /*
        return ((hb_glyph_position_t*) address)->x_advance;
    */

    private static native int glyphPositionYAdvance(long address); /*
        return ((hb_glyph_position_t*) address)->y_advance;
    */

    private static native long[] bufferGetGlyphPositions(long address); /*
        hb_buffer_t* v = ((hb_buffer_t *) address);
        unsigned int glyph_count;
        hb_glyph_position_t* glyph_positions = hb_buffer_get_glyph_positions(v, &glyph_count);
        jlongArray arr = env->NewLongArray(glyph_count);
        jlong* ptr = env->GetLongArrayElements(arr, NULL);
        for (int i = 0; i < glyph_count; i++) {
            ptr[i] = (jlong) &glyph_positions[i];
        }
        env->ReleaseLongArrayElements(arr, ptr, 0);
        return arr;
    */

    private static native void blobDestroy(long address); /*
        hb_blob_destroy((hb_blob_t*) address);
    */

    private static native long createFace(long address, int index); /*
        return (jlong) hb_face_create((hb_blob_t*) address, index);
    */

    private static native int faceGetUpem(long address); /*
        return hb_face_get_upem((hb_face_t*) address);
    */

    private static native void faceDestroy(long address); /*
        hb_face_destroy((hb_face_t*) address);
    */

    private static native long createFont(long address); /*
        return (jlong) hb_font_create((hb_face_t*) address);
    */

    private static native void fontSetScale(long address, int xScale, int yScale); /*
        hb_font_set_scale((hb_font_t*) address, xScale, yScale);
    */

    private static native void fontDestroy(long address); /*
        hb_font_destroy((hb_font_t*) address);
    */

    private static native void shape(long font, long buffer); /*
        hb_shape((hb_font_t*) font, (hb_buffer_t*) buffer, NULL, 0);
    */

    public static long languageFromString(String text) {
        return languageFromStringJni(text.getBytes(StandardCharsets.UTF_8));
    }

    private static native long languageFromStringJni(byte[] text); /*
        return (jlong) hb_language_from_string(text, -1);
    */

    public static Buffer createBuffer() {
        long address = createBufferJni();
        if (address == 0) {
            throw new RuntimeException("Failed to create Harfbuzz buffer.");
        }
        return new Buffer(address);
    }

    private static native long createBufferJni(); /*
        hb_buffer_t *buf;
        buf = hb_buffer_create();
        return (jlong) buf;
    */

    public static Blob createBlob(byte[] data) {
        long address = createBlobJni(data, data.length, 1, 0, 0);
        if (address == 0) {
            throw new RuntimeException("Failed to create Harfbuzz buffer.");
        }
        return new Blob(address);
    }

    public static Blob createBlob(FileHandle file) {
        return createBlob(file.readBytes());
    }

    private static native long createBlobJni(byte[] blob, int size, int mode, long userData, long destroyFunc); /*
        return (jlong) hb_blob_create(blob, size, (hb_memory_mode_t) mode, (void*) (uintptr_t) userData, (hb_destroy_func_t) destroyFunc);
    */

    public static Font createFTFont(long ftFaceAddress) {
        return new Font(createFTFontJni(ftFaceAddress));
    }

    private static native long createFTFontJni(long ftFaceAddress); /*
        return (jlong) hb_ft_font_create_referenced((FT_Face) ftFaceAddress);
    */


    // From LWJGL3

    public static int HB_TAG(int c1, int c2, int c3, int c4) {
        return ((c1 & 0xFF) << 24) | ((c2 & 0xFF)<< 16) | ((c3 & 0xFF) << 8) | (c4 & 0xFF);
    }

    public static final int
        HB_DIRECTION_INVALID = 0,
        HB_DIRECTION_LTR     = 4,
        HB_DIRECTION_RTL     = 5,
        HB_DIRECTION_TTB     = 6,
        HB_DIRECTION_BTT     = 7;

    public static final int
        HB_SCRIPT_COMMON                 = HB_TAG ('Z','y','y','y'),
        HB_SCRIPT_INHERITED              = HB_TAG ('Z','i','n','h'),
        HB_SCRIPT_UNKNOWN                = HB_TAG ('Z','z','z','z'),
        HB_SCRIPT_ARABIC                 = HB_TAG ('A','r','a','b'),
        HB_SCRIPT_ARMENIAN               = HB_TAG ('A','r','m','n'),
        HB_SCRIPT_BENGALI                = HB_TAG ('B','e','n','g'),
        HB_SCRIPT_CYRILLIC               = HB_TAG ('C','y','r','l'),
        HB_SCRIPT_DEVANAGARI             = HB_TAG ('D','e','v','a'),
        HB_SCRIPT_GEORGIAN               = HB_TAG ('G','e','o','r'),
        HB_SCRIPT_GREEK                  = HB_TAG ('G','r','e','k'),
        HB_SCRIPT_GUJARATI               = HB_TAG ('G','u','j','r'),
        HB_SCRIPT_GURMUKHI               = HB_TAG ('G','u','r','u'),
        HB_SCRIPT_HANGUL                 = HB_TAG ('H','a','n','g'),
        HB_SCRIPT_HAN                    = HB_TAG ('H','a','n','i'),
        HB_SCRIPT_HEBREW                 = HB_TAG ('H','e','b','r'),
        HB_SCRIPT_HIRAGANA               = HB_TAG ('H','i','r','a'),
        HB_SCRIPT_KANNADA                = HB_TAG ('K','n','d','a'),
        HB_SCRIPT_KATAKANA               = HB_TAG ('K','a','n','a'),
        HB_SCRIPT_LAO                    = HB_TAG ('L','a','o','o'),
        HB_SCRIPT_LATIN                  = HB_TAG ('L','a','t','n'),
        HB_SCRIPT_MALAYALAM              = HB_TAG ('M','l','y','m'),
        HB_SCRIPT_ORIYA                  = HB_TAG ('O','r','y','a'),
        HB_SCRIPT_TAMIL                  = HB_TAG ('T','a','m','l'),
        HB_SCRIPT_TELUGU                 = HB_TAG ('T','e','l','u'),
        HB_SCRIPT_THAI                   = HB_TAG ('T','h','a','i'),
        HB_SCRIPT_TIBETAN                = HB_TAG ('T','i','b','t'),
        HB_SCRIPT_BOPOMOFO               = HB_TAG ('B','o','p','o'),
        HB_SCRIPT_BRAILLE                = HB_TAG ('B','r','a','i'),
        HB_SCRIPT_CANADIAN_SYLLABICS     = HB_TAG ('C','a','n','s'),
        HB_SCRIPT_CHEROKEE               = HB_TAG ('C','h','e','r'),
        HB_SCRIPT_ETHIOPIC               = HB_TAG ('E','t','h','i'),
        HB_SCRIPT_KHMER                  = HB_TAG ('K','h','m','r'),
        HB_SCRIPT_MONGOLIAN              = HB_TAG ('M','o','n','g'),
        HB_SCRIPT_MYANMAR                = HB_TAG ('M','y','m','r'),
        HB_SCRIPT_OGHAM                  = HB_TAG ('O','g','a','m'),
        HB_SCRIPT_RUNIC                  = HB_TAG ('R','u','n','r'),
        HB_SCRIPT_SINHALA                = HB_TAG ('S','i','n','h'),
        HB_SCRIPT_SYRIAC                 = HB_TAG ('S','y','r','c'),
        HB_SCRIPT_THAANA                 = HB_TAG ('T','h','a','a'),
        HB_SCRIPT_YI                     = HB_TAG ('Y','i','i','i'),
        HB_SCRIPT_DESERET                = HB_TAG ('D','s','r','t'),
        HB_SCRIPT_GOTHIC                 = HB_TAG ('G','o','t','h'),
        HB_SCRIPT_OLD_ITALIC             = HB_TAG ('I','t','a','l'),
        HB_SCRIPT_BUHID                  = HB_TAG ('B','u','h','d'),
        HB_SCRIPT_HANUNOO                = HB_TAG ('H','a','n','o'),
        HB_SCRIPT_TAGALOG                = HB_TAG ('T','g','l','g'),
        HB_SCRIPT_TAGBANWA               = HB_TAG ('T','a','g','b'),
        HB_SCRIPT_CYPRIOT                = HB_TAG ('C','p','r','t'),
        HB_SCRIPT_LIMBU                  = HB_TAG ('L','i','m','b'),
        HB_SCRIPT_LINEAR_B               = HB_TAG ('L','i','n','b'),
        HB_SCRIPT_OSMANYA                = HB_TAG ('O','s','m','a'),
        HB_SCRIPT_SHAVIAN                = HB_TAG ('S','h','a','w'),
        HB_SCRIPT_TAI_LE                 = HB_TAG ('T','a','l','e'),
        HB_SCRIPT_UGARITIC               = HB_TAG ('U','g','a','r'),
        HB_SCRIPT_BUGINESE               = HB_TAG ('B','u','g','i'),
        HB_SCRIPT_COPTIC                 = HB_TAG ('C','o','p','t'),
        HB_SCRIPT_GLAGOLITIC             = HB_TAG ('G','l','a','g'),
        HB_SCRIPT_KHAROSHTHI             = HB_TAG ('K','h','a','r'),
        HB_SCRIPT_NEW_TAI_LUE            = HB_TAG ('T','a','l','u'),
        HB_SCRIPT_OLD_PERSIAN            = HB_TAG ('X','p','e','o'),
        HB_SCRIPT_SYLOTI_NAGRI           = HB_TAG ('S','y','l','o'),
        HB_SCRIPT_TIFINAGH               = HB_TAG ('T','f','n','g'),
        HB_SCRIPT_BALINESE               = HB_TAG ('B','a','l','i'),
        HB_SCRIPT_CUNEIFORM              = HB_TAG ('X','s','u','x'),
        HB_SCRIPT_NKO                    = HB_TAG ('N','k','o','o'),
        HB_SCRIPT_PHAGS_PA               = HB_TAG ('P','h','a','g'),
        HB_SCRIPT_PHOENICIAN             = HB_TAG ('P','h','n','x'),
        HB_SCRIPT_CARIAN                 = HB_TAG ('C','a','r','i'),
        HB_SCRIPT_CHAM                   = HB_TAG ('C','h','a','m'),
        HB_SCRIPT_KAYAH_LI               = HB_TAG ('K','a','l','i'),
        HB_SCRIPT_LEPCHA                 = HB_TAG ('L','e','p','c'),
        HB_SCRIPT_LYCIAN                 = HB_TAG ('L','y','c','i'),
        HB_SCRIPT_LYDIAN                 = HB_TAG ('L','y','d','i'),
        HB_SCRIPT_OL_CHIKI               = HB_TAG ('O','l','c','k'),
        HB_SCRIPT_REJANG                 = HB_TAG ('R','j','n','g'),
        HB_SCRIPT_SAURASHTRA             = HB_TAG ('S','a','u','r'),
        HB_SCRIPT_SUNDANESE              = HB_TAG ('S','u','n','d'),
        HB_SCRIPT_VAI                    = HB_TAG ('V','a','i','i'),
        HB_SCRIPT_AVESTAN                = HB_TAG ('A','v','s','t'),
        HB_SCRIPT_BAMUM                  = HB_TAG ('B','a','m','u'),
        HB_SCRIPT_EGYPTIAN_HIEROGLYPHS   = HB_TAG ('E','g','y','p'),
        HB_SCRIPT_IMPERIAL_ARAMAIC       = HB_TAG ('A','r','m','i'),
        HB_SCRIPT_INSCRIPTIONAL_PAHLAVI  = HB_TAG ('P','h','l','i'),
        HB_SCRIPT_INSCRIPTIONAL_PARTHIAN = HB_TAG ('P','r','t','i'),
        HB_SCRIPT_JAVANESE               = HB_TAG ('J','a','v','a'),
        HB_SCRIPT_KAITHI                 = HB_TAG ('K','t','h','i'),
        HB_SCRIPT_LISU                   = HB_TAG ('L','i','s','u'),
        HB_SCRIPT_MEETEI_MAYEK           = HB_TAG ('M','t','e','i'),
        HB_SCRIPT_OLD_SOUTH_ARABIAN      = HB_TAG ('S','a','r','b'),
        HB_SCRIPT_OLD_TURKIC             = HB_TAG ('O','r','k','h'),
        HB_SCRIPT_SAMARITAN              = HB_TAG ('S','a','m','r'),
        HB_SCRIPT_TAI_THAM               = HB_TAG ('L','a','n','a'),
        HB_SCRIPT_TAI_VIET               = HB_TAG ('T','a','v','t'),
        HB_SCRIPT_BATAK                  = HB_TAG ('B','a','t','k'),
        HB_SCRIPT_BRAHMI                 = HB_TAG ('B','r','a','h'),
        HB_SCRIPT_MANDAIC                = HB_TAG ('M','a','n','d'),
        HB_SCRIPT_CHAKMA                 = HB_TAG ('C','a','k','m'),
        HB_SCRIPT_MEROITIC_CURSIVE       = HB_TAG ('M','e','r','c'),
        HB_SCRIPT_MEROITIC_HIEROGLYPHS   = HB_TAG ('M','e','r','o'),
        HB_SCRIPT_MIAO                   = HB_TAG ('P','l','r','d'),
        HB_SCRIPT_SHARADA                = HB_TAG ('S','h','r','d'),
        HB_SCRIPT_SORA_SOMPENG           = HB_TAG ('S','o','r','a'),
        HB_SCRIPT_TAKRI                  = HB_TAG ('T','a','k','r'),
        HB_SCRIPT_BASSA_VAH              = HB_TAG ('B','a','s','s'),
        HB_SCRIPT_CAUCASIAN_ALBANIAN     = HB_TAG ('A','g','h','b'),
        HB_SCRIPT_DUPLOYAN               = HB_TAG ('D','u','p','l'),
        HB_SCRIPT_ELBASAN                = HB_TAG ('E','l','b','a'),
        HB_SCRIPT_GRANTHA                = HB_TAG ('G','r','a','n'),
        HB_SCRIPT_KHOJKI                 = HB_TAG ('K','h','o','j'),
        HB_SCRIPT_KHUDAWADI              = HB_TAG ('S','i','n','d'),
        HB_SCRIPT_LINEAR_A               = HB_TAG ('L','i','n','a'),
        HB_SCRIPT_MAHAJANI               = HB_TAG ('M','a','h','j'),
        HB_SCRIPT_MANICHAEAN             = HB_TAG ('M','a','n','i'),
        HB_SCRIPT_MENDE_KIKAKUI          = HB_TAG ('M','e','n','d'),
        HB_SCRIPT_MODI                   = HB_TAG ('M','o','d','i'),
        HB_SCRIPT_MRO                    = HB_TAG ('M','r','o','o'),
        HB_SCRIPT_NABATAEAN              = HB_TAG ('N','b','a','t'),
        HB_SCRIPT_OLD_NORTH_ARABIAN      = HB_TAG ('N','a','r','b'),
        HB_SCRIPT_OLD_PERMIC             = HB_TAG ('P','e','r','m'),
        HB_SCRIPT_PAHAWH_HMONG           = HB_TAG ('H','m','n','g'),
        HB_SCRIPT_PALMYRENE              = HB_TAG ('P','a','l','m'),
        HB_SCRIPT_PAU_CIN_HAU            = HB_TAG ('P','a','u','c'),
        HB_SCRIPT_PSALTER_PAHLAVI        = HB_TAG ('P','h','l','p'),
        HB_SCRIPT_SIDDHAM                = HB_TAG ('S','i','d','d'),
        HB_SCRIPT_TIRHUTA                = HB_TAG ('T','i','r','h'),
        HB_SCRIPT_WARANG_CITI            = HB_TAG ('W','a','r','a'),
        HB_SCRIPT_AHOM                   = HB_TAG ('A','h','o','m'),
        HB_SCRIPT_ANATOLIAN_HIEROGLYPHS  = HB_TAG ('H','l','u','w'),
        HB_SCRIPT_HATRAN                 = HB_TAG ('H','a','t','r'),
        HB_SCRIPT_MULTANI                = HB_TAG ('M','u','l','t'),
        HB_SCRIPT_OLD_HUNGARIAN          = HB_TAG ('H','u','n','g'),
        HB_SCRIPT_SIGNWRITING            = HB_TAG ('S','g','n','w'),
        HB_SCRIPT_ADLAM                  = HB_TAG ('A','d','l','m'),
        HB_SCRIPT_BHAIKSUKI              = HB_TAG ('B','h','k','s'),
        HB_SCRIPT_MARCHEN                = HB_TAG ('M','a','r','c'),
        HB_SCRIPT_OSAGE                  = HB_TAG ('O','s','g','e'),
        HB_SCRIPT_TANGUT                 = HB_TAG ('T','a','n','g'),
        HB_SCRIPT_NEWA                   = HB_TAG ('N','e','w','a'),
        HB_SCRIPT_MASARAM_GONDI          = HB_TAG ('G','o','n','m'),
        HB_SCRIPT_NUSHU                  = HB_TAG ('N','s','h','u'),
        HB_SCRIPT_SOYOMBO                = HB_TAG ('S','o','y','o'),
        HB_SCRIPT_ZANABAZAR_SQUARE       = HB_TAG ('Z','a','n','b'),
        HB_SCRIPT_DOGRA                  = HB_TAG ('D','o','g','r'),
        HB_SCRIPT_GUNJALA_GONDI          = HB_TAG ('G','o','n','g'),
        HB_SCRIPT_HANIFI_ROHINGYA        = HB_TAG ('R','o','h','g'),
        HB_SCRIPT_MAKASAR                = HB_TAG ('M','a','k','a'),
        HB_SCRIPT_MEDEFAIDRIN            = HB_TAG ('M','e','d','f'),
        HB_SCRIPT_OLD_SOGDIAN            = HB_TAG ('S','o','g','o'),
        HB_SCRIPT_SOGDIAN                = HB_TAG ('S','o','g','d'),
        HB_SCRIPT_ELYMAIC                = HB_TAG ('E','l','y','m'),
        HB_SCRIPT_NANDINAGARI            = HB_TAG ('N','a','n','d'),
        HB_SCRIPT_NYIAKENG_PUACHUE_HMONG = HB_TAG ('H','m','n','p'),
        HB_SCRIPT_WANCHO                 = HB_TAG ('W','c','h','o'),
        HB_SCRIPT_CHORASMIAN             = HB_TAG ('C','h','r','s'),
        HB_SCRIPT_DIVES_AKURU            = HB_TAG ('D','i','a','k'),
        HB_SCRIPT_KHITAN_SMALL_SCRIPT    = HB_TAG ('K','i','t','s'),
        HB_SCRIPT_YEZIDI                 = HB_TAG ('Y','e','z','i'),
        HB_SCRIPT_CYPRO_MINOAN           = HB_TAG ('C','p','m','n'),
        HB_SCRIPT_OLD_UYGHUR             = HB_TAG ('O','u','g','r'),
        HB_SCRIPT_TANGSA                 = HB_TAG ('T','n','s','a'),
        HB_SCRIPT_TOTO                   = HB_TAG ('T','o','t','o'),
        HB_SCRIPT_VITHKUQI               = HB_TAG ('V','i','t','h'),
        HB_SCRIPT_MATH                   = HB_TAG ('Z','m','t','h'),
        HB_SCRIPT_KAWI                   = HB_TAG ('K','a','w','i'),
        HB_SCRIPT_NAG_MUNDARI            = HB_TAG ('N','a','g','m'),
        HB_SCRIPT_GARAY                  = HB_TAG ('G','a','r','a'),
        HB_SCRIPT_GURUNG_KHEMA           = HB_TAG ('G','u','k','h'),
        HB_SCRIPT_KIRAT_RAI              = HB_TAG ('K','r','a','i'),
        HB_SCRIPT_OL_ONAL                = HB_TAG ('O','n','a','o'),
        HB_SCRIPT_SUNUWAR                = HB_TAG ('S','u','n','u'),
        HB_SCRIPT_TODHRI                 = HB_TAG ('T','o','d','r'),
        HB_SCRIPT_TULU_TIGALARI          = HB_TAG ('T','u','t','g'),
        HB_SCRIPT_INVALID                = HB_TAG('\u0000', '\u0000', '\u0000', '\u0000');

}
