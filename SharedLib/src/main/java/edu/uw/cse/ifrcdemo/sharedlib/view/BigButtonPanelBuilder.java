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
import edu.uw.cse.ifrcdemo.sharedlib.util.SwingUtil;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;

public class BigButtonPanelBuilder {
  private static final int BTN_H_GAP = 0;
  private static final int BTN_V_GAP = 30;

  public static JPanel buildPanel(List<JButton> buttons, int btnLength) {
    Dimension btnDim = new Dimension(btnLength, btnLength);

    final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, BTN_H_GAP, BTN_V_GAP));
    panel.setOpaque(true);
    panel.setBackground(LayoutViewConsts.IFRC_RED);

    buttons.forEach(btn -> {
      // so text wraps to next line
      btn.setText(SwingUtil.wrapStringWithCenter(btn.getText()));

      btn.setMaximumSize(btnDim);
      btn.setMinimumSize(btnDim);
      btn.setPreferredSize(btnDim);

      btn.setAlignmentX(Component.CENTER_ALIGNMENT);

      panel.add(btn);
    });

    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.setBackground(LayoutViewConsts.IFRC_RED);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;

    wrapper.add(panel, gbc);
    return wrapper;
  }
}
