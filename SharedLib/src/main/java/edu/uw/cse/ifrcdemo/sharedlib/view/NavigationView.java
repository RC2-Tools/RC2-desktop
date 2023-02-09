/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.sharedlib.view;

import edu.uw.cse.ifrcdemo.sharedlib.consts.LayoutViewConsts;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class NavigationView extends Box {

  private final JButton homeBtn;
  private final JButton backBtn;

  public JButton getHomeBtn() {
    return homeBtn;
  }

  public JButton getBackBtn() {
    return backBtn;
  }

  public NavigationView() {
    super(BoxLayout.Y_AXIS);

    setBackground(Color.WHITE);
    setBorder(new EmptyBorder(0,3,0,0));

    this.homeBtn = buildBtn(LayoutViewConsts.HOME_BTN_ICON);
    this.backBtn = buildBtn(LayoutViewConsts.BACK_BTN_ICON);

    add(homeBtn);
    add(backBtn);
  }

  private JButton buildBtn(String iconRes) {
    JButton btn = new JButton();

    btn.setEnabled(false);
    btn.setSize(LayoutViewConsts.CTRL_BTN_DIM);
    try {
      btn.setIcon(getResizedIcon(iconRes, LayoutViewConsts.CTRL_BTN_DIM));
    } catch (IOException e) {
      // should not happen
      e.printStackTrace();
    }

    return btn;
  }

  private ImageIcon getResizedIcon(String resName, Dimension dimension) throws IOException {
    Image image = ImageIO
        .read(getClass().getResource(resName))
        .getScaledInstance(dimension.width, dimension.height, Image.SCALE_SMOOTH);

    return new ImageIcon(image);
  }
}
