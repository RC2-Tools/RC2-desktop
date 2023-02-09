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


import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class NavigationPresenter {
  private final MainPanel mainPanel;
  private final JPanel homePanel;
  private final NavigationView navigationView;

  public MainPanel getMainPanel() {
    return mainPanel;
  }

  public JPanel getHomePanel() {
    return homePanel;
  }

  public NavigationView getNavigationView() {
    return navigationView;
  }

  public NavigationPresenter(MainPanel mainPanel, JPanel homePanel, NavigationView navigationView, String homeToken) {
    this.mainPanel = mainPanel;
    this.homePanel = homePanel;
    this.navigationView = navigationView;

    registerButtonListener(homeToken);
    registerHomeComponentListener();

    setEnableBtn(false);
  }

  private void registerButtonListener(String homeToken) {
    getNavigationView().getHomeBtn().addActionListener(e -> getMainPanel().showCard(homeToken));
    getNavigationView().getBackBtn().addActionListener(e -> getMainPanel().showPrevious());
  }

  /**
   * Turn buttons off when on home panel
   */
  private void registerHomeComponentListener() {
    getHomePanel().addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorAdded(AncestorEvent event) {
        setEnableBtn(false);
      }

      @Override
      public void ancestorRemoved(AncestorEvent event) {
        setEnableBtn(true);
      }

      @Override
      public void ancestorMoved(AncestorEvent event) {}
    });
  }

  private void setEnableBtn(boolean enable) {
    getNavigationView().getHomeBtn().setEnabled(enable);
    getNavigationView().getBackBtn().setEnabled(enable);
  }
}
