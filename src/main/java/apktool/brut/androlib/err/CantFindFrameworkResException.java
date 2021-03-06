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
package apktool.brut.androlib.err;

import apktool.brut.androlib.AndrolibException;

public class CantFindFrameworkResException extends AndrolibException {
	public CantFindFrameworkResException(int id) {
		mPkgId = id;
	}

	public int getPkgId() {
		return mPkgId;
	}

	private final int mPkgId;
}
