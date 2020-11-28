package com.wwc2.dvr.widget;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: CheckableLinearLayout.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb, jin/libuvc, jni/rapidjson folder may have a different license, see the respective files.
*/

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public final class CheckableRelativeLayout extends RelativeLayout implements Checkable {

	private boolean mChecked;

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	public CheckableRelativeLayout(final Context context) {
		this(context, null);
	}

	public CheckableRelativeLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(final boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
          /*  final int n = this.getChildCount();
            View v;
            for (int i = 0; i < n; i++) {
            	v = this.getChildAt(i);
            	if (v instanceof Checkable)
            		((Checkable)v).setChecked(checked);
            }
            refreshDrawableState();*/
          refreshDrawableState();
//			setBackgroundResource(checked ? R.mipmap.bg_select_file : R.mipmap.bg_item_nor);
        }
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

	@Override
    public int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

}
