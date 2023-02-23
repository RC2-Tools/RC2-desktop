/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.sharedlib.consts;

import java.awt.Color;
import java.awt.Dimension;

public class LayoutViewConsts {
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 400;

    public static final int SCROLLING_DIALOG_WIDTH = 400;
    public static final int SCROLLING_DIALOG_HEIGHT = 200;

    // banner
    public static final String BANNER_IMG_NAME = "/img/banner.jpg";
    public static final int BANNER_IMG_WIDTH = 563;

    // colors
    public static final Color IFRC_RED = new Color(220, 40, 30);
    public static final Color WHITE = new Color(255,255,255);

    // buttons
    public static final Dimension CTRL_BTN_DIM = new Dimension(40, 40);
    public static final String HOME_BTN_ICON = "/img/home_icon.jpg";
    public static final String BACK_BTN_ICON = "/img/back_arrow.jpg";
}
