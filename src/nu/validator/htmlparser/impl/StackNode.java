/*
 * Copyright (c) 2007 Henri Sivonen
 * Copyright (c) 2007-2008 Mozilla Foundation
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

package nu.validator.htmlparser.impl;

class StackNode<T> {
    final int group;

    final String name;

    final String popName;

    final String ns;

    final T node;

    final boolean scoping;

    final boolean special;

    final boolean fosterParenting;

    boolean tainted = false;

    /**
     * @param group
     *            TODO
     * @param name
     * @param node
     * @param scoping
     * @param special
     * @param popName
     *            TODO
     */
    StackNode(int group, final String ns, final String name, final T node,
            final boolean scoping, final boolean special,
            final boolean fosterParenting, String popName) {
        this.group = group;
        this.name = name;
        this.ns = ns;
        this.node = node;
        this.scoping = scoping;
        this.special = special;
        this.fosterParenting = fosterParenting;
        this.popName = popName;
    }

    /**
     * @param elementName
     *            TODO
     * @param node
     */
    StackNode(final String ns, ElementName elementName, final T node) {
        this.group = elementName.group;
        this.name = elementName.name;
        this.popName = elementName.name;
        this.ns = ns;
        this.node = node;
        this.scoping = elementName.scoping;
        this.special = elementName.special;
        this.fosterParenting = elementName.fosterParenting;
    }

    // [NOCPP[
    /**
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        return name;
    }
    // ]NOCPP]
}