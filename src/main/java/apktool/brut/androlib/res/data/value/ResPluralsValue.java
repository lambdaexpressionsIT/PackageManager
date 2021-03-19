/*
 *  Copyright (C) 2010 Ryszard Wiśniewski <apktool.brut.alll@gmail.com>
 *  Copyright (C) 2010 Connor Tumbleson <connor.tumbleson@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package apktool.brut.androlib.res.data.value;

import apktool.brut.androlib.AndrolibException;
import apktool.brut.androlib.res.data.ResResource;
import apktool.brut.androlib.res.xml.ResValuesXmlSerializable;
import apktool.brut.androlib.res.xml.ResXmlEncoders;
import apktool.brut.util.Duo;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class ResPluralsValue extends ResBagValue implements
        ResValuesXmlSerializable {
    ResPluralsValue(ResReferenceValue parent,
                    Duo<Integer, ResScalarValue>[] items) {
        super(parent);

        mItems = new ResScalarValue[6];
        for (int i = 0; i < items.length; i++) {
            mItems[items[i].m1 - BAG_KEY_PLURALS_START] = items[i].m2;
        }
    }

    @Override
    public void serializeToResValuesXml(XmlSerializer serializer,
                                        ResResource res) throws IOException, AndrolibException {
        serializer.startTag(null, "plurals");
        serializer.attribute(null, "name", res.getResSpec().getName());
        for (int i = 0; i < mItems.length; i++) {
            ResScalarValue item = mItems[i];
            if (item == null) {
                continue;
            }

            serializer.startTag(null, "item");
            serializer.attribute(null, "quantity", QUANTITY_MAP[i]);
            serializer.text(ResXmlEncoders.enumerateNonPositionalSubstitutionsIfRequired(item.encodeAsResXmlNonEscapedItemValue()));
            serializer.endTag(null, "item");
        }
        serializer.endTag(null, "plurals");
    }

    private final ResScalarValue[] mItems;

    public static final int BAG_KEY_PLURALS_START = 0x01000004;
    public static final int BAG_KEY_PLURALS_END = 0x01000009;
    private static final String[] QUANTITY_MAP = new String[] { "other", "zero", "one", "two", "few", "many" };
}
