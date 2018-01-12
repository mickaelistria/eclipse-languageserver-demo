/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.chamrousse.ide.action;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.plugin.chamrousse.ide.ChamrousseLocalizationConstant;
import org.eclipse.che.plugin.chamrousse.ide.ChamrousseResources;

/**
 * Action to create new chamrousse source file.
 *
 * @author Florent Benoit
 */
@Singleton
public class CreateChamrousseFileAction extends AbstractNewResourceAction {

  @Inject
  public CreateChamrousseFileAction(
      ChamrousseLocalizationConstant localizationConstant,
      ChamrousseResources chamrousseResources,
      DialogFactory dialogFactory,
      CoreLocalizationConstant coreLocalizationConstant,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.createChamrousseFileActionTitle(),
        localizationConstant.createChamrousseFileActionDescription(),
        chamrousseResources.pythonFile(),
        dialogFactory,
        coreLocalizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
  }

  @Override
  protected String getExtension() {
    return "ski";
  }

  @Override
  protected String getDefaultContent() {
    return "";
  }
}
