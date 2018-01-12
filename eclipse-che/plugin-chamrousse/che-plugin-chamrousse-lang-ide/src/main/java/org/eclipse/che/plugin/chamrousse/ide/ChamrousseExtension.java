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
package org.eclipse.che.plugin.chamrousse.ide;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_CATEGORY;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.chamrousse.ide.action.CreateChamrousseFileAction;

/**
 * Chamrousse extension entry point.
 *
 * @author Florent Benoit
 */
@Extension(title = "Chamrousse")
public class ChamrousseExtension {
  @Inject
  public ChamrousseExtension(
      FileTypeRegistry fileTypeRegistry,
      CreateChamrousseFileAction createChamrousseFileAction,
      ActionManager actionManager,
      org.eclipse.che.plugin.chamrousse.ide.ChamrousseResources chamrousseResources,
      IconRegistry iconRegistry,
      @Named("ChamrousseFileType") FileType chamrousseFile) {
    fileTypeRegistry.registerFileType(chamrousseFile);

    DefaultActionGroup newGroup = (DefaultActionGroup) actionManager.getAction(GROUP_FILE_NEW);
    actionManager.registerAction("chamrousseFile", createChamrousseFileAction);
    newGroup.add(createChamrousseFileAction, Constraints.FIRST);

    iconRegistry.registerIcon(
        new Icon(PYTHON_CATEGORY + ".samples.category.icon", chamrousseResources.category()));
  }
}
