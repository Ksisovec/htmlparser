/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2008 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.htmlparser.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class Encoding {

    public static final Encoding UTF8;

    public static final Encoding UTF16;

    public static final Encoding UTF16LE;

    public static final Encoding UTF16BE;

    public static final Encoding WINDOWS1252;

    private static Map<String, Encoding> encodingByLabel =
        new HashMap<String, Encoding>();

    private final String canonName;

    private final Charset charset;

    static {
        Set<Encoding> encodings = new HashSet<Encoding>();

        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        for (Map.Entry<String, Charset> entry : charsets.entrySet()) {
            Charset cs = entry.getValue();
            String name = toNameKey(cs.name());
            String canonName = toAsciiLowerCase(cs.name());
            if (!isBanned(name)) {
                name = name.intern();
                boolean asciiSuperset = asciiMapsToBasicLatin(testBuf, cs);
                Encoding enc = new Encoding(canonName.intern(), cs,
                        asciiSuperset, isObscure(name), isShouldNot(name),
                        isLikelyEbcdic(name, asciiSuperset));
                encodings.add(enc);
                Set<String> aliases = cs.aliases();
                for (String alias : aliases) {
                    encodingByLabel.put(toNameKey(alias).intern(), enc);
                }
            }
        }
        // Overwrite possible overlapping aliases with the real things--just in
        // case
        for (Encoding encoding : encodings) {
            encodingByLabel.put(toNameKey(encoding.getCanonName()),
                    encoding);
        }
        UTF8 = forName("utf-8");
        UTF16 = forName("utf-16");
        UTF16BE = forName("utf-16be");
        UTF16LE = forName("utf-16le");
        WINDOWS1252 = forName("windows-1252");
    }

    public static Encoding forName(String name) {
        Encoding rv = encodingByLabel.get(toNameKey(name));
        if (rv == null) {
            throw new UnsupportedCharsetException(name);
        } else {
            return rv;
        }
    }

    public static String toNameKey(String str) {
        if (str == null) {
            return null;
        }
        int j = 0;
        char[] buf = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 0x20;
            }
            if (!((c >= '\t' && c <= '\r') || (c >= '\u0020' && c <= '\u002F')
                    || (c >= '\u003A' && c <= '\u0040')
                    || (c >= '\u005B' && c <= '\u0060') || (c >= '\u007B' && c <= '\u007E'))) {
                buf[j] = c;
                j++;
            }
        }
        return new String(buf, 0, j);
    }

    public static String toAsciiLowerCase(String str) {
        if (str == null) {
            return null;
        }
        char[] buf = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 0x20;
            }
            buf[i] = c;
        }
        return new String(buf);
    }

    /**
     * @param canonName
     * @param charset
     */
    private Encoding(final String canonName, final Charset charset) {
        this.canonName = canonName;
        this.charset = charset;
    }

    /**
     * Returns the canonName.
     * 
     * @return the canonName
     */
    public String getCanonName() {
        return canonName;
    }

    /**
     * @return
     * @see java.nio.charset.Charset#canEncode()
     */
    public boolean canEncode() {
        return charset.canEncode();
    }

    /**
     * @return
     * @see java.nio.charset.Charset#newDecoder()
     */
    public CharsetDecoder newDecoder() {
        return charset.newDecoder();
    }

    /**
     * @return
     * @see java.nio.charset.Charset#newEncoder()
     */
    public CharsetEncoder newEncoder() {
        return charset.newEncoder();
    }

    protected static String msgLegacyEncoding(String name) {
        return "Legacy encoding \u201C" + name + "\u201D used. Documents must"
                + " use UTF-8.";
    }

    protected static String msgIgnoredCharset(String ignored, String name) {
        return "Internal encoding declaration specified \u201C" + ignored
                + "\u201D. Continuing as if the encoding had been \u201C"
                + name + "\u201D.";
    }
    protected static String msgNotCanonicalName(String label, String name) {
        return "The encoding \u201C" + label + "\u201D is not the canonical"
                + " name of the character encoding in use. The canonical name"
                + " is \u201C" + name + "\u201D. (Charmod C024)";
    }

    protected static String msgBadInternalCharset(String internalCharset) {
        return "Internal encoding declaration named an unsupported character"
            + " encoding \u201C" + internalCharset + "\u201D.";
    }

    protected static String msgBadEncoding(String name) {
        return "Unsupported character encoding name: \u201C" + name + "\u201D.";
    }

    public static void main(String[] args) {
        for (Map.Entry<String, Encoding> entry : encodingByLabel.entrySet()) {
            String name = entry.getKey();
            Encoding enc = entry.getValue();
            System.out.printf("%21s: canon %13s\n", name, enc.getCanonName());
        }
    }

}
