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
import apktool.brut.androlib.err.UndefinedResObjectException;
import apktool.brut.androlib.res.data.ResPackage;
import apktool.brut.androlib.res.data.ResResSpec;

public class ResReferenceValue extends ResIntValue {
    private final ResPackage mPackage;
    private final boolean mTheme;

    public ResReferenceValue(ResPackage package_, int value, String rawValue) {
        this(package_, value, rawValue, false);
    }

    public ResReferenceValue(ResPackage package_, int value, String rawValue,
                             boolean theme) {
        super(value, rawValue, "reference");
        mPackage = package_;
        mTheme = theme;
    }

    @Override
    protected String encodeAsResXml() throws AndrolibException {
        if (isNull()) {
            return "@null";
        }

        ResResSpec spec = getReferent();
        if (spec == null) {
            return "@null";
        }
        boolean newId = spec.hasDefaultResource() && spec.getDefaultResource().getValue() instanceof ResIdValue;

        // generate the beginning to fix @apktool.android
        String mStart = (mTheme ? '?' : '@') + (newId ? "+" : "");

        return mStart + spec.getFullName(mPackage, mTheme && spec.getType().getName().equals("attr"));
    }

    public ResResSpec getReferent() throws AndrolibException {
        try {
            return mPackage.getResTable().getResSpec(getValue());
        } catch (UndefinedResObjectException ex) {
            return null;
        }
    }

    public boolean isNull() {
        return mValue == 0;
    }

    public boolean referentIsNull() throws AndrolibException {
        return getReferent() == null;
    }
}