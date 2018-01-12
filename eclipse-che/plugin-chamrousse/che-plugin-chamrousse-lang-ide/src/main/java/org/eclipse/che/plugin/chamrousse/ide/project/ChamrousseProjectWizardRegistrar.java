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
package org.eclipse.che.plugin.chamrousse.ide.project;

import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

/**
 * Provides information for registering Python project type into project wizard.
 *
 * @author Florent Benoit
 */
public class ChamrousseProjectWizardRegistrar implements ProjectWizardRegistrar {
  private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

  public ChamrousseProjectWizardRegistrar() {
    wizardPages = new ArrayList<>();
  }

  @NotNull
  public String getProjectTypeId() {
    return "chamrousse";
  }

  @NotNull
  public String getCategory() {
    return "chamrousse";
  }

  @NotNull
  public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
    return wizardPages;
  }
}
