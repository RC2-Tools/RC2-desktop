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
import edu.uw.cse.ifrcdemo.translations.LogStr;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainPanel extends JPanel {

  private final TrackingCardLayout layoutMngr;
  private final Logger logger;

  public TrackingCardLayout getLayoutMngr() {
    return layoutMngr;
  }

  public MainPanel(int width, int height) {
    super();

    this.layoutMngr = new TrackingCardLayout();
    this.logger = LogManager.getLogger(MainPanel.class);

    setLayout(layoutMngr);

    setPreferredSize(new Dimension(width, height));
    setBorder(BorderFactory.createLineBorder(LayoutViewConsts.IFRC_RED));
  }

  public void showCard(String name) {
    logger.debug(LogStr.LOG_SHOW_CARD, name);

    getLayoutMngr().show(this, name);
  }

  public void showPrevious() {
    logger.debug(LogStr.LOG_SHOW_PREVIOUS);

    getLayoutMngr().previous(this);
  }
}
